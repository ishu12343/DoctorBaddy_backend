package com.healthcare.controller;

import com.healthcare.model.Doctor;
import com.healthcare.model.User;
import com.healthcare.security.JwtUtil;
import com.healthcare.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/doctors/register")
    public ResponseEntity<?> registerDoctor(@RequestBody Doctor registration) {
        try {
            Doctor doctor = authService.registerDoctor(registration);
            
            // Generate JWT token
            String token = jwtUtil.generateToken(doctor.getEmail(), "DOCTOR");
            
            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Doctor registered successfully");
            response.put("doctorId", doctor.getId());
            response.put("token", token);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/patients/register")
    public ResponseEntity<?> registerPatient(@RequestBody User registration) {
        try {
            User user = authService.registerPatient(registration);
            
            // Generate JWT token
            String token = jwtUtil.generateToken(user.getEmail(), "PATIENT");
            
            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Patient registered successfully");
            response.put("userId", user.getId());
            response.put("token", token);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
