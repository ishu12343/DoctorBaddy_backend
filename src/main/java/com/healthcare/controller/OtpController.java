package com.healthcare.controller;

import com.healthcare.service.OtpService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/otp")
public class OtpController {
    
    private final OtpService otpService;
    
    public OtpController(OtpService otpService) {
        this.otpService = otpService;
    }
    
    @PostMapping("/generate")
    public ResponseEntity<?> generateOtp(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String mobile = request.get("mobile");
            String userType = request.get("userType"); // DOCTOR or PATIENT
            
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Email is required"));
            }
            if (mobile == null || mobile.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Mobile is required"));
            }
            if (userType == null || userType.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User type is required"));
            }
            
            Map<String, Object> response = otpService.generateOtp(email, mobile, userType);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Failed to generate OTP: " + e.getMessage()));
        }
    }
    
    @PostMapping("/validate")
    public ResponseEntity<?> validateOtp(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String mobile = request.get("mobile");
            String userType = request.get("userType");
            String otp = request.get("otp");
            
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Email is required"));
            }
            if (mobile == null || mobile.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Mobile is required"));
            }
            if (userType == null || userType.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User type is required"));
            }
            if (otp == null || otp.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "OTP is required"));
            }
            
            Map<String, Object> response = otpService.validateOtp(email, mobile, userType, otp);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Failed to validate OTP: " + e.getMessage()));
        }
    }
}
