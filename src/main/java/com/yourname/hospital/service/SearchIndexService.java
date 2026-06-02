package com.yourname.hospital.service;

import com.yourname.hospital.dto.SearchReindexResponse;
import com.yourname.hospital.dto.SearchReindexStatusResponse;
import com.yourname.hospital.entity.MetadataRecord;
import com.yourname.hospital.repository.MetadataRecordRepository;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.stereotype.Service;

@Service
public class SearchIndexService {

    private static final String STATUS_IDLE = "IDLE";
    private static final String STATUS_RUNNING = "RUNNING";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_FAILED = "FAILED";
    private static final String STATUS_SKIPPED = "SKIPPED";

    private final ElasticsearchService elasticsearchService;
    private final MetadataRecordRepository metadataRepository;
    private final SparkJobService sparkJobService;
    private final ExecutorService reindexExecutor;
    private final Object reindexLock = new Object();
    private volatile ReindexState reindexState = ReindexState.idle();

    public SearchIndexService(
            ElasticsearchService elasticsearchService,
            MetadataRecordRepository metadataRepository,
            SparkJobService sparkJobService) {
        this.elasticsearchService = elasticsearchService;
        this.metadataRepository = metadataRepository;
        this.sparkJobService = sparkJobService;
        this.reindexExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r);
            thread.setName("search-reindex-worker");
            thread.setDaemon(true);
            return thread;
        });
    }

    public void indexMetadata(MetadataRecord metadata) {
        if (!elasticsearchService.isEnabled() || metadata == null) {
            return;
        }
        try {
            elasticsearchService.indexMetadata(metadata);
        } catch (Exception ignored) {
            // Avoid failing the main transaction on search indexing errors.
        }
    }

    public void deleteMetadata(Long recordId) {
        if (!elasticsearchService.isEnabled() || recordId == null) {
            return;
        }
        try {
            elasticsearchService.deleteMetadata(recordId);
        } catch (Exception ignored) {
            // Ignore delete failures to keep core flows stable.
        }
    }

    public SearchReindexResponse reindexAll() {
        synchronized (reindexLock) {
            if (reindexState.isRunning()) {
                return new SearchReindexResponse(
                        reindexState.getIndexed(),
                        "ALREADY_RUNNING",
                        "Reindex is already in progress.");
            }

            String jobId = UUID.randomUUID().toString();
            boolean sparkEnabled = sparkJobService.isEnabled();
            reindexState = ReindexState.running(jobId, sparkEnabled);
            reindexExecutor.submit(() -> runReindex(jobId, sparkEnabled));
            return new SearchReindexResponse(
                    0,
                    "STARTED",
                    "Reindex started. jobId=" + jobId);
        }
    }

    public SearchReindexStatusResponse getReindexStatus() {
        return reindexState.toResponse();
    }

    @PreDestroy
    public void shutdownExecutor() {
        reindexExecutor.shutdownNow();
    }

    private void runReindex(String jobId, boolean sparkEnabled) {
        if (sparkEnabled) {
            runSparkReindex(jobId);
            return;
        }
        runDirectReindex(jobId);
    }

    private void runSparkReindex(String jobId) {
        SparkJobService.SparkJobResult result = sparkJobService.runReindexJob();
        int total = (int) Math.min(Integer.MAX_VALUE, metadataRepository.count());
        if ("SPARK_OK".equalsIgnoreCase(result.getStatus())) {
            updateFinalState(
                    jobId,
                    STATUS_COMPLETED,
                    total,
                    total,
                    total,
                    trimMessage(result.getOutput()));
            return;
        }
        updateFinalState(
                jobId,
                STATUS_FAILED,
                0,
                total,
                0,
                trimMessage(result.getOutput()));
    }

    private void runDirectReindex(String jobId) {
        if (!elasticsearchService.isEnabled()) {
            updateFinalState(jobId, STATUS_SKIPPED, 0, 0, 0, "Elasticsearch is disabled.");
            return;
        }

        List<MetadataRecord> all = metadataRepository.findAll();
        int total = all.size();
        int indexed = 0;
        String firstError = null;

        updateProgressState(jobId, 0, total, 0, "Starting reindex...");
        for (int i = 0; i < total; i++) {
            MetadataRecord record = all.get(i);
            try {
                elasticsearchService.indexMetadata(record);
                indexed += 1;
            } catch (Exception ex) {
                if (firstError == null) {
                    firstError = ex.getMessage();
                }
            }

            int processed = i + 1;
            if (processed == total || processed % 100 == 0) {
                updateProgressState(jobId, processed, total, indexed, null);
            }
        }

        String message = firstError != null
                ? "Completed with some indexing errors: " + firstError
                : null;
        updateFinalState(jobId, STATUS_COMPLETED, total, total, indexed, message);
    }

    private void updateProgressState(String jobId, int processed, int total, int indexed, String message) {
        synchronized (reindexLock) {
            if (!reindexState.matches(jobId)) {
                return;
            }
            reindexState = reindexState.withProgress(processed, total, indexed, message);
        }
    }

    private void updateFinalState(String jobId, String status, int processed, int total, int indexed, String message) {
        synchronized (reindexLock) {
            if (!reindexState.matches(jobId)) {
                return;
            }
            reindexState = reindexState.finished(status, processed, total, indexed, message);
        }
    }

    private String trimMessage(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.length() > 800 ? trimmed.substring(0, 800) + "..." : trimmed;
    }

    private static class ReindexState {
        private final String jobId;
        private final String status;
        private final int processed;
        private final int total;
        private final int indexed;
        private final boolean running;
        private final String message;
        private final LocalDateTime startedAt;
        private final LocalDateTime finishedAt;
        private final boolean sparkEnabled;

        private ReindexState(
                String jobId,
                String status,
                int processed,
                int total,
                int indexed,
                boolean running,
                String message,
                LocalDateTime startedAt,
                LocalDateTime finishedAt,
                boolean sparkEnabled) {
            this.jobId = jobId;
            this.status = status;
            this.processed = processed;
            this.total = total;
            this.indexed = indexed;
            this.running = running;
            this.message = message;
            this.startedAt = startedAt;
            this.finishedAt = finishedAt;
            this.sparkEnabled = sparkEnabled;
        }

        static ReindexState idle() {
            return new ReindexState(
                    null,
                    STATUS_IDLE,
                    0,
                    0,
                    0,
                    false,
                    null,
                    null,
                    null,
                    false);
        }

        static ReindexState running(String jobId, boolean sparkEnabled) {
            return new ReindexState(
                    jobId,
                    STATUS_RUNNING,
                    0,
                    0,
                    0,
                    true,
                    sparkEnabled ? "Spark reindex is running..." : "Reindex is running...",
                    LocalDateTime.now(),
                    null,
                    sparkEnabled);
        }

        boolean isRunning() {
            return running;
        }

        int getIndexed() {
            return indexed;
        }

        boolean matches(String otherJobId) {
            return jobId != null && jobId.equals(otherJobId);
        }

        ReindexState withProgress(int processed, int total, int indexed, String message) {
            return new ReindexState(
                    jobId,
                    STATUS_RUNNING,
                    processed,
                    total,
                    indexed,
                    true,
                    message != null ? message : this.message,
                    startedAt,
                    null,
                    sparkEnabled);
        }

        ReindexState finished(String status, int processed, int total, int indexed, String message) {
            return new ReindexState(
                    jobId,
                    status,
                    processed,
                    total,
                    indexed,
                    false,
                    message,
                    startedAt,
                    LocalDateTime.now(),
                    sparkEnabled);
        }

        SearchReindexStatusResponse toResponse() {
            return new SearchReindexStatusResponse(
                    jobId,
                    status,
                    processed,
                    total,
                    indexed,
                    running,
                    message,
                    startedAt != null ? startedAt.toString() : null,
                    finishedAt != null ? finishedAt.toString() : null);
        }
    }
}
