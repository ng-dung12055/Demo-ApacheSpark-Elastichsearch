package com.yourname.hospital.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "kho_khoa")
public class KeyVault {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String storageLocation;
    private LocalDateTime updatedAt = LocalDateTime.now();

    public KeyVault() {
    }

    public KeyVault(String storageLocation) {
        this.storageLocation = storageLocation;
    }

    public Long getId() {
        return id;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
