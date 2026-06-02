package com.yourname.hospital.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ho_so_benh_an")
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "benh_nhan_id")
    private Patient patient;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private RecordStatus status = RecordStatus.PROCESSING;

    @Enumerated(EnumType.STRING)
    private SecurityLevel securityLevel = SecurityLevel.CONFIDENTIAL;

    @JsonIgnore
    @OneToMany(mappedBy = "record", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Diagnosis> diagnoses = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "record", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LabResult> labResults = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "record", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Prescription> prescriptions = new ArrayList<>();

    @OneToOne(mappedBy = "record", cascade = CascadeType.ALL, orphanRemoval = true)
    private MetadataRecord metadata;

    @JsonIgnore
    @OneToOne(mappedBy = "record", cascade = CascadeType.ALL, orphanRemoval = true)
    private EncryptedContent encryptedContent;

    public MedicalRecord() {
    }

    public Long getId() {
        return id;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public RecordStatus getStatus() {
        return status;
    }

    public void setStatus(RecordStatus status) {
        this.status = status;
    }

    public SecurityLevel getSecurityLevel() {
        return securityLevel;
    }

    public void setSecurityLevel(SecurityLevel securityLevel) {
        this.securityLevel = securityLevel;
    }

    public List<Diagnosis> getDiagnoses() {
        return diagnoses;
    }

    public List<LabResult> getLabResults() {
        return labResults;
    }

    public List<Prescription> getPrescriptions() {
        return prescriptions;
    }

    public MetadataRecord getMetadata() {
        return metadata;
    }

    public void setMetadata(MetadataRecord metadata) {
        this.metadata = metadata;
    }

    public EncryptedContent getEncryptedContent() {
        return encryptedContent;
    }

    public void setEncryptedContent(EncryptedContent encryptedContent) {
        this.encryptedContent = encryptedContent;
    }
}
