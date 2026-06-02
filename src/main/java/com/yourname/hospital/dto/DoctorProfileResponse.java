package com.yourname.hospital.dto;

public class DoctorProfileResponse {

    private String username;
    private String fullName;
    private String phone;
    private String email;
    private String staffCode;
    private String department;
    private String title;
    private String specialty;
    private String licenseNumber;
    private String facility;
    private String shift;
    private String employmentStatus;
    private String internalPhone;
    private String workAddress;
    private String gender;
    private String dob;
    private String avatarUrl;
    private String role;
    private String accountStatus;

    public DoctorProfileResponse() {
    }

    public DoctorProfileResponse(
            String username,
            String fullName,
            String phone,
            String email,
            String staffCode,
            String department,
            String title,
            String specialty,
            String licenseNumber,
            String facility,
            String shift,
            String employmentStatus,
            String internalPhone,
            String workAddress,
            String gender,
            String dob,
            String avatarUrl,
            String role,
            String accountStatus) {
        this.username = username;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.staffCode = staffCode;
        this.department = department;
        this.title = title;
        this.specialty = specialty;
        this.licenseNumber = licenseNumber;
        this.facility = facility;
        this.shift = shift;
        this.employmentStatus = employmentStatus;
        this.internalPhone = internalPhone;
        this.workAddress = workAddress;
        this.gender = gender;
        this.dob = dob;
        this.avatarUrl = avatarUrl;
        this.role = role;
        this.accountStatus = accountStatus;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getStaffCode() {
        return staffCode;
    }

    public void setStaffCode(String staffCode) {
        this.staffCode = staffCode;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getFacility() {
        return facility;
    }

    public void setFacility(String facility) {
        this.facility = facility;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public String getEmploymentStatus() {
        return employmentStatus;
    }

    public void setEmploymentStatus(String employmentStatus) {
        this.employmentStatus = employmentStatus;
    }

    public String getInternalPhone() {
        return internalPhone;
    }

    public void setInternalPhone(String internalPhone) {
        this.internalPhone = internalPhone;
    }

    public String getWorkAddress() {
        return workAddress;
    }

    public void setWorkAddress(String workAddress) {
        this.workAddress = workAddress;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }
}
