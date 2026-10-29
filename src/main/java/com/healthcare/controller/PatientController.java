package com.healthcare.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.dto.ApiResponse;
import com.healthcare.dto.AppointmentRequest;
import com.healthcare.dto.ForgotPasswordRequest;
import com.healthcare.dto.LoginRequest;
import com.healthcare.dto.RatingRequest;
import com.healthcare.model.Patient;
import com.healthcare.service.PatientService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/patient")
public class PatientController {
    private final PatientService patientService;
    private final ObjectMapper objectMapper;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
        this.objectMapper = new ObjectMapper();
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
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, Object> updates,
                                           HttpServletRequest request) {
        try {
            Long patientId = getUserIdFromToken(request);
            System.out.println("DEBUG: Received updates: " + updates);
            System.out.println("DEBUG: Patient ID: " + patientId);
            
            // Handle documentPath - convert to JSON string for storage in photo_path
            if (updates.containsKey("documentPath")) {
                Object documentPath = updates.get("documentPath");
                System.out.println("DEBUG: documentPath type: " + documentPath.getClass());
                System.out.println("DEBUG: documentPath value: " + documentPath);
                if (documentPath instanceof Map) {
                    // Convert the documentPath map to JSON string for storage in photo_path
                    String jsonString = objectMapper.writeValueAsString(documentPath);
                    System.out.println("DEBUG: JSON string: " + jsonString);
                    updates.put("photo_path", jsonString);
                }
            }
            
            // Map emergency_contact to emergency_contact_number for backend compatibility
            if (updates.containsKey("emergency_contact")) {
                updates.put("emergency_contact_number", updates.get("emergency_contact"));
            }
            
            System.out.println("DEBUG: Final updates map: " + updates);
            return ResponseEntity.ok(patientService.updateProfile(patientId, updates));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Profile update failed: " + e.getMessage()));
        }
    }

    // ===== DOCTORS LIST =====
    
    @GetMapping("/doctors")
    public ResponseEntity<?> listDoctors(
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long doctor_id,
            @RequestParam(required = false) String patient_address,
            @RequestParam(required = false) String patient_location,
            @RequestParam(required = false) String patient_city) {
        try {
            return ResponseEntity.ok(patientService.listDoctors(specialty, city, search, doctor_id, patient_address, patient_location, patient_city));
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
    
    @PutMapping("/appointments/{appointmentId}")
    public ResponseEntity<?> updateAppointment(@PathVariable Long appointmentId, @RequestBody AppointmentRequest request, HttpServletRequest httpRequest) {
        try {
            Long patientId = getUserIdFromToken(httpRequest);
            return ResponseEntity.ok(patientService.updateAppointment(appointmentId, patientId, request));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Failed to update appointment: " + e.getMessage()));
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

    @PostMapping("/appointments/{appointmentId}/rate")
    public ResponseEntity<?> rateAppointment(@PathVariable Long appointmentId, @RequestBody RatingRequest request, HttpServletRequest httpRequest) {
        try {
            Long patientId = getUserIdFromToken(httpRequest);
            return ResponseEntity.ok(patientService.rateAppointment(appointmentId, patientId, request));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Failed to rate appointment: " + e.getMessage()));
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
