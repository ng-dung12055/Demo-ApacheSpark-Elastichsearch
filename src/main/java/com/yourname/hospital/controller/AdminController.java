package com.yourname.hospital.controller;

import com.yourname.hospital.dto.AdminUserResponse;
import com.yourname.hospital.dto.AuditLogResponse;
import com.yourname.hospital.dto.CreateUserRequest;
import com.yourname.hospital.dto.MasterKeyBackupResponse;
import com.yourname.hospital.dto.MasterKeyRequest;
import com.yourname.hospital.dto.MasterKeySummaryResponse;
import com.yourname.hospital.dto.RoleResponse;
import com.yourname.hospital.dto.SearchReindexResponse;
import com.yourname.hospital.dto.SearchReindexStatusResponse;
import com.yourname.hospital.dto.UpdateUserRoleRequest;
import com.yourname.hospital.entity.KeyEntity;
import com.yourname.hospital.entity.User;
import com.yourname.hospital.service.AdminService;
import com.yourname.hospital.service.AuditLogService;
import com.yourname.hospital.service.KeyService;
import com.yourname.hospital.service.SearchIndexService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final KeyService keyService;
    private final AuditLogService auditLogService;
    private final SearchIndexService searchIndexService;

    public AdminController(
            AdminService adminService,
            KeyService keyService,
            AuditLogService auditLogService,
            SearchIndexService searchIndexService) {
        this.adminService = adminService;
        this.keyService = keyService;
        this.auditLogService = auditLogService;
        this.searchIndexService = searchIndexService;
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public User createUser(@Valid @RequestBody CreateUserRequest request) {
        try {
            return adminService.createUser(request);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminUserResponse> listUsers() {
        return adminService.listUsers();
    }

    @PutMapping("/users/{accountId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public void updateRole(
            @PathVariable Long accountId,
            @Valid @RequestBody UpdateUserRoleRequest request) {
        try {
            adminService.updateUserRole(accountId, request);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (java.util.NoSuchElementException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        }
    }

    @PutMapping("/users/{accountId}/lock")
    @PreAuthorize("hasRole('ADMIN')")
    public void lockUser(@PathVariable Long accountId, @RequestParam boolean active) {
        try {
            adminService.lockAccount(accountId, active);
        } catch (java.util.NoSuchElementException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        }
    }

    @DeleteMapping("/users/{accountId}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(@PathVariable Long accountId) {
        try {
            adminService.deleteAccount(accountId);
        } catch (java.util.NoSuchElementException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        }
    }

    @PostMapping("/master-key")
    @PreAuthorize("hasRole('ADMIN')")
    public KeyEntity createMasterKey(@RequestBody MasterKeyRequest request) {
        return keyService.createMasterKey(request);
    }

    @GetMapping("/master-key")
    @PreAuthorize("hasRole('ADMIN')")
    public List<MasterKeySummaryResponse> listMasterKeys() {
        return keyService.listMasterKeys().stream()
                .map(this::toSummary)
                .toList();
    }

    @GetMapping("/master-key/backup")
    @PreAuthorize("hasRole('ADMIN')")
    public List<MasterKeyBackupResponse> backupMasterKeys() {
        return keyService.listMasterKeys().stream()
                .map(this::toBackup)
                .toList();
    }

    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public List<RoleResponse> listRoles() {
        return adminService.listRoles();
    }

    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AuditLogResponse> listAuditLogs(@RequestParam(defaultValue = "50") int limit) {
        return auditLogService.listRecent(limit);
    }

    @PostMapping("/search/reindex")
    @PreAuthorize("hasRole('ADMIN')")
    public SearchReindexResponse reindexSearch() {
        return searchIndexService.reindexAll();
    }

    @GetMapping("/search/reindex/status")
    @PreAuthorize("hasRole('ADMIN')")
    public SearchReindexStatusResponse reindexSearchStatus() {
        return searchIndexService.getReindexStatus();
    }

    private MasterKeySummaryResponse toSummary(KeyEntity key) {
        String vaultLocation = key.getVault() != null ? key.getVault().getStorageLocation() : null;
        return new MasterKeySummaryResponse(
                key.getId(),
                key.getType().name(),
                key.getStatus().name(),
                key.getCreatedAt().toString(),
                vaultLocation);
    }

    private MasterKeyBackupResponse toBackup(KeyEntity key) {
        String vaultLocation = key.getVault() != null ? key.getVault().getStorageLocation() : null;
        return new MasterKeyBackupResponse(
                key.getId(),
                key.getType().name(),
                key.getStatus().name(),
                key.getCreatedAt().toString(),
                vaultLocation,
                key.getKeyValueEnc());
    }
}
