package com.yourname.hospital.dto;

import java.util.List;

public class DoctorPatientDetailResponse {

    private String patientCode;
    private String fullName;
    private String phone;
    private String email;
    private String dob;
    private String gender;
    private String address;
    private String medicalHistory;
    private List<RecordItem> records;

    public DoctorPatientDetailResponse() {
    }

    public DoctorPatientDetailResponse(
            String patientCode,
            String fullName,
            String phone,
            String email,
            String dob,
            String gender,
            String address,
            String medicalHistory,
            List<RecordItem> records) {
        this.patientCode = patientCode;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.dob = dob;
        this.gender = gender;
        this.address = address;
        this.medicalHistory = medicalHistory;
        this.records = records;
    }

    public String getPatientCode() {
        return patientCode;
    }

    public void setPatientCode(String patientCode) {
        this.patientCode = patientCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }

    public List<RecordItem> getRecords() {
        return records;
    }

    public void setRecords(List<RecordItem> records) {
        this.records = records;
    }

    public static class RecordItem {
        private Long recordId;
        private String status;
        private String createdAt;
        private String updatedAt;
        private String summary;
        private String keywords;

        public RecordItem() {
        }

        public RecordItem(
                Long recordId,
                String status,
                String createdAt,
                String updatedAt,
                String summary,
                String keywords) {
            this.recordId = recordId;
            this.status = status;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
            this.summary = summary;
            this.keywords = keywords;
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
    }
}
