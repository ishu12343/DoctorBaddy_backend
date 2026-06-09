package com.healthcare.service.impl;

import com.healthcare.dto.ForgotPasswordRequest;
import com.healthcare.model.Admin;
import com.healthcare.model.Doctor;
import com.healthcare.model.Patient;
import com.healthcare.repository.AdminRepository;
import com.healthcare.repository.DoctorRepository;
import com.healthcare.repository.PatientRepository;
import com.healthcare.security.JwtUtil;
import com.healthcare.service.AdminService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {
    
    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Map<String, Object> createAdmin(Admin admin) {
        if (adminRepository.existsByEmail(admin.getEmail())) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Admin with this email already exists");
            return response;
        }
        
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        admin.setIsActive(true);
        
        Admin savedAdmin = adminRepository.save(admin);
        
        String token = jwtUtil.generateToken(savedAdmin.getId().toString(), "ADMIN");
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Admin created successfully");
        response.put("token", token);
        Map<String, Object> adminData = new HashMap<>();
        adminData.put("id", savedAdmin.getId());
        adminData.put("full_name", savedAdmin.getFullName());
        adminData.put("username", savedAdmin.getFullName());
        adminData.put("email", savedAdmin.getEmail());
        adminData.put("mobile", savedAdmin.getMobile());
        adminData.put("role", savedAdmin.getRole());
        adminData.put("profile_photo", savedAdmin.getProfilePhoto());
        response.put("admin", adminData);
        
        return response;
    }

    @Override
    public Map<String, Object> login(String identifier, String password) {
        Pattern emailPattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
        Pattern mobilePattern = Pattern.compile("^[0-9]{10}$");
        
        Admin admin;
        if (emailPattern.matcher(identifier).matches()) {
            admin = adminRepository.findByEmail(identifier)
                .orElseThrow(() -> new RuntimeException("Invalid credentials or inactive account"));
        } else if (mobilePattern.matcher(identifier).matches()) {
            admin = adminRepository.findByMobile(identifier)
                .orElseThrow(() -> new RuntimeException("Invalid credentials or inactive account"));
        } else {
            throw new RuntimeException("Please enter a valid email address or 10-digit mobile number");
        }
        
        if (!admin.getIsActive()) {
            throw new RuntimeException("Invalid credentials or inactive account");
        }
        
        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new RuntimeException("Invalid credentials or inactive account");
        }
        
        admin.setLastLogin(LocalDateTime.now());
        adminRepository.save(admin);
        
        String token = jwtUtil.generateToken(admin.getId().toString(), "ADMIN");
        
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        Map<String, Object> adminData = new HashMap<>();
        adminData.put("id", admin.getId());
        adminData.put("full_name", admin.getFullName());
        adminData.put("username", admin.getFullName());
        adminData.put("email", admin.getEmail());
        adminData.put("mobile", admin.getMobile());
        adminData.put("role", admin.getRole());
        adminData.put("profile_photo", admin.getProfilePhoto());
        response.put("admin", adminData);
        
        return response;
    }

    @Override
    public Map<String, Object> sendOtp(String identifier) {
        Pattern emailPattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
        Pattern mobilePattern = Pattern.compile("^[0-9]{10}$");
        
        Admin admin;
        String identifierType;
        
        if (emailPattern.matcher(identifier).matches()) {
            admin = adminRepository.findByEmail(identifier)
                .orElseThrow(() -> new RuntimeException("No admin account found with this email"));
            identifierType = "email";
        } else if (mobilePattern.matcher(identifier).matches()) {
            admin = adminRepository.findByMobile(identifier)
                .orElseThrow(() -> new RuntimeException("No admin account found with this mobile"));
            identifierType = "mobile";
        } else {
            throw new RuntimeException("Please enter a valid email address or 10-digit mobile number");
        }
        
        if (!admin.getIsActive()) {
            throw new RuntimeException("Admin account is inactive");
        }
        
        String otp = String.format("%06d", new Random().nextInt(1000000));
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(10);
        
        admin.setResetOtp(otp);
        admin.setOtpExpiresAt(expiryTime);
        adminRepository.save(admin);
        
        System.out.println("OTP for admin " + admin.getFullName() + ": " + otp);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "OTP sent to your registered " + identifierType);
        response.put("identifier_type", identifierType);
        response.put("otp", otp);
        
        return response;
    }

    @Override
    public Map<String, Object> resetPassword(ForgotPasswordRequest request) {
        Pattern emailPattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
        
        Admin admin;
        if (emailPattern.matcher(request.getIdentifier()).matches()) {
            admin = adminRepository.findByEmail(request.getIdentifier())
                .orElseThrow(() -> new RuntimeException("No admin account found with this identifier"));
        } else {
            admin = adminRepository.findByMobile(request.getIdentifier())
                .orElseThrow(() -> new RuntimeException("No admin account found with this identifier"));
        }
        
        if (admin.getResetOtp() == null || admin.getOtpExpiresAt() == null) {
            throw new RuntimeException("No OTP found. Please request a new OTP.");
        }
        
        if (LocalDateTime.now().isAfter(admin.getOtpExpiresAt())) {
            admin.setResetOtp(null);
            admin.setOtpExpiresAt(null);
            adminRepository.save(admin);
            throw new RuntimeException("OTP has expired. Please request a new one.");
        }
        
        if (!admin.getResetOtp().equals(request.getOtp())) {
            throw new RuntimeException("Invalid OTP. Please check and try again.");
        }
        
        admin.setPassword(passwordEncoder.encode(request.getNewPassword()));
        admin.setResetOtp(null);
        admin.setOtpExpiresAt(null);
        adminRepository.save(admin);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Password reset successfully. You can now login with your new password.");
        
        return response;
    }

    @Override
    public Map<String, Object> getProfile(Long adminId) {
        Admin admin = adminRepository.findByIdAndIsActiveTrue(adminId)
            .orElseThrow(() -> new RuntimeException("Admin not found"));
        
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", admin.getId());
        profile.put("full_name", admin.getFullName());
        profile.put("email", admin.getEmail());
        profile.put("mobile", admin.getMobile());
        profile.put("role", admin.getRole());
        profile.put("profile_photo", admin.getProfilePhoto());
        profile.put("created_at", admin.getCreatedAt());
        profile.put("last_login", admin.getLastLogin());
        
        return profile;
    }

    @Override
    public Map<String, Object> updateProfile(Long adminId, Map<String, Object> updates) {
        Admin admin = adminRepository.findByIdAndIsActiveTrue(adminId)
            .orElseThrow(() -> new RuntimeException("Admin not found or no changes made"));
        
        if (updates.containsKey("full_name")) admin.setFullName((String) updates.get("full_name"));
        if (updates.containsKey("email")) admin.setEmail((String) updates.get("email"));
        if (updates.containsKey("mobile")) admin.setMobile((String) updates.get("mobile"));
        if (updates.containsKey("profile_photo")) admin.setProfilePhoto((String) updates.get("profile_photo"));
        
        adminRepository.save(admin);
        
        Admin updatedAdmin = adminRepository.findByIdAndIsActiveTrue(adminId).orElse(null);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Admin profile updated successfully");
        response.put("admin", updatedAdmin);
        
        return response;
    }

    @Override
    public List<Map<String, Object>> listDoctors() {
        List<Doctor> doctors = doctorRepository.findAll();
        
        return doctors.stream().map(doctor -> {
            Map<String, Object> doctorData = new HashMap<>();
            doctorData.put("id", doctor.getId());
            doctorData.put("full_name", doctor.getFullName());
            doctorData.put("email", doctor.getEmail());
            doctorData.put("approved", doctor.getApproved());
            doctorData.put("suspended", doctor.getSuspended());
            doctorData.put("specialty", doctor.getSpecialty());
            doctorData.put("documents_verified", false);
            return doctorData;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> viewDoctor(Long id) {
        Doctor doctor = doctorRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Doctor not found"));
        
        Map<String, Object> doctorData = new HashMap<>();
        doctorData.put("id", doctor.getId());
        doctorData.put("full_name", doctor.getFullName());
        doctorData.put("email", doctor.getEmail());
        doctorData.put("mobile", doctor.getMobile());
        doctorData.put("location", doctor.getLocation());
        doctorData.put("registration_number", doctor.getRegistrationNumber());
        doctorData.put("council", doctor.getCouncil());
        doctorData.put("degree", doctor.getDegree());
        doctorData.put("specialty", doctor.getSpecialty());
        doctorData.put("experience", doctor.getExperience());
        doctorData.put("clinic_name", doctor.getClinicName());
        doctorData.put("clinic_address", doctor.getClinicAddress());
        doctorData.put("role", doctor.getRole());
        doctorData.put("approved", doctor.getApproved());
        doctorData.put("suspended", doctor.getSuspended());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", doctorData);
        
        return response;
    }

    @Override
    public Map<String, Object> approveDoctor(Long docId) {
        Doctor doctor = doctorRepository.findById(docId)
            .orElseThrow(() -> new RuntimeException("Doctor not found"));
        
        doctor.setApproved(true);
        doctor.setSuspended(false);
        doctor.setStatus("ACTIVE");
        doctorRepository.save(doctor);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Doctor approved");
        
        return response;
    }

    @Override
    public Map<String, Object> rejectDoctor(Long docId) {
        Doctor doctor = doctorRepository.findById(docId)
            .orElseThrow(() -> new RuntimeException("Doctor not found"));
        
        doctor.setApproved(false);
        doctor.setSuspended(false);
        doctor.setStatus("INACTIVE");
        doctorRepository.save(doctor);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Doctor rejected");
        
        return response;
    }

    @Override
    public Map<String, Object> suspendDoctor(Long docId) {
        Doctor doctor = doctorRepository.findById(docId)
            .orElseThrow(() -> new RuntimeException("Doctor not found"));
        
        doctor.setSuspended(true);
        doctor.setStatus("SUSPENDED");
        doctorRepository.save(doctor);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Doctor suspended successfully");
        
        return response;
    }

    @Override
    public Map<String, Object> unsuspendDoctor(Long docId) {
        Doctor doctor = doctorRepository.findById(docId)
            .orElseThrow(() -> new RuntimeException("Doctor not found"));
        
        doctor.setSuspended(false);
        doctor.setStatus(doctor.getApproved() ? "ACTIVE" : "INACTIVE");
        doctorRepository.save(doctor);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Doctor unsuspended successfully");
        
        return response;
    }

    @Override
    public Map<String, Object> deleteDoctor(Long docId) {
        Doctor doctor = doctorRepository.findById(docId)
            .orElseThrow(() -> new RuntimeException("Doctor not found"));
        
        doctorRepository.delete(doctor);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Doctor deleted successfully");
        
        return response;
    }

    @Override
    public List<Map<String, Object>> listPatients() {
        List<Patient> patients = patientRepository.findAll();
        
        return patients.stream().map(patient -> {
            Map<String, Object> patientData = new HashMap<>();
            patientData.put("id", patient.getId());
            patientData.put("full_name", patient.getFullName());
            patientData.put("email", patient.getEmail());
            patientData.put("mobile", patient.getMobile());
            patientData.put("role", patient.getRole());
            patientData.put("gender", patient.getGender());
            patientData.put("is_active", patient.getIsActive());
            return patientData;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> viewPatient(Long id) {
        Patient patient = patientRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Patient not found"));
        
        Map<String, Object> patientData = new HashMap<>();
        patientData.put("id", patient.getId());
        patientData.put("full_name", patient.getFullName());
        patientData.put("email", patient.getEmail());
        patientData.put("mobile", patient.getMobile());
        patientData.put("date_of_birth", patient.getDateOfBirth());
        patientData.put("gender", patient.getGender());
        patientData.put("blood_group", patient.getBloodGroup());
        patientData.put("address", patient.getAddress());
        patientData.put("emergency_contact_number", patient.getEmergencyContactNumber());
        patientData.put("role", patient.getRole());
        patientData.put("is_active", patient.getIsActive());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", patientData);
        
        return response;
    }

    @Override
    public Map<String, Object> activatePatient(Long patId) {
        Patient patient = patientRepository.findById(patId)
            .orElseThrow(() -> new RuntimeException("Patient not found"));
        
        patient.setIsActive(true);
        patientRepository.save(patient);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Patient activated");
        
        return response;
    }

    @Override
    public Map<String, Object> deactivatePatient(Long patId) {
        Patient patient = patientRepository.findById(patId)
            .orElseThrow(() -> new RuntimeException("Patient not found"));
        
        patient.setIsActive(false);
        patientRepository.save(patient);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Patient deactivated");
        
        return response;
    }

    @Override
    public Map<String, Object> deletePatient(Long patId) {
        Patient patient = patientRepository.findById(patId)
            .orElseThrow(() -> new RuntimeException("Patient not found"));
        
        patientRepository.delete(patient);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Patient deleted successfully");
        
        return response;
    }
}
