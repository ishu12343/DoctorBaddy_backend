package com.healthcare.service;

import com.healthcare.dto.AppointmentRequest;
import com.healthcare.dto.ForgotPasswordRequest;
import com.healthcare.dto.RatingRequest;

import java.util.Map;

public interface PatientService {
    Map<String, Object> registerPatient(com.healthcare.model.Patient patient);
    Map<String, Object> login(String identifier, String password);
    Map<String, Object> sendOtp(String identifier);
    Map<String, Object> resetPassword(ForgotPasswordRequest request);
    Map<String, Object> getProfile(Long patientId);
    Map<String, Object> updateProfile(Long patientId, Map<String, Object> updates);
    Map<String, Object> listDoctors(String specialty, String city, String search, Long doctorId);
    Map<String, Object> bookAppointment(Long patientId, AppointmentRequest request);
    Map<String, Object> getAppointments(Long patientId);
    Map<String, Object> cancelAppointment(Long appointmentId, Long patientId);
    Map<String, Object> rescheduleAppointment(Long appointmentId, Long patientId, AppointmentRequest request);
    Map<String, Object> rateAppointment(Long appointmentId, Long patientId, RatingRequest request);
}
