package com.yourname.hospital.security;

import com.yourname.hospital.service.AuditLogService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AuditLogFilter extends OncePerRequestFilter {

    private final AuditLogService auditLogService;

    public AuditLogFilter(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path == null || !path.startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } finally {
            try {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String username = auth != null && auth.isAuthenticated() ? auth.getName() : "anonymous";
                String role = auth != null && auth.getAuthorities() != null
                        ? auth.getAuthorities().stream()
                            .map(granted -> granted.getAuthority())
                            .collect(Collectors.joining(","))
                        : "anonymous";
                auditLogService.record(
                        username,
                        role,
                        request.getMethod(),
                        request.getRequestURI(),
                        response.getStatus(),
                        resolveClientIp(request),
                        request.getHeader("User-Agent"));
            } catch (Exception ignored) {
                // Avoid breaking the request if audit logging fails.
            }
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
