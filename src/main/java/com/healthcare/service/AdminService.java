package com.healthcare.service;

import com.healthcare.dto.ForgotPasswordRequest;

import java.util.List;
import java.util.Map;

public interface AdminService {
    Map<String, Object> createAdmin(com.healthcare.model.Admin admin);
    Map<String, Object> login(String identifier, String password);
    Map<String, Object> sendOtp(String identifier);
    Map<String, Object> resetPassword(ForgotPasswordRequest request);
    Map<String, Object> getProfile(Long adminId);
    Map<String, Object> updateProfile(Long adminId, Map<String, Object> updates);
    List<Map<String, Object>> listDoctors();
    Map<String, Object> viewDoctor(Long id);
    Map<String, Object> approveDoctor(Long docId);
    Map<String, Object> rejectDoctor(Long docId);
    Map<String, Object> suspendDoctor(Long docId);
    Map<String, Object> unsuspendDoctor(Long docId);
    Map<String, Object> deleteDoctor(Long docId);
    List<Map<String, Object>> listPatients();
    Map<String, Object> viewPatient(Long id);
    Map<String, Object> activatePatient(Long patId);
    Map<String, Object> deactivatePatient(Long patId);
    Map<String, Object> deletePatient(Long patId);
}
