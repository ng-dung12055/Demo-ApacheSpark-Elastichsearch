package com.yourname.hospital.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String role;
    private String method;
    private String path;
    private int statusCode;
    private String clientIp;
    private String userAgent;

    private LocalDateTime createdAt = LocalDateTime.now();

    public AuditLog() {
    }

    public AuditLog(
            String username,
            String role,
            String method,
            String path,
            int statusCode,
            String clientIp,
            String userAgent) {
        this.username = username;
        this.role = role;
        this.method = method;
        this.path = path;
        this.statusCode = statusCode;
        this.clientIp = clientIp;
        this.userAgent = userAgent;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getClientIp() {
        return clientIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
