package com.yourname.hospital.dto;

public class AuditLogResponse {

    private Long id;
    private String username;
    private String role;
    private String method;
    private String path;
    private int statusCode;
    private String clientIp;
    private String userAgent;
    private String createdAt;

    public AuditLogResponse(
            Long id,
            String username,
            String role,
            String method,
            String path,
            int statusCode,
            String clientIp,
            String userAgent,
            String createdAt) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.method = method;
        this.path = path;
        this.statusCode = statusCode;
        this.clientIp = clientIp;
        this.userAgent = userAgent;
        this.createdAt = createdAt;
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

    public String getCreatedAt() {
        return createdAt;
    }
}
