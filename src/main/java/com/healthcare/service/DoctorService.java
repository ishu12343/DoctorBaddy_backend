package com.healthcare.service;

import com.healthcare.dto.DoctorProfileDTO;
import com.healthcare.model.Doctor;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

public interface DoctorService {
    // Existing methods
    Doctor saveDoctor(Doctor doctor);
    List<Doctor> getAllDoctors();
    Doctor registerDoctor(Doctor doctor, MultipartFile idProof, MultipartFile license,
                        MultipartFile degreeCert, MultipartFile photo) throws IOException;
    String login(Doctor loginRequest);
    
    // New methods for doctor dashboard
    DoctorProfileDTO getDoctorProfile(Long doctorId);
    DoctorProfileDTO updateDoctorProfile(Long doctorId, DoctorProfileDTO profileDTO);
    String updateDoctorPassword(Long doctorId, String currentPassword, String newPassword);
    String uploadDocument(Long doctorId, String documentType, MultipartFile file) throws IOException;
    
    // Dashboard statistics
    Map<String, Object> getDashboardStats(Long doctorId);
    
    // Document management
    byte[] getDocument(Long doctorId, String documentType) throws IOException;
    void deleteDocument(Long doctorId, String documentType) throws IOException;
    
    // Profile verification
    boolean requestVerification(Long doctorId);
    boolean isProfileComplete(Long doctorId);
    
    // Availability management
    boolean updateConsultationFee(Long doctorId, double fee);
    boolean updateAboutSection(Long doctorId, String about);
    
    // Doctor status
    boolean updateOnlineStatus(Long doctorId, boolean isOnline);
    boolean isDoctorAvailable(Long doctorId);
    
    // Search and filter doctors (for patients)
    List<DoctorProfileDTO> searchDoctors(String query, String specialty, String location, 
                                       Double minRating, Integer minExperience);
    
    // Admin methods
    boolean approveDoctor(Long doctorId, Long adminId, String comments);
    boolean rejectDoctor(Long doctorId, Long adminId, String reason);
    List<DoctorProfileDTO> getPendingApprovals();
    
    // Analytics
    Map<String, Object> getAppointmentAnalytics(Long doctorId, String period);
    Map<String, Object> getEarningsAnalytics(Long doctorId, String period);
    
    // Settings
    boolean updateNotificationPreferences(Long doctorId, Map<String, Boolean> preferences);
    Map<String, Boolean> getNotificationPreferences(Long doctorId);
    
    // Account management
    boolean deactivateAccount(Long doctorId, String reason);
    boolean deleteAccount(Long doctorId, String password);
}
