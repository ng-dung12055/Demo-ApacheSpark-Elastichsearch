package com.yourname.hospital.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chuan_doan")
public class Diagnosis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "ho_so_id")
    private MedicalRecord record;

    private String icdCode;

    @Column(name = "mo_ta_enc", length = 2000)
    private String descriptionEnc;

    private LocalDateTime createdAt = LocalDateTime.now();
    private Long doctorId;

    public Diagnosis() {
    }

    public Long getId() {
        return id;
    }

    public MedicalRecord getRecord() {
        return record;
    }

    public void setRecord(MedicalRecord record) {
        this.record = record;
    }

    public String getIcdCode() {
        return icdCode;
    }

    public void setIcdCode(String icdCode) {
        this.icdCode = icdCode;
    }

    public String getDescriptionEnc() {
        return descriptionEnc;
    }

    public void setDescriptionEnc(String descriptionEnc) {
        this.descriptionEnc = descriptionEnc;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }
}
