package com.yourname.hospital.repository;

import com.yourname.hospital.entity.KeyEntity;
import com.yourname.hospital.entity.KeyStatus;
import com.yourname.hospital.entity.KeyType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeyEntityRepository extends JpaRepository<KeyEntity, Long> {
    Optional<KeyEntity> findByTypeAndStatus(KeyType type, KeyStatus status);
    List<KeyEntity> findByType(KeyType type);
}
