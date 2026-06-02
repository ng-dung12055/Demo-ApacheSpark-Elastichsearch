package com.yourname.hospital.repository;

import com.yourname.hospital.entity.Staff;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByStaffCode(String staffCode);
    Optional<Staff> findByUserId(Long userId);
}
