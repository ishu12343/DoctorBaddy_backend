package com.healthcare.service.impl;

import com.healthcare.model.Otp;
import com.healthcare.repository.OtpRepository;
import com.healthcare.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {
    
    private final OtpRepository otpRepository;
    
    @Override
    public Map<String, Object> generateOtp(String email, String mobile, String userType) {
        // Delete any existing unverified OTP for this email/mobile
        otpRepository.deleteByEmailAndMobileAndUserType(email, mobile, userType);
        
        // Generate 6-digit OTP
        String otp = String.format("%06d", new Random().nextInt(1000000));
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(10);
        
        // Create new OTP record
        Otp otpRecord = new Otp();
        otpRecord.setEmail(email);
        otpRecord.setMobile(mobile);
        otpRecord.setOtp(otp);
        otpRecord.setUserType(userType);
        otpRecord.setVerified(false);
        otpRecord.setExpiresAt(expiryTime);
        
        otpRepository.save(otpRecord);
        
        // Log OTP for development (in production, this would be sent via email/SMS)
        System.out.println("========================================");
        System.out.println("OTP for " + userType + " Registration");
        System.out.println("Email: " + email);
        System.out.println("Mobile: " + mobile);
        System.out.println("OTP: " + otp);
        System.out.println("Expires at: " + expiryTime);
        System.out.println("========================================");
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "OTP sent successfully to email and mobile");
        response.put("otp", otp); // Include OTP in response for development
        response.put("expiresAt", expiryTime.toString());
        
        return response;
    }
    
    @Override
    public Map<String, Object> validateOtp(String email, String mobile, String userType, String otp) {
        // Find the OTP record
        Otp otpRecord = otpRepository.findByEmailAndMobileAndUserTypeAndVerifiedFalse(email, mobile, userType)
            .orElse(null);
        
        if (otpRecord == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "No valid OTP found. Please request a new OTP.");
            return response;
        }
        
        // Check if OTP has expired
        if (LocalDateTime.now().isAfter(otpRecord.getExpiresAt())) {
            otpRepository.delete(otpRecord);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "OTP has expired. Please request a new OTP.");
            return response;
        }
        
        // Validate OTP
        if (!otpRecord.getOtp().equals(otp)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Invalid OTP. Please check and try again.");
            return response;
        }
        
        // Mark OTP as verified
        otpRecord.setVerified(true);
        otpRepository.save(otpRecord);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "OTP validated successfully");
        
        return response;
    }
    
    @Override
    public void cleanupExpiredOtps() {
        LocalDateTime now = LocalDateTime.now();
        otpRepository.deleteByExpiresAtBefore(now);
    }
}
