package com.yourname.hospital.dto;

public class MasterKeySummaryResponse {

    private Long id;
    private String type;
    private String status;
    private String createdAt;
    private String vaultLocation;

    public MasterKeySummaryResponse() {
    }

    public MasterKeySummaryResponse(
            Long id,
            String type,
            String status,
            String createdAt,
            String vaultLocation) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.createdAt = createdAt;
        this.vaultLocation = vaultLocation;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getVaultLocation() {
        return vaultLocation;
    }

    public void setVaultLocation(String vaultLocation) {
        this.vaultLocation = vaultLocation;
    }
}
