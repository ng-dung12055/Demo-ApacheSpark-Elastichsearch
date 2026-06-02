package com.yourname.hospital.dto;

public class MasterKeyBackupResponse {

    private Long id;
    private String type;
    private String status;
    private String createdAt;
    private String vaultLocation;
    private String keyValueEnc;

    public MasterKeyBackupResponse() {
    }

    public MasterKeyBackupResponse(
            Long id,
            String type,
            String status,
            String createdAt,
            String vaultLocation,
            String keyValueEnc) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.createdAt = createdAt;
        this.vaultLocation = vaultLocation;
        this.keyValueEnc = keyValueEnc;
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

    public String getKeyValueEnc() {
        return keyValueEnc;
    }

    public void setKeyValueEnc(String keyValueEnc) {
        this.keyValueEnc = keyValueEnc;
    }
}
