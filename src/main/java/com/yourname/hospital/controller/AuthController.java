package com.yourname.hospital.controller;

import com.yourname.hospital.dto.AuthResponse;
import com.yourname.hospital.dto.LoginRequest;
import com.yourname.hospital.dto.RegisterRequest;
import com.yourname.hospital.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.registerPatient(request);
    }
}
