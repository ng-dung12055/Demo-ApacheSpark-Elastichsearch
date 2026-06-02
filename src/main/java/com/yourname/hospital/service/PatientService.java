package com.yourname.hospital.service;

import com.yourname.hospital.dto.ChangePasswordRequest;
import com.yourname.hospital.dto.PatientHistoryPageResponse;
import com.yourname.hospital.dto.PatientProfileResponse;
import com.yourname.hospital.dto.PatientRecordDetailResponse;
import com.yourname.hospital.dto.UpdateProfileRequest;
import com.yourname.hospital.entity.Account;
import com.yourname.hospital.entity.EncryptedContent;
import com.yourname.hospital.entity.KeyEntity;
import com.yourname.hospital.entity.MedicalRecord;
import com.yourname.hospital.entity.MetadataRecord;
import com.yourname.hospital.entity.Patient;
import com.yourname.hospital.entity.PatientProfile;
import com.yourname.hospital.entity.RecordStatus;
import com.yourname.hospital.entity.SecurityLevel;
import com.yourname.hospital.repository.AccountRepository;
import com.yourname.hospital.repository.KeyEntityRepository;
import com.yourname.hospital.repository.MedicalRecordRepository;
import com.yourname.hospital.repository.PatientRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final MedicalRecordRepository recordRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final KeyEntityRepository keyRepository;
    private final CryptoService cryptoService;
    private final KeyService keyService;

    public PatientService(
            PatientRepository patientRepository,
            MedicalRecordRepository recordRepository,
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder,
            KeyEntityRepository keyRepository,
            CryptoService cryptoService,
            KeyService keyService) {
        this.patientRepository = patientRepository;
        this.recordRepository = recordRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.keyRepository = keyRepository;
        this.cryptoService = cryptoService;
        this.keyService = keyService;
    }

    public PatientProfileResponse getProfile(String username) {
        Patient patient = getPatientByUsername(username);
        return buildProfileResponse(patient);
    }

    @Transactional
    public PatientProfileResponse updateProfile(String username, UpdateProfileRequest request) {
        Patient patient = getPatientByUsername(username);
        Account account = accountRepository.findByUsername(username).orElseThrow();
        if (request.getFullName() != null) {
            account.getUser().setFullName(request.getFullName());
        }
        if (request.getEmail() != null) {
            account.getUser().setEmail(request.getEmail());
        }
        PatientProfile profile = patient.getProfile();
        if (request.getPhone() != null) {
            account.getUser().setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            profile.setAddress(request.getAddress());
        }
        if (request.getGender() != null) {
            profile.setGender(request.getGender());
        }
        if (request.getDob() != null && !request.getDob().isBlank()) {
            profile.setDob(LocalDate.parse(request.getDob()));
        }
        if (request.getCccd() != null && !request.getCccd().isBlank()) {
            byte[] masterKey = keyService.getActiveMasterKey();
            profile.setCccdEnc(cryptoService.encryptText(request.getCccd(), masterKey));
            profile.setCccdHash(cryptoService.sha256(request.getCccd()));
        }
        return buildProfileResponse(patient);
    }

    public List<MedicalRecord> listRecords(String username) {
        Patient patient = getPatientByUsername(username);
        return recordRepository.findByPatient(patient);
    }

    public PatientHistoryPageResponse listRecordsPage(String username, int page, int size, String status) {
        Patient patient = getPatientByUsername(username);
        int safeSize = Math.max(1, Math.min(size, 200));
        int safePage = Math.max(0, page);
        PageRequest pageRequest = PageRequest.of(
                safePage,
                safeSize,
                Sort.by("updatedAt").descending());
        RecordStatus desiredStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                desiredStatus = RecordStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                desiredStatus = null;
            }
        }
        var pageResult = desiredStatus != null
                ? recordRepository.findByPatientAndStatus(patient, desiredStatus, pageRequest)
                : recordRepository.findByPatient(patient, pageRequest);
        return new PatientHistoryPageResponse(
                pageResult.getContent(),
                pageResult.getTotalElements(),
                safePage,
                safeSize);
    }

    public PatientRecordDetailResponse getRecordDetail(String username, Long recordId) {
        Patient patient = getPatientByUsername(username);
        MedicalRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalStateException("Record not found"));
        if (record.getPatient() == null || !record.getPatient().getId().equals(patient.getId())) {
            throw new IllegalStateException("Record not found");
        }
        MetadataRecord metadata = record.getMetadata();
        SecurityLevel level = record.getSecurityLevel() != null
                ? record.getSecurityLevel()
                : SecurityLevel.CONFIDENTIAL;
        String diagnosis = null;
        EncryptedContent encrypted = record.getEncryptedContent();
        if (encrypted != null && encrypted.getKeyId() != null && level != SecurityLevel.RESTRICTED) {
            try {
                KeyEntity key = keyRepository.findById(encrypted.getKeyId()).orElse(null);
                if (key != null) {
                    byte[] dataKey = keyService.unwrapDataKey(key.getKeyValueEnc());
                    String encryptedData = encrypted.getEncryptedData();
                    boolean integrityValid = cryptoService.verifyHmacSha256(
                            encryptedData,
                            encrypted.getIntegrityHash(),
                            dataKey);
                    if (!integrityValid) {
                        throw new IllegalStateException("Encrypted content integrity check failed");
                    }
                    diagnosis = cryptoService.decryptText(encryptedData, dataKey);
                }
            } catch (Exception ex) {
                diagnosis = null;
            }
        }
        return new PatientRecordDetailResponse(
                record.getId(),
                record.getStatus().name(),
                record.getCreatedAt().toString(),
                record.getUpdatedAt().toString(),
                metadata != null ? metadata.getSummary() : null,
                metadata != null ? metadata.getKeywords() : null,
                diagnosis,
                level.name()
        );
    }

    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalStateException("Password confirmation does not match");
        }
        Account account = accountRepository.findByUsername(username).orElseThrow();
        if (!passwordEncoder.matches(request.getCurrentPassword(), account.getPasswordHash())) {
            throw new IllegalStateException("Current password incorrect");
        }
        if (passwordEncoder.matches(request.getNewPassword(), account.getPasswordHash())) {
            throw new IllegalStateException("New password must be different");
        }
        account.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
    }

    private Patient getPatientByUsername(String username) {
        Account account = accountRepository.findByUsername(username).orElseThrow();
        return patientRepository.findByUserId(account.getUser().getId())
                .orElseThrow(() -> new IllegalStateException("Patient not found"));
    }

    private PatientProfileResponse buildProfileResponse(Patient patient) {
        PatientProfile profile = patient.getProfile();
        String cccd = null;
        if (profile != null && profile.getCccdEnc() != null) {
            try {
                cccd = cryptoService.decryptText(profile.getCccdEnc(), keyService.getActiveMasterKey());
            } catch (Exception ex) {
                cccd = null;
            }
        }
        return new PatientProfileResponse(
                patient.getPatientCode(),
                patient.getUser().getFullName(),
                patient.getUser().getPhone(),
                patient.getUser().getEmail(),
                cccd,
                profile != null && profile.getDob() != null ? profile.getDob().toString() : null,
                profile != null ? profile.getGender() : null,
                profile != null ? profile.getAddress() : null);
    }
}
