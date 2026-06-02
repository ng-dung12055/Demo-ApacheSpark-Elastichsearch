package com.yourname.hospital.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "metadata_ho_so")
public class MetadataRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "ho_so_id")
    private MedicalRecord record;

    private String patientCode;

    @Column(columnDefinition = "BINARY(32)")
    private byte[] cccdHash;

    private String patientName;
    private String keywords;

    @Column(length = 2000)
    private String summary;

    private LocalDateTime updatedAt = LocalDateTime.now();

    public MetadataRecord() {
    }

    public Long getId() {
        return id;
    }

    public MedicalRecord getRecord() {
        return record;
    }

    @JsonProperty("recordId")
    public Long getRecordId() {
        return record != null ? record.getId() : null;
    }

    public void setRecord(MedicalRecord record) {
        this.record = record;
    }

    public String getPatientCode() {
        return patientCode;
    }

    public void setPatientCode(String patientCode) {
        this.patientCode = patientCode;
    }

    public byte[] getCccdHash() {
        return cccdHash;
    }

    public void setCccdHash(byte[] cccdHash) {
        this.cccdHash = cccdHash;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
