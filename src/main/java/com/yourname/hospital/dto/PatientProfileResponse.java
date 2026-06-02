package com.yourname.hospital.dto;

public class PatientProfileResponse {

    private String patientCode;
    private String fullName;
    private String phone;
    private String email;
    private String cccd;
    private String dob;
    private String gender;
    private String address;

    public PatientProfileResponse() {
    }

    public PatientProfileResponse(
            String patientCode,
            String fullName,
            String phone,
            String email,
            String cccd,
            String dob,
            String gender,
            String address) {
        this.patientCode = patientCode;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.cccd = cccd;
        this.dob = dob;
        this.gender = gender;
        this.address = address;
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

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
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
}
