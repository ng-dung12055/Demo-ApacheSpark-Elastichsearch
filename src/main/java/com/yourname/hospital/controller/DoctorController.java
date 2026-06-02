package com.yourname.hospital.controller;

import com.yourname.hospital.dto.ChangePasswordRequest;
import com.yourname.hospital.dto.CreateRecordRequest;
import com.yourname.hospital.dto.DoctorPatientBasicResponse;
import com.yourname.hospital.dto.DoctorPatientDetailResponse;
import com.yourname.hospital.dto.DoctorProfileResponse;
import com.yourname.hospital.dto.DoctorProfileUpdateRequest;
import com.yourname.hospital.dto.DoctorRecordResponse;
import com.yourname.hospital.dto.DoctorRecordDetailResponse;
import com.yourname.hospital.dto.SearchRecordPageResponse;
import com.yourname.hospital.dto.UpdateRecordRequest;
import com.yourname.hospital.entity.MedicalRecord;
import com.yourname.hospital.entity.MetadataRecord;
import com.yourname.hospital.security.AppUserDetails;
import com.yourname.hospital.service.DoctorService;
import com.yourname.hospital.service.SearchService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    private final DoctorService doctorService;
    private final SearchService searchService;

    public DoctorController(DoctorService doctorService, SearchService searchService) {
        this.doctorService = doctorService;
        this.searchService = searchService;
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('DOCTOR')")
    public List<MetadataRecord> search(
            @RequestParam(required = false) String patientCode,
            @RequestParam(required = false) String cccd) {
        if (patientCode != null) {
            try {
                return doctorService.searchByPatientCode(patientCode);
            } catch (IllegalArgumentException ex) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST, ex.getMessage());
            } catch (IllegalStateException ex) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, ex.getMessage());
            }
        }
        if (cccd != null) {
            return doctorService.searchByCccd(cccd);
        }
        throw new IllegalArgumentException("Missing patientCode or cccd");
    }

    @GetMapping("/search/keyword")
    @PreAuthorize("hasRole('DOCTOR')")
    public SearchRecordPageResponse searchByKeyword(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String patientCode,
            @RequestParam(required = false) Long recordId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            return searchService.searchRecordsPage(keyword, patientCode, recordId, page, size);
        } catch (IllegalArgumentException ex) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @GetMapping("/patient/{patientCode}")
    @PreAuthorize("hasRole('DOCTOR')")
    public DoctorPatientDetailResponse patientDetail(@PathVariable String patientCode) {
        try {
            return doctorService.getPatientDetail(patientCode);
        } catch (IllegalArgumentException ex) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (IllegalStateException ex) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    @GetMapping("/patient/{patientCode}/basic")
    @PreAuthorize("hasRole('DOCTOR')")
    public DoctorPatientBasicResponse patientBasic(@PathVariable String patientCode) {
        try {
            return doctorService.getPatientBasic(patientCode);
        } catch (IllegalArgumentException ex) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (IllegalStateException ex) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    @GetMapping("/record/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public DoctorRecordResponse recordDetail(@PathVariable Long id) {
        try {
            return doctorService.getRecordDetail(id);
        } catch (IllegalStateException ex) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    @GetMapping("/record/{id}/detail")
    @PreAuthorize("hasRole('DOCTOR')")
    public DoctorRecordDetailResponse recordFullDetail(@PathVariable Long id) {
        try {
            return doctorService.getRecordFullDetail(id);
        } catch (IllegalStateException ex) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('DOCTOR')")
    public DoctorProfileResponse profile(@org.springframework.security.core.annotation.AuthenticationPrincipal
                                                 com.yourname.hospital.security.AppUserDetails userDetails) {
        return doctorService.getProfile(userDetails.getUsername());
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('DOCTOR')")
    public DoctorProfileResponse updateProfile(
            @org.springframework.security.core.annotation.AuthenticationPrincipal
                    com.yourname.hospital.security.AppUserDetails userDetails,
            @RequestBody DoctorProfileUpdateRequest request) {
        return doctorService.updateProfile(userDetails.getUsername(), request);
    }

    @PutMapping("/password")
    @PreAuthorize("hasRole('DOCTOR')")
    public void changePassword(
            @AuthenticationPrincipal com.yourname.hospital.security.AppUserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        try {
            doctorService.changePassword(userDetails.getUsername(), request);
        } catch (IllegalStateException ex) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @PostMapping("/record")
    @PreAuthorize("hasRole('DOCTOR')")
    public MedicalRecord createRecord(
            @Valid @RequestBody CreateRecordRequest request,
            @AuthenticationPrincipal AppUserDetails userDetails) {
        if (userDetails == null
                || userDetails.getAccount() == null
                || userDetails.getAccount().getUser() == null
                || userDetails.getAccount().getUser().getId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Doctor context is not available");
        }
        return doctorService.createRecord(request, userDetails.getAccount().getUser().getId());
    }

    @PutMapping("/record/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public MedicalRecord updateRecord(@PathVariable Long id, @RequestBody UpdateRecordRequest request) {
        return doctorService.updateRecord(id, request);
    }

    @DeleteMapping("/record/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public void deleteRecord(@PathVariable Long id) {
        doctorService.deleteRecord(id);
    }
}
