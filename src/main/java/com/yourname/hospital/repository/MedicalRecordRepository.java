package com.yourname.hospital.repository;

import com.yourname.hospital.entity.MedicalRecord;
import com.yourname.hospital.entity.Patient;
import com.yourname.hospital.entity.RecordStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    List<MedicalRecord> findByPatient(Patient patient);
    Page<MedicalRecord> findByPatient(Patient patient, Pageable pageable);
    Page<MedicalRecord> findByPatientAndStatus(Patient patient, RecordStatus status, Pageable pageable);
}
