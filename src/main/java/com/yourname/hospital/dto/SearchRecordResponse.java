package com.yourname.hospital.dto;

public class SearchRecordResponse {

    private Long recordId;
    private String patientCode;
    private String patientName;
    private String keywords;
    private String summary;
    private String updatedAt;

    public SearchRecordResponse(
            Long recordId,
            String patientCode,
            String patientName,
            String keywords,
            String summary,
            String updatedAt) {
        this.recordId = recordId;
        this.patientCode = patientCode;
        this.patientName = patientName;
        this.keywords = keywords;
        this.summary = summary;
        this.updatedAt = updatedAt;
    }

    public Long getRecordId() {
        return recordId;
    }

    public String getPatientCode() {
        return patientCode;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getKeywords() {
        return keywords;
    }

    public String getSummary() {
        return summary;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
