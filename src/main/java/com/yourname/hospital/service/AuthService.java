package com.yourname.hospital.service;

import com.yourname.hospital.dto.AuthResponse;
import com.yourname.hospital.dto.LoginRequest;
import com.yourname.hospital.dto.RegisterRequest;
import com.yourname.hospital.entity.*;
import com.yourname.hospital.repository.*;
import com.yourname.hospital.security.JwtService;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PatientRepository patientRepository;
    private final PatientProfileRepository profileRepository;
    private final CryptoService cryptoService;
    private final KeyService keyService;

    public AuthService(
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            UserRepository userRepository,
            AccountRepository accountRepository,
            RoleRepository roleRepository,
            PatientRepository patientRepository,
            PatientProfileRepository profileRepository,
            CryptoService cryptoService,
            KeyService keyService) {
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
        this.patientRepository = patientRepository;
        this.profileRepository = profileRepository;
        this.cryptoService = cryptoService;
        this.keyService = keyService;
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (AuthenticationException ex) {
            Account account = accountRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED, "Invalid credentials"));
            if (!account.isActive()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account locked");
            }
            if (!passwordEncoder.matches(request.getPassword(), account.getPasswordHash())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
            }
        }
        String token = jwtService.generateToken(request.getUsername());
        Account account = accountRepository.findByUsername(request.getUsername()).orElseThrow();
        String roleName = account.getUser().getRoles().stream().findFirst().map(Role::getName).orElse("UNKNOWN");
        if ("PATIENT".equals(roleName)) {
            Patient patient = patientRepository.findByUserId(account.getUser().getId()).orElse(null);
            String patientCode = patient != null ? patient.getPatientCode() : null;
            return new AuthResponse(token, roleName, patientCode);
        }
        return new AuthResponse(token, roleName);
    }

    @Transactional
    public AuthResponse registerPatient(RegisterRequest request) {
        if (accountRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalStateException("Username already exists");
        }
        User user = new User(request.getFullName(), request.getEmail(), request.getPhone());
        Role patientRole = roleRepository.findByName("PATIENT").orElseThrow();
        user.getRoles().add(patientRole);
        userRepository.save(user);

        Account account = new Account(request.getUsername(), passwordEncoder.encode(request.getPassword()), user);
        accountRepository.save(account);

        Patient patient = new Patient("BN-" + user.getId(), user);
        patientRepository.save(patient);

        PatientProfile profile = new PatientProfile();
        profile.setPatient(patient);
        profile.setDob(request.getDob() == null ? null : LocalDate.parse(request.getDob()));
        profile.setGender(request.getGender());
        profile.setAddress(request.getAddress());
        profile.setCccdHash(cryptoService.sha256(request.getCccd()));
        profile.setCccdEnc(cryptoService.encryptText(request.getCccd(), keyService.getActiveMasterKey()));
        profileRepository.save(profile);
        patient.setProfile(profile);

        String token = jwtService.generateToken(account.getUsername());
        return new AuthResponse(token, "PATIENT", patient.getPatientCode());
    }
}
