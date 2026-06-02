package com.yourname.hospital.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "khoa")
public class KeyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private KeyType type;

    @Column(length = 2000)
    private String keyValueEnc;

    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private KeyStatus status = KeyStatus.ACTIVE;

    @ManyToOne
    @JoinColumn(name = "kho_khoa_id")
    private KeyVault vault;

    public KeyEntity() {
    }

    public KeyEntity(KeyType type, String keyValueEnc, KeyVault vault) {
        this.type = type;
        this.keyValueEnc = keyValueEnc;
        this.vault = vault;
    }

    public Long getId() {
        return id;
    }

    public KeyType getType() {
        return type;
    }

    public void setType(KeyType type) {
        this.type = type;
    }

    public String getKeyValueEnc() {
        return keyValueEnc;
    }

    public void setKeyValueEnc(String keyValueEnc) {
        this.keyValueEnc = keyValueEnc;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public KeyStatus getStatus() {
        return status;
    }

    public void setStatus(KeyStatus status) {
        this.status = status;
    }

    public KeyVault getVault() {
        return vault;
    }

    public void setVault(KeyVault vault) {
        this.vault = vault;
    }
}
