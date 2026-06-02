package com.yourname.hospital.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "ho_so_ca_nhan")
public class PatientProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "benh_nhan_id")
    private Patient patient;

    @Column(name = "cccd_enc")
    private String cccdEnc;

    @Column(name = "cccd_hash", unique = true, columnDefinition = "BINARY(32)")
    private byte[] cccdHash;

    private LocalDate dob;
    private String gender;
    private String address;

    @Column(length = 2000)
    private String medicalHistory;

    public PatientProfile() {
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

    public String getCccdEnc() {
        return cccdEnc;
    }

    public void setCccdEnc(String cccdEnc) {
        this.cccdEnc = cccdEnc;
    }

    public byte[] getCccdHash() {
        return cccdHash;
    }

    public void setCccdHash(byte[] cccdHash) {
        this.cccdHash = cccdHash;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
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
}
