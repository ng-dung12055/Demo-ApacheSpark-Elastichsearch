package com.yourname.hospital.dto;

public class DoctorRecordDetailResponse {

    private Long recordId;
    private String status;
    private String createdAt;
    private String updatedAt;
    private String summary;
    private String keywords;
    private String diagnosis;
    private String securityLevel;

    public DoctorRecordDetailResponse() {
    }

    public DoctorRecordDetailResponse(
            Long recordId,
            String status,
            String createdAt,
            String updatedAt,
            String summary,
            String keywords,
            String diagnosis,
            String securityLevel) {
        this.recordId = recordId;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.summary = summary;
        this.keywords = keywords;
        this.diagnosis = diagnosis;
        this.securityLevel = securityLevel;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getSecurityLevel() {
        return securityLevel;
    }

    public void setSecurityLevel(String securityLevel) {
        this.securityLevel = securityLevel;
    }
}
