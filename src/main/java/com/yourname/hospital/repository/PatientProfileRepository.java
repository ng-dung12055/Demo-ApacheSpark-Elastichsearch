package com.yourname.hospital.repository;

import com.yourname.hospital.entity.PatientProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientProfileRepository extends JpaRepository<PatientProfile, Long> {
    Optional<PatientProfile> findByCccdHash(byte[] cccdHash);
}
