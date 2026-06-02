package com.yourname.hospital.controller;

import com.yourname.hospital.dto.ChangePasswordRequest;
import com.yourname.hospital.dto.PatientHistoryPageResponse;
import com.yourname.hospital.dto.PatientProfileResponse;
import com.yourname.hospital.dto.PatientRecordDetailResponse;
import com.yourname.hospital.dto.UpdateProfileRequest;
import com.yourname.hospital.entity.MedicalRecord;
import com.yourname.hospital.security.AppUserDetails;
import com.yourname.hospital.service.PatientService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('PATIENT')")
    public PatientProfileResponse getProfile(@AuthenticationPrincipal AppUserDetails userDetails) {
        return patientService.getProfile(userDetails.getUsername());
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('PATIENT')")
    public PatientProfileResponse updateProfile(
            @AuthenticationPrincipal AppUserDetails userDetails,
            @RequestBody UpdateProfileRequest request) {
        return patientService.updateProfile(userDetails.getUsername(), request);
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('PATIENT')")
    public List<MedicalRecord> getHistory(@AuthenticationPrincipal AppUserDetails userDetails) {
        return patientService.listRecords(userDetails.getUsername());
    }

    @GetMapping("/history/page")
    @PreAuthorize("hasRole('PATIENT')")
    public PatientHistoryPageResponse getHistoryPage(
            @AuthenticationPrincipal AppUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String status) {
        return patientService.listRecordsPage(userDetails.getUsername(), page, size, status);
    }

    @GetMapping("/record/{id}/detail")
    @PreAuthorize("hasRole('PATIENT')")
    public PatientRecordDetailResponse recordDetail(
            @AuthenticationPrincipal AppUserDetails userDetails,
            @PathVariable Long id) {
        try {
            return patientService.getRecordDetail(userDetails.getUsername(), id);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    @PutMapping("/password")
    @PreAuthorize("hasRole('PATIENT')")
    public void changePassword(
            @AuthenticationPrincipal AppUserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        try {
            patientService.changePassword(userDetails.getUsername(), request);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }
}
