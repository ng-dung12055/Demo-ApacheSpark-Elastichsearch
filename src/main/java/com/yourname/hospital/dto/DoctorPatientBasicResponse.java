package com.yourname.hospital.dto;

public class DoctorPatientBasicResponse {

    private String patientCode;
    private String fullName;
    private String phone;
    private String email;
    private String dob;
    private String gender;
    private String address;

    public DoctorPatientBasicResponse() {
    }

    public DoctorPatientBasicResponse(
            String patientCode,
            String fullName,
            String phone,
            String email,
            String dob,
            String gender,
            String address) {
        this.patientCode = patientCode;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
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
