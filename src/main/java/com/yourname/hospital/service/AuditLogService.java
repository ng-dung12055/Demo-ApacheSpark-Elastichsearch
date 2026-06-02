package com.yourname.hospital.service;

import com.yourname.hospital.dto.AuditLogResponse;
import com.yourname.hospital.entity.AuditLog;
import com.yourname.hospital.repository.AuditLogRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void record(
            String username,
            String role,
            String method,
            String path,
            int statusCode,
            String clientIp,
            String userAgent) {
        AuditLog log = new AuditLog(
                username,
                role,
                method,
                path,
                statusCode,
                clientIp,
                userAgent);
        auditLogRepository.save(log);
    }

    public List<AuditLogResponse> listRecent(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 200));
        PageRequest page = PageRequest.of(0, safeLimit, Sort.by("createdAt").descending());
        return auditLogRepository.findAll(page).stream()
                .map(log -> new AuditLogResponse(
                        log.getId(),
                        log.getUsername(),
                        log.getRole(),
                        log.getMethod(),
                        log.getPath(),
                        log.getStatusCode(),
                        log.getClientIp(),
                        log.getUserAgent(),
                        log.getCreatedAt().toString()))
                .toList();
    }
}
