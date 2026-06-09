package com.healthcare.controller;

import com.healthcare.dto.ApiResponse;
import com.healthcare.dto.AppointmentRequest;
import com.healthcare.dto.ForgotPasswordRequest;
import com.healthcare.dto.LoginRequest;
import com.healthcare.model.Patient;
import com.healthcare.service.PatientService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/patient")
public class PatientController {
    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    // ===== FORGOT PASSWORD ENDPOINTS =====
    
    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> request) {
        try {
            String identifier = request.get("identifier");
            return ResponseEntity.ok(patientService.sendOtp(identifier));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("Failed to send OTP", e.getMessage()));
        }
    }
    
    @PostMapping("/forgot-password/reset")
    public ResponseEntity<?> resetPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            return ResponseEntity.ok(patientService.resetPassword(request));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("Failed to reset password", e.getMessage()));
        }
    }

    // ===== REGISTER =====
    
    @PostMapping("/register")
    public ResponseEntity<?> registerPatient(@RequestBody Patient patient) {
        try {
            return ResponseEntity.ok(patientService.registerPatient(patient));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Registration failed: " + e.getMessage()));
        }
    }

    // ===== LOGIN =====
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            String identifier = loginRequest.getEmail() != null ? loginRequest.getEmail() : loginRequest.getIdentifier();
            String password = loginRequest.getPassword();
            
            if (identifier == null || identifier.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Email/mobile and password required"));
            }
            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Email/mobile and password required"));
            }
            
            return ResponseEntity.ok(patientService.login(identifier, password));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Collections.singletonMap("error", "Invalid email/mobile or password"));
        }
    }
    
    // ===== LOGOUT =====
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        return ResponseEntity.ok(Collections.singletonMap("message", "Patient logged out successfully. Token revoked."));
    }

    // ===== PROFILE =====
    
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        try {
            Long patientId = getUserIdFromToken(request);
            return ResponseEntity.ok(patientService.getProfile(patientId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Failed to fetch profile: " + e.getMessage()));
        }
    }
    
    @PutMapping("/updateprofile")
    public ResponseEntity<?> updateProfile(@RequestParam Map<String, String> updates, 
                                           @RequestParam(required = false) MultipartFile photoPath,
                                           @RequestParam(required = false) MultipartFile documentPath,
                                           HttpServletRequest request) {
        try {
            Long patientId = getUserIdFromToken(request);
            Map<String, Object> updateMap = new HashMap<>(updates);
            
            // Handle file uploads if present
            if (photoPath != null && !photoPath.isEmpty()) {
                updateMap.put("photo_path", photoPath.getOriginalFilename());
            }
            if (documentPath != null && !documentPath.isEmpty()) {
                updateMap.put("document_path", documentPath.getOriginalFilename());
            }
            
            return ResponseEntity.ok(patientService.updateProfile(patientId, updateMap));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Profile update failed: " + e.getMessage()));
        }
    }

    // ===== DOCTORS LIST =====
    
    @GetMapping("/doctors")
    public ResponseEntity<?> listDoctors(
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long doctor_id) {
        try {
            return ResponseEntity.ok(patientService.listDoctors(specialty, city, search, doctor_id));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Failed to fetch doctors: " + e.getMessage()));
        }
    }

    // ===== APPOINTMENT MANAGEMENT =====
    
    @PostMapping("/appointments/book")
    public ResponseEntity<?> bookAppointment(@RequestBody AppointmentRequest request, HttpServletRequest httpRequest) {
        try {
            Long patientId = getUserIdFromToken(httpRequest);
            return ResponseEntity.ok(patientService.bookAppointment(patientId, request));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Failed to book appointment: " + e.getMessage()));
        }
    }
    
    @GetMapping("/appointments")
    public ResponseEntity<?> getAppointments(HttpServletRequest request) {
        try {
            Long patientId = getUserIdFromToken(request);
            return ResponseEntity.ok(patientService.getAppointments(patientId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Failed to fetch appointments: " + e.getMessage()));
        }
    }
    
    @PutMapping("/appointments/{appointmentId}/cancel")
    public ResponseEntity<?> cancelAppointment(@PathVariable Long appointmentId, HttpServletRequest request) {
        try {
            Long patientId = getUserIdFromToken(request);
            return ResponseEntity.ok(patientService.cancelAppointment(appointmentId, patientId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Failed to cancel appointment: " + e.getMessage()));
        }
    }
    
    @PostMapping("/appointments/{appointmentId}/reschedule")
    public ResponseEntity<?> rescheduleAppointment(@PathVariable Long appointmentId, @RequestBody AppointmentRequest request, HttpServletRequest httpRequest) {
        try {
            Long patientId = getUserIdFromToken(httpRequest);
            return ResponseEntity.ok(patientService.rescheduleAppointment(appointmentId, patientId, request));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Failed to reschedule appointment: " + e.getMessage()));
        }
    }
    
    private Long getUserIdFromToken(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        if (userId != null) {
            return Long.parseLong(userId.toString());
        }
        throw new RuntimeException("Invalid token");
    }
}
