package com.yourname.hospital.config;

import com.yourname.hospital.dto.MasterKeyRequest;
import com.yourname.hospital.entity.Permission;
import com.yourname.hospital.entity.Role;
import com.yourname.hospital.entity.User;
import com.yourname.hospital.repository.PermissionRepository;
import com.yourname.hospital.repository.RoleRepository;
import com.yourname.hospital.repository.UserRepository;
import com.yourname.hospital.service.AdminService;
import com.yourname.hospital.service.KeyService;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(
            PermissionRepository permissionRepository,
            RoleRepository roleRepository,
            UserRepository userRepository,
            com.yourname.hospital.repository.PatientRepository patientRepository,
            com.yourname.hospital.repository.PatientProfileRepository profileRepository,
            com.yourname.hospital.repository.StaffRepository staffRepository,
            AdminService adminService,
            KeyService keyService,
            com.yourname.hospital.service.CryptoService cryptoService) {
        return args -> {
            if (keyService.listMasterKeys().isEmpty()) {
                MasterKeyRequest request = new MasterKeyRequest();
                request.setStorageLocation("demo-vault");
                keyService.createMasterKey(request);
            }

            roleRepository.findByName("NURSE").ifPresent(role -> {
                List<User> users = userRepository.findAll();
                for (User user : users) {
                    boolean removed = user.getRoles().removeIf(r -> "NURSE".equals(r.getName()));
                    if (removed) {
                        userRepository.save(user);
                    }
                }
                roleRepository.delete(role);
            });

            if (roleRepository.count() == 0) {
                Permission p1 = new Permission("RECORD_READ", "Read medical record metadata");
                Permission p2 = new Permission("RECORD_WRITE", "Create/update medical record");
                Permission p3 = new Permission("USER_ADMIN", "Manage users and roles");
                permissionRepository.saveAll(List.of(p1, p2, p3));

                Role admin = new Role("ADMIN", "System administrator");
                admin.getPermissions().addAll(List.of(p1, p2, p3));
                roleRepository.save(admin);

                Role doctor = new Role("DOCTOR", "Medical staff");
                doctor.getPermissions().addAll(List.of(p1, p2));
                roleRepository.save(doctor);

                Role patient = new Role("PATIENT", "Patient");
                patient.getPermissions().add(p1);
                roleRepository.save(patient);
            }

            if (userRepository.count() == 0) {
                adminService.createUser(TestData.adminUser());
                User doctorUser = adminService.createUser(TestData.doctorUser());
                com.yourname.hospital.entity.Staff staff = new com.yourname.hospital.entity.Staff(
                        "BS-001", "Internal Medicine", "Attending Doctor", doctorUser);
                staff.setSpecialty("Cardiology");
                staff.setLicenseNumber("CC-2026-001");
                staff.setFacility("City General Hospital");
                staff.setShift("Morning");
                staff.setEmploymentStatus("Active");
                staff.setInternalPhone("Ext-102");
                staff.setWorkAddress("Ward A, Floor 3");
                staff.setGender("Male");
                staffRepository.save(staff);
                User patientUser = adminService.createUser(TestData.patientUser());
                com.yourname.hospital.entity.Patient patient = new com.yourname.hospital.entity.Patient(
                        "BN-" + patientUser.getId(), patientUser);
                patientRepository.save(patient);
                com.yourname.hospital.entity.PatientProfile profile = new com.yourname.hospital.entity.PatientProfile();
                profile.setPatient(patient);
                profile.setCccdHash(cryptoService.sha256("012345678901"));
                profile.setCccdEnc(cryptoService.encryptText("012345678901", keyService.getActiveMasterKey()));
                profile.setAddress("Demo Address");
                profileRepository.save(profile);
                patient.setProfile(profile);
            }
        };
    }
}

class TestData {

    static com.yourname.hospital.dto.CreateUserRequest adminUser() {
        com.yourname.hospital.dto.CreateUserRequest req = new com.yourname.hospital.dto.CreateUserRequest();
        req.setUsername("admin");
        req.setPassword("Admin@123");
        req.setFullName("Admin Demo");
        req.setRole("ADMIN");
        req.setEmail("admin@demo.local");
        return req;
    }

    static com.yourname.hospital.dto.CreateUserRequest doctorUser() {
        com.yourname.hospital.dto.CreateUserRequest req = new com.yourname.hospital.dto.CreateUserRequest();
        req.setUsername("doctor");
        req.setPassword("Doctor@123");
        req.setFullName("Bac Si Demo");
        req.setRole("DOCTOR");
        req.setEmail("doctor@demo.local");
        return req;
    }

    static com.yourname.hospital.dto.CreateUserRequest patientUser() {
        com.yourname.hospital.dto.CreateUserRequest req = new com.yourname.hospital.dto.CreateUserRequest();
        req.setUsername("patient");
        req.setPassword("Patient@123");
        req.setFullName("Benh Nhan Demo");
        req.setRole("PATIENT");
        req.setEmail("patient@demo.local");
        return req;
    }
}
