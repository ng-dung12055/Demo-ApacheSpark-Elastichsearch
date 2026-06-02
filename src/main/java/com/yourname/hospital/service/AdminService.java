package com.yourname.hospital.service;

import com.yourname.hospital.dto.AdminUserResponse;
import com.yourname.hospital.dto.CreateUserRequest;
import com.yourname.hospital.dto.RoleResponse;
import com.yourname.hospital.dto.UpdateUserRoleRequest;
import com.yourname.hospital.entity.Account;
import com.yourname.hospital.entity.Role;
import com.yourname.hospital.entity.User;
import com.yourname.hospital.repository.AccountRepository;
import com.yourname.hospital.repository.PatientRepository;
import com.yourname.hospital.repository.RoleRepository;
import com.yourname.hospital.repository.StaffRepository;
import com.yourname.hospital.repository.UserRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PatientRepository patientRepository;
    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(
            UserRepository userRepository,
            AccountRepository accountRepository,
            RoleRepository roleRepository,
            PatientRepository patientRepository,
            StaffRepository staffRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
        this.patientRepository = patientRepository;
        this.staffRepository = staffRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createUser(CreateUserRequest request) {
        if (accountRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalStateException("Username already exists");
        }
        if ("NURSE".equalsIgnoreCase(request.getRole())) {
            throw new IllegalArgumentException("Role not allowed");
        }
        User user = new User(request.getFullName(), request.getEmail(), request.getPhone());
        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));
        user.getRoles().add(role);
        userRepository.save(user);

        Account account = new Account(request.getUsername(), passwordEncoder.encode(request.getPassword()), user);
        accountRepository.save(account);
        return user;
    }

    public List<AdminUserResponse> listUsers() {
        List<Account> accounts = accountRepository.findAll();
        List<AdminUserResponse> results = new ArrayList<>();
        for (Account account : accounts) {
            User user = account.getUser();
            List<String> roles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());
            results.add(new AdminUserResponse(
                    account.getId(),
                    account.getUsername(),
                    user.getFullName(),
                    roles,
                    account.isActive(),
                    user.getEmail(),
                    user.getPhone()));
        }
        return results;
    }

    @Transactional
    public void updateUserRole(Long accountId, UpdateUserRoleRequest request) {
        Account account = accountRepository.findById(accountId).orElseThrow();
        if ("NURSE".equalsIgnoreCase(request.getRole())) {
            throw new IllegalArgumentException("Role not allowed");
        }
        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        account.getUser().setRoles(roles);
    }

    public List<RoleResponse> listRoles() {
        return roleRepository.findAll().stream()
                .filter(role -> !"NURSE".equalsIgnoreCase(role.getName()))
                .map(role -> new RoleResponse(role.getName(), role.getDescription()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void lockAccount(Long accountId, boolean active) {
        Account account = accountRepository.findById(accountId).orElseThrow();
        account.setActive(active);
    }

    @Transactional
    public void deleteAccount(Long accountId) {
        Account account = accountRepository.findById(accountId).orElseThrow();
        User user = account.getUser();
        if (user != null) {
            Long userId = user.getId();
            patientRepository.findByUserId(userId).ifPresent(patientRepository::delete);
            staffRepository.findByUserId(userId).ifPresent(staffRepository::delete);
        }
        accountRepository.delete(account);
        if (user != null) {
            userRepository.delete(user);
        }
    }
}
