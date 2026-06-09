package com.healthcare.controller;

import com.healthcare.dto.ApiResponse;
import com.healthcare.dto.AppointmentRequest;
import com.healthcare.dto.ForgotPasswordRequest;
import com.healthcare.dto.LoginRequest;
import com.healthcare.model.Doctor;
import com.healthcare.service.DoctorService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.healthcare.exception.AuthenticationException;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/doctor")
public class DoctorController {
    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    // ===== FORGOT PASSWORD ENDPOINTS =====
    
    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> request) {
        try {
            String identifier = request.get("identifier");
            return ResponseEntity.ok(doctorService.sendOtp(identifier));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("Failed to send OTP", e.getMessage()));
        }
    }
    
    @PostMapping("/forgot-password/reset")
    public ResponseEntity<?> resetPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            return ResponseEntity.ok(doctorService.resetPassword(request));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("Failed to reset password", e.getMessage()));
        }
    }

    // ===== REGISTER =====
    
    @PostMapping("/register")
    public ResponseEntity<?> registerDoctor(@RequestBody Doctor doctor) {
        try {
            return ResponseEntity.ok(doctorService.registerDoctor(doctor));
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
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Email/mobile and password are required"));
            }
            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Email/mobile and password are required"));
            }
            
            return ResponseEntity.ok(doctorService.login(identifier, password));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Login failed: " + e.getMessage()));
        }
    }
    
    // ===== LOGOUT =====
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        return ResponseEntity.ok(Collections.singletonMap("message", "Doctor logged out successfully"));
    }

    // ===== PROFILE =====
    
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        try {
            Long doctorId = getUserIdFromToken(request);
            return ResponseEntity.ok(doctorService.getProfile(doctorId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Failed to fetch profile: " + e.getMessage()));
        }
    }
    
    @PutMapping("/profile/update")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, Object> updates, HttpServletRequest request) {
        try {
            Long doctorId = getUserIdFromToken(request);
            return ResponseEntity.ok(doctorService.updateProfile(doctorId, updates));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Profile update failed: " + e.getMessage()));
        }
    }

    // ===== APPOINTMENT MANAGEMENT =====
    
    @GetMapping("/appointments")
    public ResponseEntity<?> getAppointments(HttpServletRequest request) {
        try {
            Long doctorId = getUserIdFromToken(request);
            return ResponseEntity.ok(doctorService.getAppointments(doctorId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Failed to fetch appointments: " + e.getMessage()));
        }
    }
    
    @PostMapping("/appointments/{appointmentId}/approve")
    public ResponseEntity<?> approveAppointment(@PathVariable Long appointmentId, HttpServletRequest request) {
        try {
            Long doctorId = getUserIdFromToken(request);
            return ResponseEntity.ok(doctorService.approveAppointment(appointmentId, doctorId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Failed to approve appointment: " + e.getMessage()));
        }
    }
    
    @PostMapping("/appointments/{appointmentId}/reject")
    public ResponseEntity<?> rejectAppointment(@PathVariable Long appointmentId, HttpServletRequest request) {
        try {
            Long doctorId = getUserIdFromToken(request);
            return ResponseEntity.ok(doctorService.rejectAppointment(appointmentId, doctorId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Failed to reject appointment: " + e.getMessage()));
        }
    }
    
    @PostMapping("/appointments/{appointmentId}/complete")
    public ResponseEntity<?> completeAppointment(@PathVariable Long appointmentId, HttpServletRequest request) {
        try {
            Long doctorId = getUserIdFromToken(request);
            return ResponseEntity.ok(doctorService.completeAppointment(appointmentId, doctorId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Failed to complete appointment: " + e.getMessage()));
        }
    }
    
    @PostMapping("/appointments/{appointmentId}/reschedule")
    public ResponseEntity<?> rescheduleAppointment(@PathVariable Long appointmentId, @RequestBody AppointmentRequest request, HttpServletRequest httpRequest) {
        try {
            Long doctorId = getUserIdFromToken(httpRequest);
            return ResponseEntity.ok(doctorService.rescheduleAppointment(appointmentId, doctorId, request));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Failed to reschedule appointment: " + e.getMessage()));
        }
    }
    
    @GetMapping("/appointments/stats")
    public ResponseEntity<?> getAppointmentStats(HttpServletRequest request) {
        try {
            Long doctorId = getUserIdFromToken(request);
            return ResponseEntity.ok(doctorService.getAppointmentStats(doctorId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Failed to fetch appointment stats: " + e.getMessage()));
        }
    }
    
    @GetMapping("/patients")
    public ResponseEntity<?> getPatients(HttpServletRequest request) {
        try {
            Long doctorId = getUserIdFromToken(request);
            return ResponseEntity.ok(doctorService.getPatients(doctorId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Failed to fetch patients: " + e.getMessage()));
        }
    }
    
    @GetMapping("/ratings/summary")
    public ResponseEntity<?> getRatingSummary(HttpServletRequest request) {
        try {
            Long doctorId = getUserIdFromToken(request);
            return ResponseEntity.ok(doctorService.getRatingSummary(doctorId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Failed to fetch rating summary: " + e.getMessage()));
        }
    }
    
    @GetMapping("/recent-activities")
    public ResponseEntity<?> getRecentActivities(HttpServletRequest request) {
        try {
            Long doctorId = getUserIdFromToken(request);
            return ResponseEntity.ok(doctorService.getRecentActivities(doctorId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Failed to fetch recent activities: " + e.getMessage()));
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