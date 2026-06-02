package com.yourname.hospital.service;

import com.yourname.hospital.dto.ChangePasswordRequest;
import com.yourname.hospital.dto.CreateRecordRequest;
import com.yourname.hospital.dto.DoctorPatientBasicResponse;
import com.yourname.hospital.dto.DoctorProfileResponse;
import com.yourname.hospital.dto.DoctorProfileUpdateRequest;
import com.yourname.hospital.dto.DoctorRecordDetailResponse;
import com.yourname.hospital.dto.DoctorRecordResponse;
import com.yourname.hospital.dto.UpdateRecordRequest;
import com.yourname.hospital.entity.*;
import com.yourname.hospital.repository.AccountRepository;
import com.yourname.hospital.repository.KeyEntityRepository;
import com.yourname.hospital.repository.MetadataRecordRepository;
import com.yourname.hospital.repository.MedicalRecordRepository;
import com.yourname.hospital.repository.PatientRepository;
import com.yourname.hospital.repository.StaffRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DoctorService {

    private final MedicalRecordRepository recordRepository;
    private final MetadataRecordRepository metadataRepository;
    private final PatientRepository patientRepository;
    private final KeyEntityRepository keyRepository;
    private final CryptoService cryptoService;
    private final KeyService keyService;
    private final SearchIndexService searchIndexService;
    private final AccountRepository accountRepository;
    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;

    public DoctorService(
            MedicalRecordRepository recordRepository,
            MetadataRecordRepository metadataRepository,
            PatientRepository patientRepository,
            KeyEntityRepository keyRepository,
            CryptoService cryptoService,
            KeyService keyService,
            SearchIndexService searchIndexService,
            AccountRepository accountRepository,
            StaffRepository staffRepository,
            PasswordEncoder passwordEncoder) {
        this.recordRepository = recordRepository;
        this.metadataRepository = metadataRepository;
        this.patientRepository = patientRepository;
        this.keyRepository = keyRepository;
        this.cryptoService = cryptoService;
        this.keyService = keyService;
        this.searchIndexService = searchIndexService;
        this.accountRepository = accountRepository;
        this.staffRepository = staffRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public DoctorProfileResponse getProfile(String username) {
        Account account = accountRepository.findByUsername(username).orElseThrow();
        User user = account.getUser();
        Staff staff = staffRepository.findByUserId(user.getId()).orElse(null);
        String roleName = user.getRoles().stream().findFirst().map(Role::getName).orElse("DOCTOR");
        String accountStatus = account.isActive() ? "ACTIVE" : "LOCKED";
        return new DoctorProfileResponse(
                account.getUsername(),
                user.getFullName(),
                user.getPhone(),
                user.getEmail(),
                staff != null ? staff.getStaffCode() : null,
                staff != null ? staff.getDepartment() : null,
                staff != null ? staff.getTitle() : null,
                staff != null ? staff.getSpecialty() : null,
                staff != null ? staff.getLicenseNumber() : null,
                staff != null ? staff.getFacility() : null,
                staff != null ? staff.getShift() : null,
                staff != null ? staff.getEmploymentStatus() : null,
                staff != null ? staff.getInternalPhone() : null,
                staff != null ? staff.getWorkAddress() : null,
                staff != null ? staff.getGender() : null,
                staff != null && staff.getDob() != null ? staff.getDob().toString() : null,
                staff != null ? staff.getAvatarUrl() : null,
                roleName,
                accountStatus);
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

    @Transactional
    public DoctorProfileResponse updateProfile(String username, DoctorProfileUpdateRequest request) {
        Account account = accountRepository.findByUsername(username).orElseThrow();
        User user = account.getUser();
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }

        Staff staff = staffRepository.findByUserId(user.getId()).orElse(null);
        if (staff == null) {
            staff = new Staff("BS-" + user.getId(), null, null, user);
        }
        if (request.getDepartment() != null) {
            staff.setDepartment(request.getDepartment());
        }
        if (request.getTitle() != null) {
            staff.setTitle(request.getTitle());
        }
        if (request.getSpecialty() != null) {
            staff.setSpecialty(request.getSpecialty());
        }
        if (request.getLicenseNumber() != null) {
            staff.setLicenseNumber(request.getLicenseNumber());
        }
        if (request.getFacility() != null) {
            staff.setFacility(request.getFacility());
        }
        if (request.getShift() != null) {
            staff.setShift(request.getShift());
        }
        if (request.getEmploymentStatus() != null) {
            staff.setEmploymentStatus(request.getEmploymentStatus());
        }
        if (request.getInternalPhone() != null) {
            staff.setInternalPhone(request.getInternalPhone());
        }
        if (request.getWorkAddress() != null) {
            staff.setWorkAddress(request.getWorkAddress());
        }
        if (request.getGender() != null) {
            staff.setGender(request.getGender());
        }
        if (request.getDob() != null && !request.getDob().isBlank()) {
            staff.setDob(LocalDate.parse(request.getDob()));
        }
        if (request.getAvatarUrl() != null) {
            staff.setAvatarUrl(request.getAvatarUrl());
        }
        staffRepository.save(staff);
        return getProfile(username);
    }

    public DoctorRecordResponse getRecordDetail(Long recordId) {
        MedicalRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalStateException("Record not found"));
        MetadataRecord metadata = record.getMetadata();
        SecurityLevel level = record.getSecurityLevel() != null
                ? record.getSecurityLevel()
                : SecurityLevel.CONFIDENTIAL;
        return new DoctorRecordResponse(
                record.getId(),
                record.getStatus().name(),
                record.getCreatedAt().toString(),
                record.getUpdatedAt().toString(),
                metadata != null ? metadata.getSummary() : null,
                metadata != null ? metadata.getKeywords() : null,
                level.name()
        );
    }

    public DoctorRecordDetailResponse getRecordFullDetail(Long recordId) {
        MedicalRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalStateException("Record not found"));
        MetadataRecord metadata = record.getMetadata();
        SecurityLevel level = record.getSecurityLevel() != null
                ? record.getSecurityLevel()
                : SecurityLevel.CONFIDENTIAL;
        String diagnosis = null;
        EncryptedContent encrypted = record.getEncryptedContent();
        if (encrypted != null && encrypted.getKeyId() != null) {
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
        return new DoctorRecordDetailResponse(
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

    public List<MetadataRecord> searchByPatientCode(String patientCode) {
        if (patientCode == null || !patientCode.matches("^BN-\\d+$")) {
            throw new IllegalArgumentException("Invalid patient code format");
        }
        patientRepository.findByPatientCode(patientCode)
                .orElseThrow(() -> new IllegalStateException("Patient code not found"));
        return metadataRepository.findByPatientCode(patientCode);
    }

    public com.yourname.hospital.dto.DoctorPatientDetailResponse getPatientDetail(String patientCode) {
        if (patientCode == null || !patientCode.matches("^BN-\\d+$")) {
            throw new IllegalArgumentException("Invalid patient code format");
        }
        Patient patient = patientRepository.findByPatientCode(patientCode)
                .orElseThrow(() -> new IllegalStateException("Patient code not found"));
        com.yourname.hospital.entity.PatientProfile profile = patient.getProfile();
        java.util.List<com.yourname.hospital.entity.MedicalRecord> records =
                recordRepository.findByPatient(patient);
        java.util.List<com.yourname.hospital.dto.DoctorPatientDetailResponse.RecordItem> items =
                new java.util.ArrayList<>();
        for (com.yourname.hospital.entity.MedicalRecord record : records) {
            com.yourname.hospital.entity.MetadataRecord metadata = record.getMetadata();
            items.add(new com.yourname.hospital.dto.DoctorPatientDetailResponse.RecordItem(
                    record.getId(),
                    record.getStatus().name(),
                    record.getCreatedAt().toString(),
                    record.getUpdatedAt().toString(),
                    metadata != null ? metadata.getSummary() : null,
                    metadata != null ? metadata.getKeywords() : null
            ));
        }
        return new com.yourname.hospital.dto.DoctorPatientDetailResponse(
                patient.getPatientCode(),
                patient.getUser().getFullName(),
                patient.getUser().getPhone(),
                patient.getUser().getEmail(),
                profile != null && profile.getDob() != null ? profile.getDob().toString() : null,
                profile != null ? profile.getGender() : null,
                profile != null ? profile.getAddress() : null,
                profile != null ? profile.getMedicalHistory() : null,
                items
        );
    }

    public DoctorPatientBasicResponse getPatientBasic(String patientCode) {
        if (patientCode == null || !patientCode.matches("^BN-\\d+$")) {
            throw new IllegalArgumentException("Invalid patient code format");
        }
        Patient patient = patientRepository.findByPatientCode(patientCode)
                .orElseThrow(() -> new IllegalStateException("Patient code not found"));
        PatientProfile profile = patient.getProfile();
        return new DoctorPatientBasicResponse(
                patient.getPatientCode(),
                patient.getUser().getFullName(),
                patient.getUser().getPhone(),
                patient.getUser().getEmail(),
                profile != null && profile.getDob() != null ? profile.getDob().toString() : null,
                profile != null ? profile.getGender() : null,
                profile != null ? profile.getAddress() : null
        );
    }

    public List<MetadataRecord> searchByCccd(String cccd) {
        return metadataRepository.findByCccdHash(cryptoService.sha256(cccd));
    }

    @Transactional
    public MedicalRecord createRecord(CreateRecordRequest request, Long doctorId) {
        Patient patient = patientRepository.findByPatientCode(request.getPatientCode())
                .orElseThrow(() -> new IllegalStateException("Patient not found"));

        MedicalRecord record = new MedicalRecord();
        record.setPatient(patient);
        SecurityLevel level = request.getSecurityLevel() != null
                ? request.getSecurityLevel()
                : SecurityLevel.CONFIDENTIAL;
        record.setSecurityLevel(level);

        byte[] dataKey = cryptoService.generateAesKey();
        String wrappedKey = keyService.wrapDataKey(dataKey);
        KeyEntity dataKeyEntity = keyRepository.save(new KeyEntity(KeyType.DATA, wrappedKey, null));

        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setRecord(record);
        diagnosis.setIcdCode("ICD-10");
        diagnosis.setDoctorId(doctorId);
        diagnosis.setDescriptionEnc(cryptoService.encryptText(request.getDiagnosis(), dataKey));
        record.getDiagnoses().add(diagnosis);

        MetadataRecord metadata = new MetadataRecord();
        metadata.setRecord(record);
        metadata.setPatientCode(patient.getPatientCode());
        metadata.setCccdHash(patient.getProfile().getCccdHash());
        metadata.setPatientName(patient.getUser().getFullName());
        metadata.setKeywords(request.getKeywords());
        metadata.setSummary(request.getSummary());
        record.setMetadata(metadata);

        EncryptedContent content = new EncryptedContent();
        content.setRecord(record);
        String encrypted = cryptoService.encryptText(request.getDiagnosis(), dataKey);
        content.setEncryptedData(encrypted);
        content.setAlgorithm("AES-GCM");
        content.setKeyId(dataKeyEntity.getId());
        content.setIntegrityHash(cryptoService.hmacSha256(encrypted, dataKey));
        record.setEncryptedContent(content);

        MedicalRecord saved = recordRepository.save(record);
        searchIndexService.indexMetadata(saved.getMetadata());
        return saved;
    }

    @Transactional
    public MedicalRecord updateRecord(Long recordId, UpdateRecordRequest request) {
        MedicalRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalStateException("Record not found"));
        record.setUpdatedAt(LocalDateTime.now());
        if (request.getStatus() != null) {
            record.setStatus(request.getStatus());
        }
        if (request.getSecurityLevel() != null) {
            record.setSecurityLevel(request.getSecurityLevel());
        }

        byte[] dataKey = cryptoService.generateAesKey();
        String wrappedKey = keyService.wrapDataKey(dataKey);
        KeyEntity dataKeyEntity = keyRepository.save(new KeyEntity(KeyType.DATA, wrappedKey, null));

        if (request.getDiagnosis() != null && !record.getDiagnoses().isEmpty()) {
            Diagnosis diagnosis = record.getDiagnoses().get(0);
            diagnosis.setDescriptionEnc(cryptoService.encryptText(request.getDiagnosis(), dataKey));
        }

        if (record.getMetadata() != null) {
            if (request.getKeywords() != null) {
                record.getMetadata().setKeywords(request.getKeywords());
            }
            if (request.getSummary() != null) {
                record.getMetadata().setSummary(request.getSummary());
            }
        }

        if (record.getEncryptedContent() != null && request.getDiagnosis() != null) {
            String encrypted = cryptoService.encryptText(request.getDiagnosis(), dataKey);
            record.getEncryptedContent().setEncryptedData(encrypted);
            record.getEncryptedContent().setKeyId(dataKeyEntity.getId());
            record.getEncryptedContent().setIntegrityHash(cryptoService.hmacSha256(encrypted, dataKey));
        }

        if (record.getMetadata() != null) {
            record.getMetadata().setUpdatedAt(LocalDateTime.now());
            searchIndexService.indexMetadata(record.getMetadata());
        }
        return record;
    }

    @Transactional
    public void deleteRecord(Long recordId) {
        recordRepository.deleteById(recordId);
        searchIndexService.deleteMetadata(recordId);
    }
}
