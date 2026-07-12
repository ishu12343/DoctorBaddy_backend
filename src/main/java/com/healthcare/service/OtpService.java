package com.healthcare.service;

import java.util.Map;

public interface OtpService {
    
    Map<String, Object> generateOtp(String email, String mobile, String userType);
    
    Map<String, Object> validateOtp(String email, String mobile, String userType, String otp);
    
    void cleanupExpiredOtps();
}
