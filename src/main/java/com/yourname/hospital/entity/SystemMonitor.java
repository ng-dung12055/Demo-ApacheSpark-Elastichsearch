package com.yourname.hospital.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "giam_sat_he_thong")
public class SystemMonitor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sparkStatus;
    private String elasticStatus;
    private String storageUsage;
    private LocalDateTime updatedAt = LocalDateTime.now();

    public SystemMonitor() {
    }

    public SystemMonitor(String sparkStatus, String elasticStatus, String storageUsage) {
        this.sparkStatus = sparkStatus;
        this.elasticStatus = elasticStatus;
        this.storageUsage = storageUsage;
    }

    public Long getId() {
        return id;
    }

    public String getSparkStatus() {
        return sparkStatus;
    }

    public void setSparkStatus(String sparkStatus) {
        this.sparkStatus = sparkStatus;
    }

    public String getElasticStatus() {
        return elasticStatus;
    }

    public void setElasticStatus(String elasticStatus) {
        this.elasticStatus = elasticStatus;
    }

    public String getStorageUsage() {
        return storageUsage;
    }

    public void setStorageUsage(String storageUsage) {
        this.storageUsage = storageUsage;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
