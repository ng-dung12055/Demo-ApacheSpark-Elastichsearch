package com.yourname.hospital.service;

import com.yourname.hospital.dto.SearchRecordPageResponse;
import com.yourname.hospital.dto.SearchRecordResponse;
import com.yourname.hospital.entity.MetadataRecord;
import com.yourname.hospital.repository.MetadataRecordRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class SearchService {

    private final ElasticsearchService elasticsearchService;
    private final MetadataRecordRepository metadataRepository;

    public SearchService(
            ElasticsearchService elasticsearchService,
            MetadataRecordRepository metadataRepository) {
        this.elasticsearchService = elasticsearchService;
        this.metadataRepository = metadataRepository;
    }

    public List<SearchRecordResponse> searchByKeyword(String keyword, int limit) {
        return searchRecords(keyword, null, null, limit);
    }

    public SearchRecordPageResponse searchRecordsPage(
            String keyword,
            String patientCode,
            Long recordId,
            int page,
            int size) {
        String normalizedKeyword = keyword != null ? keyword.trim() : "";
        String normalizedPatientCode = patientCode != null ? patientCode.trim() : "";
        boolean hasKeyword = !normalizedKeyword.isEmpty();
        boolean hasPatient = !normalizedPatientCode.isEmpty();
        boolean hasRecordId = recordId != null;

        if (!hasKeyword && !hasPatient && !hasRecordId) {
            throw new IllegalArgumentException("Missing search criteria");
        }
        if (hasPatient && !normalizedPatientCode.matches("^BN-\\d+$")) {
            throw new IllegalArgumentException("Invalid patient code format");
        }
        if (hasRecordId && recordId <= 0) {
            throw new IllegalArgumentException("Invalid record id");
        }

        int safeSize = Math.max(1, Math.min(size, 2000));
        int safePage = Math.max(0, page);
        if (elasticsearchService.isEnabled()) {
            try {
                return elasticsearchService.searchRecordsPage(
                        hasKeyword ? normalizedKeyword : null,
                        hasPatient ? normalizedPatientCode : null,
                        recordId,
                        safePage,
                        safeSize);
            } catch (Exception ignored) {
                // Fallback to database search.
            }
        }

        List<MetadataRecord> records = new ArrayList<>();
        long total;
        boolean manualPaging = true;
        boolean keywordFilteredByDb = false;

        if (hasRecordId && !hasKeyword) {
            var pageRequest = PageRequest.of(safePage, safeSize, Sort.by("updatedAt").descending());
            records = new ArrayList<>(metadataRepository.findByRecord_Id(recordId, pageRequest).getContent());
            total = metadataRepository.countByRecord_Id(recordId);
            manualPaging = false;
        } else if (hasPatient && hasKeyword && !hasRecordId) {
            var pageRequest = PageRequest.of(safePage, safeSize, Sort.by("updatedAt").descending());
            var pageResult = metadataRepository.searchByPatientCodeAndKeyword(
                    normalizedPatientCode,
                    normalizedKeyword,
                    pageRequest);
            records = new ArrayList<>(pageResult.getContent());
            total = pageResult.getTotalElements();
            manualPaging = false;
            keywordFilteredByDb = true;
        } else if (hasPatient && !hasKeyword) {
            var pageRequest = PageRequest.of(safePage, safeSize, Sort.by("updatedAt").descending());
            records = new ArrayList<>(metadataRepository.findByPatientCode(normalizedPatientCode, pageRequest)
                    .getContent());
            total = metadataRepository.countByPatientCode(normalizedPatientCode);
            manualPaging = false;
        } else if (!hasPatient && !hasRecordId) {
            PageRequest pageRequest = PageRequest.of(safePage, safeSize, Sort.by("updatedAt").descending());
            var pageResult = metadataRepository
                    .findByKeywordsContainingIgnoreCaseOrSummaryContainingIgnoreCaseOrPatientNameContainingIgnoreCase(
                            normalizedKeyword,
                            normalizedKeyword,
                            normalizedKeyword,
                            pageRequest);
            records = new ArrayList<>(pageResult.getContent());
            total = pageResult.getTotalElements();
            manualPaging = false;
        } else if (hasRecordId) {
            records = new ArrayList<>(metadataRepository.findByRecord_Id(recordId));
            total = records.size();
        } else {
            records = new ArrayList<>(metadataRepository.findByPatientCode(normalizedPatientCode));
            total = records.size();
        }

        if ((hasPatient || hasRecordId) && hasKeyword && !keywordFilteredByDb) {
            String lowerKeyword = normalizedKeyword.toLowerCase(Locale.ROOT);
            records = records.stream()
                    .filter(record -> matchesKeyword(record, lowerKeyword))
                    .toList();
            total = records.size();
        }
        if (hasRecordId) {
            records = records.stream()
                    .filter(record -> record.getRecord() != null
                            && recordId.equals(record.getRecord().getId()))
                    .toList();
            total = records.size();
        }

        if (manualPaging) {
            records = new ArrayList<>(records);
            records.sort(Comparator.comparing(
                    MetadataRecord::getUpdatedAt,
                    Comparator.nullsLast(Comparator.naturalOrder())).reversed());
            int from = safePage * safeSize;
            if (from >= records.size()) {
                records = List.of();
            } else {
                int to = Math.min(records.size(), from + safeSize);
                records = records.subList(from, to);
            }
        }

        return new SearchRecordPageResponse(
                records.stream().map(this::toResponse).toList(),
                total,
                safePage,
                safeSize);
    }

    public List<SearchRecordResponse> searchRecords(
            String keyword,
            String patientCode,
            Long recordId,
            int limit) {
        SearchRecordPageResponse page = searchRecordsPage(keyword, patientCode, recordId, 0, limit);
        return page.getItems();
    }

    private SearchRecordResponse toResponse(MetadataRecord record) {
        Long recordId = record.getRecord() != null ? record.getRecord().getId() : null;
        return new SearchRecordResponse(
                recordId,
                record.getPatientCode(),
                record.getPatientName(),
                record.getKeywords(),
                record.getSummary(),
                record.getUpdatedAt().toString());
    }

    private boolean matchesKeyword(MetadataRecord record, String lowerKeyword) {
        return containsIgnoreCase(record.getPatientName(), lowerKeyword)
                || containsIgnoreCase(record.getKeywords(), lowerKeyword)
                || containsIgnoreCase(record.getSummary(), lowerKeyword);
    }

    private boolean containsIgnoreCase(String value, String lowerKeyword) {
        if (value == null) {
            return false;
        }
        return value.toLowerCase(Locale.ROOT).contains(lowerKeyword);
    }
}
