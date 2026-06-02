package com.yourname.hospital.repository;

import com.yourname.hospital.entity.KeyVault;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeyVaultRepository extends JpaRepository<KeyVault, Long> {
}
