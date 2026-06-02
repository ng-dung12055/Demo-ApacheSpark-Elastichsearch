package com.yourname.hospital.repository;

import com.yourname.hospital.entity.EncryptedContent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EncryptedContentRepository extends JpaRepository<EncryptedContent, Long> {
}
