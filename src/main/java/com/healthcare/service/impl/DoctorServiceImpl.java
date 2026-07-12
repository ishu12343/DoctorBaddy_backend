package com.healthcare.service.impl;

import com.healthcare.dto.AppointmentRequest;
import com.healthcare.dto.ForgotPasswordRequest;
import com.healthcare.exception.AuthenticationException;
import com.healthcare.model.Appointment;
import com.healthcare.model.Doctor;
import com.healthcare.model.Otp;
import com.healthcare.model.Patient;
import com.healthcare.repository.AppointmentRepository;
import com.healthcare.repository.DoctorRepository;
import com.healthcare.repository.OtpRepository;
import com.healthcare.repository.PatientRepository;
import com.healthcare.repository.RatingRepository;
import com.healthcare.security.JwtUtil;
import com.healthcare.service.DoctorService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final RatingRepository ratingRepository;
    private final OtpRepository otpRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Doctor saveDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    @Override
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    // ===== New methods to match Python backend =====
    
    @Override
    public Map<String, Object> registerDoctor(Doctor doctor) {
        if (doctorRepository.existsByEmail(doctor.getEmail())) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Email already registered");
            return response;
        }
        
        // Verify OTP before registration
        Otp otpRecord = otpRepository.findByEmailAndMobileAndUserType(
            doctor.getEmail(), doctor.getMobile(), "DOCTOR"
        ).orElse(null);
        
        if (otpRecord == null || !otpRecord.getVerified()) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Please verify your OTP before registering");
            return response;
        }
        
        // Check if OTP has expired
        if (LocalDateTime.now().isAfter(otpRecord.getExpiresAt())) {
            otpRepository.delete(otpRecord);
            Map<String, Object> response = new HashMap<>();
            response.put("error", "OTP has expired. Please request a new OTP");
            return response;
        }
        
        doctor.setPassword(passwordEncoder.encode(doctor.getPassword()));
        doctor.setApproved(false);
        doctor.setSuspended(false);
        doctor.setStatus("PENDING");
        
        Doctor savedDoctor = doctorRepository.save(doctor);
        
        // Delete the used OTP record
        otpRepository.delete(otpRecord);
        
        String token = jwtUtil.generateToken(savedDoctor.getId().toString(), "DOCTOR");
        
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        Map<String, Object> doctorData = new HashMap<>();
        doctorData.put("id", savedDoctor.getId());
        doctorData.put("full_name", savedDoctor.getFullName());
        doctorData.put("email", savedDoctor.getEmail());
        doctorData.put("mobile", savedDoctor.getMobile());
        doctorData.put("role", "DOCTOR");
        response.put("doctor", doctorData);
        
        return response;
    }
    
    @Override
    public Map<String, Object> login(String identifier, String password) {
        Pattern emailPattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
        Pattern mobilePattern = Pattern.compile("^[0-9]{10}$");
        
        Doctor doctor;
        if (emailPattern.matcher(identifier).matches()) {
            doctor = doctorRepository.findByEmail(identifier)
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));
        } else if (mobilePattern.matcher(identifier).matches()) {
            doctor = doctorRepository.findByMobile(identifier)
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));
        } else {
            throw new AuthenticationException("Please enter a valid email address or 10-digit mobile number");
        }
        
        if (!passwordEncoder.matches(password, doctor.getPassword())) {
            throw new AuthenticationException("Invalid credentials");
        }
        
        if (!doctor.getApproved()) {
            throw new AuthenticationException("Your account is pending approval by the admin. Please wait for approval.");
        }
        
        if (doctor.getSuspended()) {
            throw new AuthenticationException("Your account has been suspended by the admin. Please contact support.");
        }
        
        String token = jwtUtil.generateToken(doctor.getId().toString(), "DOCTOR");
        
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        Map<String, Object> doctorData = new HashMap<>();
        doctorData.put("id", doctor.getId());
        doctorData.put("full_name", doctor.getFullName());
        doctorData.put("email", doctor.getEmail());
        doctorData.put("mobile", doctor.getMobile());
        doctorData.put("role", doctor.getRole());
        doctorData.put("approved", doctor.getApproved());
        doctorData.put("suspended", doctor.getSuspended());
        response.put("doctor", doctorData);
        
        return response;
    }
    
    @Override
    public Map<String, Object> sendOtp(String identifier) {
        Pattern emailPattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
        Pattern mobilePattern = Pattern.compile("^[+]?[0-9]{10,15}$");
        
        Doctor doctor;
        String identifierType;
        
        if (emailPattern.matcher(identifier).matches()) {
            doctor = doctorRepository.findByEmail(identifier)
                .orElseThrow(() -> new RuntimeException("No doctor account found with this email"));
            identifierType = "email";
        } else if (mobilePattern.matcher(identifier.replace("-", "").replace(" ", "")).matches()) {
            doctor = doctorRepository.findByMobile(identifier)
                .orElseThrow(() -> new RuntimeException("No doctor account found with this mobile"));
            identifierType = "mobile";
        } else {
            throw new RuntimeException("Invalid email or mobile format");
        }
        
        if (!doctor.getApproved()) {
            throw new RuntimeException("Your doctor account is pending approval. Please contact admin.");
        }
        
        if (doctor.getSuspended()) {
            throw new RuntimeException("Your doctor account is suspended. Please contact admin.");
        }
        
        // Generate 6-digit OTP
        String otp = String.format("%06d", new Random().nextInt(1000000));
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(10);
        
        doctor.setResetOtp(otp);
        doctor.setOtpExpiresAt(expiryTime);
        doctorRepository.save(doctor);
        
        System.out.println("OTP for doctor " + doctor.getFullName() + ": " + otp);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "OTP sent to your registered " + identifierType);
        response.put("identifier_type", identifierType);
        response.put("otp", otp);
        
        return response;
    }
    
    @Override
    public Map<String, Object> resetPassword(ForgotPasswordRequest request) {
        Pattern emailPattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
        
        Doctor doctor;
        if (emailPattern.matcher(request.getIdentifier()).matches()) {
            doctor = doctorRepository.findByEmail(request.getIdentifier())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        } else {
            doctor = doctorRepository.findByMobile(request.getIdentifier())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        }
        
        if (!doctor.getApproved()) {
            throw new RuntimeException("Your doctor account is pending approval. Please contact admin.");
        }
        
        if (doctor.getSuspended()) {
            throw new RuntimeException("Your doctor account is suspended. Please contact admin.");
        }
        
        if (doctor.getResetOtp() == null || doctor.getOtpExpiresAt() == null) {
            throw new RuntimeException("No OTP found. Please request a new OTP.");
        }
        
        if (LocalDateTime.now().isAfter(doctor.getOtpExpiresAt())) {
            doctor.setResetOtp(null);
            doctor.setOtpExpiresAt(null);
            doctorRepository.save(doctor);
            throw new RuntimeException("OTP has expired. Please request a new one.");
        }
        
        if (!doctor.getResetOtp().equals(request.getOtp())) {
            throw new RuntimeException("Invalid OTP. Please check and try again.");
        }
        
        doctor.setPassword(passwordEncoder.encode(request.getNewPassword()));
        doctor.setResetOtp(null);
        doctor.setOtpExpiresAt(null);
        doctor.setUpdatedAt(LocalDateTime.now());
        doctorRepository.save(doctor);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Password reset successfully. You can now login with your new password.");
        
        return response;
    }
    
    @Override
    public Map<String, Object> getProfile(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
            .orElseThrow(() -> new RuntimeException("Doctor not found"));
        
        // Sync status based on approval and suspension
        String newStatus = "INACTIVE";
        if (doctor.getSuspended()) {
            newStatus = "SUSPENDED";
        } else if (doctor.getApproved()) {
            newStatus = "ACTIVE";
        } else {
            newStatus = "PENDING";
        }
        
        if (!newStatus.equals(doctor.getStatus())) {
            doctor.setStatus(newStatus);
            doctorRepository.save(doctor);
        }
        
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", doctor.getId());
        profile.put("full_name", doctor.getFullName());
        profile.put("email", doctor.getEmail());
        profile.put("mobile", doctor.getMobile());
        profile.put("gender", doctor.getGender());
        profile.put("location", doctor.getLocation());
        profile.put("registration_number", doctor.getRegistrationNumber());
        profile.put("license_number", doctor.getLicenseNumber());
        profile.put("council", doctor.getCouncil());
        profile.put("degree", doctor.getDegree());
        profile.put("specialty", doctor.getSpecialty());
        profile.put("experience", doctor.getExperience());
        profile.put("clinic_name", doctor.getClinicName());
        profile.put("clinic_address", doctor.getClinicAddress());
        profile.put("profile_photo", doctor.getProfilePhoto());
        profile.put("role", doctor.getRole());
        profile.put("dob", doctor.getDob());
        profile.put("blood_group", doctor.getBloodGroup());
        profile.put("available_days", doctor.getAvailableDays());
        profile.put("available_from", doctor.getAvailableFrom());
        profile.put("available_to", doctor.getAvailableTo());
        profile.put("city", doctor.getCity());
        profile.put("state", doctor.getState());
        profile.put("zip_code", doctor.getZipCode());
        profile.put("languages", doctor.getLanguages());
        profile.put("status", doctor.getStatus());
        profile.put("documents", doctor.getDocuments());
        profile.put("approved", doctor.getApproved());
        profile.put("suspended", doctor.getSuspended());
        profile.put("created_at", doctor.getCreatedAt());
        profile.put("updated_at", doctor.getUpdatedAt());
        
        return profile;
    }
    
    @Override
    public Map<String, Object> updateProfile(Long doctorId, Map<String, Object> updates) {
        Doctor doctor = doctorRepository.findById(doctorId)
            .orElseThrow(() -> new RuntimeException("Doctor not found"));
        
        if (updates.containsKey("full_name")) doctor.setFullName(convertToString(updates.get("full_name")));
        if (updates.containsKey("email")) doctor.setEmail(convertToString(updates.get("email")));
        if (updates.containsKey("mobile")) doctor.setMobile(convertToString(updates.get("mobile")));
        if (updates.containsKey("gender")) doctor.setGender(convertToString(updates.get("gender")));
        if (updates.containsKey("location")) doctor.setLocation(convertToString(updates.get("location")));
        if (updates.containsKey("registration_number")) doctor.setRegistrationNumber(convertToString(updates.get("registration_number")));
        if (updates.containsKey("license_number")) doctor.setLicenseNumber(convertToString(updates.get("license_number")));
        if (updates.containsKey("council")) doctor.setCouncil(convertToString(updates.get("council")));
        if (updates.containsKey("degree")) doctor.setDegree(convertToString(updates.get("degree")));
        if (updates.containsKey("specialty")) doctor.setSpecialty(convertToString(updates.get("specialty")));
        if (updates.containsKey("experience")) doctor.setExperience(convertToString(updates.get("experience")));
        if (updates.containsKey("clinic_name")) doctor.setClinicName(convertToString(updates.get("clinic_name")));
        if (updates.containsKey("clinic_address")) doctor.setClinicAddress(convertToString(updates.get("clinic_address")));
        if (updates.containsKey("profile_photo")) doctor.setProfilePhoto(convertToString(updates.get("profile_photo")));
        if (updates.containsKey("dob")) doctor.setDob(convertToString(updates.get("dob")));
        if (updates.containsKey("blood_group")) doctor.setBloodGroup(convertToString(updates.get("blood_group")));
        if (updates.containsKey("available_days")) doctor.setAvailableDays(convertToString(updates.get("available_days")));
        if (updates.containsKey("available_from")) doctor.setAvailableFrom(convertToString(updates.get("available_from")));
        if (updates.containsKey("available_to")) doctor.setAvailableTo(convertToString(updates.get("available_to")));
        if (updates.containsKey("city")) doctor.setCity(convertToString(updates.get("city")));
        if (updates.containsKey("state")) doctor.setState(convertToString(updates.get("state")));
        if (updates.containsKey("zip_code")) doctor.setZipCode(convertToString(updates.get("zip_code")));
        if (updates.containsKey("languages")) doctor.setLanguages(convertToString(updates.get("languages")));
        if (updates.containsKey("status")) doctor.setStatus(convertToString(updates.get("status")));
        if (updates.containsKey("approved")) doctor.setApproved(convertToBoolean(updates.get("approved")));
        if (updates.containsKey("suspended")) doctor.setSuspended(convertToBoolean(updates.get("suspended")));
        if (updates.containsKey("documents")) doctor.setDocuments(convertToString(updates.get("documents")));
        
        doctor.setUpdatedAt(LocalDateTime.now());
        doctorRepository.save(doctor);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Profile updated successfully");
        
        return response;
    }
    
    @Override
    public Map<String, Object> getAppointments(Long doctorId) {
        List<Appointment> appointments = appointmentRepository.findByDoctorIdOrderByAppointmentDatetimeDesc(doctorId);
        
        List<Map<String, Object>> appointmentList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        for (Appointment appointment : appointments) {
            Patient patient = patientRepository.findById(appointment.getPatientId()).orElse(null);
            
            Map<String, Object> apptData = new HashMap<>();
            apptData.put("id", appointment.getId());
            apptData.put("appointment_datetime", appointment.getAppointmentDatetime().format(formatter));
            apptData.put("reason", appointment.getReason());
            apptData.put("status", appointment.getStatus());
            apptData.put("created_at", appointment.getCreatedAt().format(formatter));
            
            if (patient != null) {
                apptData.put("patient_name", patient.getFullName());
                apptData.put("patient_email", patient.getEmail());
                apptData.put("patient_phone", patient.getMobile());
                apptData.put("patient_blood_group", patient.getBloodGroup());
                apptData.put("patient_gender", patient.getGender());
            }
            
            appointmentList.add(apptData);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("appointments", appointmentList);
        
        return response;
    }
    
    @Override
    public Map<String, Object> approveAppointment(Long appointmentId, Long doctorId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        if (!appointment.getDoctorId().equals(doctorId)) {
            throw new RuntimeException("Appointment not found or unauthorized");
        }
        
        if (!"PENDING".equals(appointment.getStatus())) {
            throw new RuntimeException("Only pending appointments can be approved");
        }
        
        appointment.setStatus("CONFIRMED");
        appointmentRepository.save(appointment);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Appointment approved successfully");
        
        return response;
    }
    
    @Override
    public Map<String, Object> rejectAppointment(Long appointmentId, Long doctorId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        if (!appointment.getDoctorId().equals(doctorId)) {
            throw new RuntimeException("Appointment not found or unauthorized");
        }
        
        if (!"PENDING".equals(appointment.getStatus()) && !"CONFIRMED".equals(appointment.getStatus())) {
            throw new RuntimeException("Only pending or confirmed appointments can be rejected");
        }
        
        appointment.setStatus("CANCELLED");
        appointmentRepository.save(appointment);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Appointment rejected successfully");
        
        return response;
    }
    
    @Override
    public Map<String, Object> completeAppointment(Long appointmentId, Long doctorId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        if (!appointment.getDoctorId().equals(doctorId)) {
            throw new RuntimeException("Appointment not found or unauthorized");
        }
        
        if (!"CONFIRMED".equals(appointment.getStatus())) {
            throw new RuntimeException("Only confirmed appointments can be marked as completed");
        }
        
        appointment.setStatus("COMPLETED");
        appointmentRepository.save(appointment);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Appointment marked as completed");
        
        return response;
    }
    
    @Override
    public Map<String, Object> rescheduleAppointment(Long appointmentId, Long doctorId, AppointmentRequest request) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        if (!appointment.getDoctorId().equals(doctorId)) {
            throw new RuntimeException("Appointment not found or unauthorized");
        }
        
        if (request.getNewDate() == null || request.getNewTime() == null) {
            throw new RuntimeException("New date and time are required");
        }
        
        try {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            
            LocalDateTime newDateTime = LocalDateTime.parse(
                request.getNewDate() + " " + request.getNewTime() + ":00",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            );
            
            if (newDateTime.isBefore(LocalDateTime.now())) {
                throw new RuntimeException("New appointment time must be in the future");
            }
            
            appointment.setAppointmentDatetime(newDateTime);
            appointment.setStatus("CONFIRMED");
            appointmentRepository.save(appointment);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Appointment rescheduled successfully");
            response.put("new_datetime", newDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            response.put("appointment_id", appointmentId);
            
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Invalid date or time format");
        }
    }
    
    @Override
    public Map<String, Object> getAppointmentStats(Long doctorId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total_appointments", appointmentRepository.countByDoctorId(doctorId));
        stats.put("pending_appointments", appointmentRepository.countPendingByDoctorId(doctorId));
        stats.put("confirmed_appointments", appointmentRepository.countConfirmedByDoctorId(doctorId));
        stats.put("completed_appointments", appointmentRepository.countCompletedByDoctorId(doctorId));
        stats.put("cancelled_appointments", appointmentRepository.countCancelledByDoctorId(doctorId));
        stats.put("today_appointments", appointmentRepository.countTodayByDoctorId(doctorId));
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("stats", stats);
        
        return response;
    }
    
    @Override
    public Map<String, Object> getPatients(Long doctorId) {
        List<Appointment> appointments = appointmentRepository.findByDoctorIdOrderByAppointmentDatetimeDesc(doctorId);
        
        List<Map<String, Object>> patientList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        for (Appointment appointment : appointments) {
            Patient patient = patientRepository.findById(appointment.getPatientId()).orElse(null);
            
            if (patient != null) {
                Map<String, Object> patientData = new HashMap<>();
                patientData.put("appointment_id", appointment.getId());
                patientData.put("appointment_datetime", appointment.getAppointmentDatetime().format(formatter));
                patientData.put("reason", appointment.getReason());
                patientData.put("appointment_status", appointment.getStatus());
                patientData.put("appointment_created_at", appointment.getCreatedAt().format(formatter));
                patientData.put("patient_id", patient.getId());
                patientData.put("full_name", patient.getFullName());
                patientData.put("email", patient.getEmail());
                patientData.put("mobile", patient.getMobile());
                patientData.put("gender", patient.getGender());
                
                patientList.add(patientData);
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("patients", patientList);
        
        return response;
    }
    
    @Override
    public Map<String, Object> getRatingSummary(Long doctorId) {
        Double averageRating = ratingRepository.getAverageRatingByDoctorId(doctorId);
        Long totalReviews = ratingRepository.countByDoctorId(doctorId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("rating", averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : 0.0);
        response.put("reviewCount", totalReviews != null ? totalReviews : 0);
        
        return response;
    }
    
    @Override
    public Map<String, Object> getRecentActivities(Long doctorId) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        
        // Get recent appointments (last 7 days) with patient details
        List<Appointment> recentAppointments = appointmentRepository
            .findByDoctorIdAndCreatedAtAfterOrderByCreatedAtDesc(doctorId, sevenDaysAgo);
        
        List<Map<String, Object>> activities = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        for (Appointment appointment : recentAppointments) {
            Patient patient = patientRepository.findById(appointment.getPatientId()).orElse(null);
            
            if (patient != null) {
                Map<String, Object> activity = new HashMap<>();
                activity.put("activity_id", appointment.getId());
                activity.put("activity_type", "appointment");
                activity.put("status", appointment.getStatus());
                activity.put("appointment_datetime", appointment.getAppointmentDatetime().format(formatter));
                activity.put("reason", appointment.getReason());
                activity.put("created_at", appointment.getCreatedAt().format(formatter));
                activity.put("patient_id", patient.getId());
                activity.put("patient_name", patient.getFullName());
                activity.put("patient_email", patient.getEmail());
                activity.put("patient_mobile", patient.getMobile());
                activity.put("action_type", "appointment_status_change");
                
                activities.add(activity);
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("activities", activities);
        
        return response;
    }
    
    private String convertToString(Object value) {
        if (value == null) return null;
        return value.toString();
    }
    
    private Boolean convertToBoolean(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof String) return Boolean.parseBoolean((String) value);
        if (value instanceof Number) return ((Number) value).intValue() != 0;
        return false;
    }
}
