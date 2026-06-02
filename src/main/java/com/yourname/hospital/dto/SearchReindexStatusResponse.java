package com.yourname.hospital.dto;

public class SearchReindexStatusResponse {

    private String jobId;
    private String status;
    private int processed;
    private int total;
    private int indexed;
    private boolean running;
    private String message;
    private String startedAt;
    private String finishedAt;

    public SearchReindexStatusResponse(
            String jobId,
            String status,
            int processed,
            int total,
            int indexed,
            boolean running,
            String message,
            String startedAt,
            String finishedAt) {
        this.jobId = jobId;
        this.status = status;
        this.processed = processed;
        this.total = total;
        this.indexed = indexed;
        this.running = running;
        this.message = message;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
    }

    public String getJobId() {
        return jobId;
    }

    public String getStatus() {
        return status;
    }

    public int getProcessed() {
        return processed;
    }

    public int getTotal() {
        return total;
    }

    public int getIndexed() {
        return indexed;
    }

    public boolean isRunning() {
        return running;
    }

    public String getMessage() {
        return message;
    }

    public String getStartedAt() {
        return startedAt;
    }

    public String getFinishedAt() {
        return finishedAt;
    }
}
