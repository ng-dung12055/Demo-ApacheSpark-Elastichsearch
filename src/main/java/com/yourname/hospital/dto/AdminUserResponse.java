package com.yourname.hospital.dto;

import java.util.List;

public class AdminUserResponse {

    private Long accountId;
    private String username;
    private String fullName;
    private List<String> roles;
    private boolean active;
    private String email;
    private String phone;

    public AdminUserResponse() {
    }

    public AdminUserResponse(
            Long accountId,
            String username,
            String fullName,
            List<String> roles,
            boolean active,
            String email,
            String phone) {
        this.accountId = accountId;
        this.username = username;
        this.fullName = fullName;
        this.roles = roles;
        this.active = active;
        this.email = email;
        this.phone = phone;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
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

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
