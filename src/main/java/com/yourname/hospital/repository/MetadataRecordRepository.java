package com.yourname.hospital.repository;

import com.yourname.hospital.entity.MetadataRecord;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MetadataRecordRepository extends JpaRepository<MetadataRecord, Long> {
    List<MetadataRecord> findByPatientCode(String patientCode);
    Page<MetadataRecord> findByPatientCode(String patientCode, Pageable pageable);
    List<MetadataRecord> findByRecord_Id(Long recordId);
    Page<MetadataRecord> findByRecord_Id(Long recordId, Pageable pageable);
    List<MetadataRecord> findByCccdHash(byte[] cccdHash);
    @Query(
            value = """
                    select m from MetadataRecord m
                    where m.patientCode = :patientCode
                      and (
                        lower(coalesce(m.keywords, '')) like lower(concat('%', :keyword, '%'))
                        or lower(coalesce(m.summary, '')) like lower(concat('%', :keyword, '%'))
                        or lower(coalesce(m.patientName, '')) like lower(concat('%', :keyword, '%'))
                      )
                    """,
            countQuery = """
                    select count(m) from MetadataRecord m
                    where m.patientCode = :patientCode
                      and (
                        lower(coalesce(m.keywords, '')) like lower(concat('%', :keyword, '%'))
                        or lower(coalesce(m.summary, '')) like lower(concat('%', :keyword, '%'))
                        or lower(coalesce(m.patientName, '')) like lower(concat('%', :keyword, '%'))
                      )
                    """
    )
    Page<MetadataRecord> searchByPatientCodeAndKeyword(
            @Param("patientCode") String patientCode,
            @Param("keyword") String keyword,
            Pageable pageable);
    Page<MetadataRecord> findByKeywordsContainingIgnoreCaseOrSummaryContainingIgnoreCaseOrPatientNameContainingIgnoreCase(
            String keywords,
            String summary,
            String patientName,
            Pageable pageable);
    long countByPatientCode(String patientCode);
    long countByRecord_Id(Long recordId);
    long countByKeywordsContainingIgnoreCaseOrSummaryContainingIgnoreCaseOrPatientNameContainingIgnoreCase(
            String keywords,
            String summary,
            String patientName);
}
