package com.healthcare.service;

import com.healthcare.dto.AppointmentRequest;
import com.healthcare.dto.ForgotPasswordRequest;
import com.healthcare.model.Doctor;

import java.util.List;
import java.util.Map;

public interface DoctorService {
    Doctor saveDoctor(Doctor doctor);
    List<Doctor> getAllDoctors();

    Map<String, Object> registerDoctor(Doctor doctor);
    Map<String, Object> login(String identifier, String password);
    Map<String, Object> sendOtp(String identifier);
    Map<String, Object> resetPassword(ForgotPasswordRequest request);
    Map<String, Object> getProfile(Long doctorId);
    Map<String, Object> updateProfile(Long doctorId, Map<String, Object> updates);
    Map<String, Object> getAppointments(Long doctorId);
    Map<String, Object> approveAppointment(Long appointmentId, Long doctorId);
    Map<String, Object> rejectAppointment(Long appointmentId, Long doctorId);
    Map<String, Object> completeAppointment(Long appointmentId, Long doctorId);
    Map<String, Object> rescheduleAppointment(Long appointmentId, Long doctorId, AppointmentRequest request);
    Map<String, Object> getAppointmentStats(Long doctorId);
    Map<String, Object> getPatients(Long doctorId);
    Map<String, Object> getRatingSummary(Long doctorId);
    Map<String, Object> getRecentActivities(Long doctorId);
}
