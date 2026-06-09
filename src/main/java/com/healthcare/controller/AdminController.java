package com.healthcare.controller;

import com.healthcare.dto.ApiResponse;
import com.healthcare.dto.ForgotPasswordRequest;
import com.healthcare.dto.LoginRequest;
import com.healthcare.model.Admin;
import com.healthcare.service.AdminService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // ===== FORGOT PASSWORD ENDPOINTS =====
    
    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> request) {
        try {
            String identifier = request.get("identifier");
            return ResponseEntity.ok(adminService.sendOtp(identifier));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("Failed to send OTP", e.getMessage()));
        }
    }
    
    @PostMapping("/forgot-password/reset")
    public ResponseEntity<?> resetPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            return ResponseEntity.ok(adminService.resetPassword(request));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("Failed to reset password", e.getMessage()));
        }
    }

    // ===== CREATE ADMIN =====
    
    @PostMapping("/create")
    public ResponseEntity<?> createAdmin(@RequestBody Admin admin) {
        try {
            return ResponseEntity.ok(adminService.createAdmin(admin));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Admin creation failed: " + e.getMessage()));
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
            
            return ResponseEntity.ok(adminService.login(identifier, password));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Collections.singletonMap("error", "Invalid credentials or inactive account"));
        }
    }
    
    // ===== LOGOUT =====
    
    @PostMapping("/api/admin/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        return ResponseEntity.ok(Collections.singletonMap("message", "Admin logged out successfully. Token revoked."));
    }

    // ===== PROFILE =====
    
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        try {
            Long adminId = getUserIdFromToken(request);
            return ResponseEntity.ok(adminService.getProfile(adminId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Failed to fetch profile: " + e.getMessage()));
        }
    }
    
    @PutMapping("/profile/update")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, Object> updates, HttpServletRequest request) {
        try {
            Long adminId = getUserIdFromToken(request);
            return ResponseEntity.ok(adminService.updateProfile(adminId, updates));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Profile update failed: " + e.getMessage()));
        }
    }

    // ===== DOCTORS MANAGEMENT =====
    
    @GetMapping("/doctors")
    public ResponseEntity<?> listDoctors(HttpServletRequest request) {
        try {
            return ResponseEntity.ok(adminService.listDoctors());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Failed to fetch doctors: " + e.getMessage()));
        }
    }
    
    @GetMapping("/doctors/view")
    public ResponseEntity<?> viewDoctor(@RequestParam Long id, HttpServletRequest request) {
        try {
            return ResponseEntity.ok(adminService.viewDoctor(id));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Failed to fetch doctor: " + e.getMessage()));
        }
    }
    
    @PutMapping("/doctors/{docId}/approve")
    public ResponseEntity<?> approveDoctor(@PathVariable Long docId, HttpServletRequest request) {
        try {
            return ResponseEntity.ok(adminService.approveDoctor(docId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Failed to approve doctor: " + e.getMessage()));
        }
    }
    
    @PutMapping("/doctors/{docId}/reject")
    public ResponseEntity<?> rejectDoctor(@PathVariable Long docId, HttpServletRequest request) {
        try {
            return ResponseEntity.ok(adminService.rejectDoctor(docId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Failed to reject doctor: " + e.getMessage()));
        }
    }
    
    @PutMapping("/doctors/{docId}/suspend")
    public ResponseEntity<?> suspendDoctor(@PathVariable Long docId, HttpServletRequest request) {
        try {
            return ResponseEntity.ok(adminService.suspendDoctor(docId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Failed to suspend doctor: " + e.getMessage()));
        }
    }
    
    @PutMapping("/doctors/{docId}/unsuspend")
    public ResponseEntity<?> unsuspendDoctor(@PathVariable Long docId, HttpServletRequest request) {
        try {
            return ResponseEntity.ok(adminService.unsuspendDoctor(docId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Failed to unsuspend doctor: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/doctors/{docId}/delete")
    public ResponseEntity<?> deleteDoctor(@PathVariable Long docId, HttpServletRequest request) {
        try {
            return ResponseEntity.ok(adminService.deleteDoctor(docId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Failed to delete doctor: " + e.getMessage()));
        }
    }

    // ===== PATIENTS MANAGEMENT =====
    
    @GetMapping("/patients")
    public ResponseEntity<?> listPatients(HttpServletRequest request) {
        try {
            return ResponseEntity.ok(adminService.listPatients());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Failed to fetch patients: " + e.getMessage()));
        }
    }
    
    @GetMapping("/patient/view")
    public ResponseEntity<?> viewPatient(@RequestParam Long id, HttpServletRequest request) {
        try {
            return ResponseEntity.ok(adminService.viewPatient(id));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Failed to fetch patient: " + e.getMessage()));
        }
    }
    
    @PutMapping("/patients/{patId}/activate")
    public ResponseEntity<?> activatePatient(@PathVariable Long patId, HttpServletRequest request) {
        try {
            return ResponseEntity.ok(adminService.activatePatient(patId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Failed to activate patient: " + e.getMessage()));
        }
    }
    
    @PutMapping("/patients/{patId}/deactivate")
    public ResponseEntity<?> deactivatePatient(@PathVariable Long patId, HttpServletRequest request) {
        try {
            return ResponseEntity.ok(adminService.deactivatePatient(patId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Failed to deactivate patient: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/patients/{patId}/delete")
    public ResponseEntity<?> deletePatient(@PathVariable Long patId, HttpServletRequest request) {
        try {
            return ResponseEntity.ok(adminService.deletePatient(patId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Failed to delete patient: " + e.getMessage()));
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
