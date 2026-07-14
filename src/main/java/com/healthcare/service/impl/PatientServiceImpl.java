package com.healthcare.service.impl;

import com.healthcare.dto.AppointmentRequest;
import com.healthcare.dto.ForgotPasswordRequest;
import com.healthcare.dto.RatingRequest;
import com.healthcare.model.Appointment;
import com.healthcare.model.Doctor;
import com.healthcare.model.Otp;
import com.healthcare.model.Patient;
import com.healthcare.model.Rating;
import com.healthcare.repository.*;
import com.healthcare.security.JwtUtil;
import com.healthcare.service.PatientService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class PatientServiceImpl implements PatientService {
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private RatingRepository ratingRepository;
    
    @Autowired
    private OtpRepository otpRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Map<String, Object> registerPatient(Patient patient) {
        if (patientRepository.existsByEmail(patient.getEmail())) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Email already registered");
            return response;
        }
        
        // Verify OTP before registration
        Otp otpRecord = otpRepository.findByEmailAndMobileAndUserType(
            patient.getEmail(), patient.getMobile(), "PATIENT"
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
        
        patient.setPassword(passwordEncoder.encode(patient.getPassword()));
        patient.setIsActive(true);
        patient.setVerified(true);
        
        Patient savedPatient = patientRepository.save(patient);
        
        // Delete the used OTP record
        otpRepository.delete(otpRecord);
        
        String token = jwtUtil.generateToken(savedPatient.getId().toString(), "PATIENT", savedPatient.getEmail());
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "✅ Registered successfully");
        response.put("token", token);
        
        return response;
    }

    @Override
    public Map<String, Object> login(String identifier, String password) {
        Pattern emailPattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
        Pattern mobilePattern = Pattern.compile("^[0-9]{10}$");
        
        Patient patient;
        if (emailPattern.matcher(identifier).matches()) {
            patient = patientRepository.findByEmail(identifier)
                .orElseThrow(() -> new RuntimeException("Invalid email/mobile or password"));
        } else if (mobilePattern.matcher(identifier).matches()) {
            patient = patientRepository.findByMobile(identifier)
                .orElseThrow(() -> new RuntimeException("Invalid email/mobile or password"));
        } else {
            throw new RuntimeException("Please enter a valid email address or 10-digit mobile number");
        }
        
        if (!passwordEncoder.matches(password, patient.getPassword())) {
            throw new RuntimeException("Invalid email/mobile or password");
        }
        
        if (!patient.getIsActive()) {
            throw new RuntimeException("Your account is deactivated. Please contact admin.");
        }
        
        String token = jwtUtil.generateToken(patient.getId().toString(), "PATIENT", patient.getEmail());
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "✅ Login successful");
        response.put("token", token);
        Map<String, Object> patientData = new HashMap<>();
        patientData.put("id", patient.getId());
        patientData.put("name", patient.getFullName());
        patientData.put("email", patient.getEmail());
        patientData.put("role", patient.getRole());
        response.put("patient", patientData);
        
        return response;
    }

    @Override
    public Map<String, Object> sendOtp(String identifier) {
        Pattern emailPattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
        Pattern mobilePattern = Pattern.compile("^[+]?[0-9]{10,15}$");
        
        Patient patient;
        String identifierType;
        
        if (emailPattern.matcher(identifier).matches()) {
            patient = patientRepository.findByEmail(identifier)
                .orElseThrow(() -> new RuntimeException("No patient account found with this email"));
            identifierType = "email";
        } else if (mobilePattern.matcher(identifier.replace("-", "").replace(" ", "")).matches()) {
            patient = patientRepository.findByMobile(identifier)
                .orElseThrow(() -> new RuntimeException("No patient account found with this mobile"));
            identifierType = "mobile";
        } else {
            throw new RuntimeException("Invalid email or mobile format");
        }
        
        if (!patient.getIsActive()) {
            throw new RuntimeException("Your patient account is deactivated. Please contact admin.");
        }
        
        String otp = String.format("%06d", new Random().nextInt(1000000));
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(10);
        
        patient.setResetOtp(otp);
        patient.setOtpExpiresAt(expiryTime);
        patientRepository.save(patient);
        
        System.out.println("OTP for patient " + patient.getFullName() + ": " + otp);
        
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
        
        Patient patient;
        if (emailPattern.matcher(request.getIdentifier()).matches()) {
            patient = patientRepository.findByEmail(request.getIdentifier())
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        } else {
            patient = patientRepository.findByMobile(request.getIdentifier())
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        }
        
        if (!patient.getIsActive()) {
            throw new RuntimeException("Your patient account is deactivated. Please contact admin.");
        }
        
        if (patient.getResetOtp() == null || patient.getOtpExpiresAt() == null) {
            throw new RuntimeException("No OTP found. Please request a new OTP.");
        }
        
        if (LocalDateTime.now().isAfter(patient.getOtpExpiresAt())) {
            patient.setResetOtp(null);
            patient.setOtpExpiresAt(null);
            patientRepository.save(patient);
            throw new RuntimeException("OTP has expired. Please request a new one.");
        }
        
        if (!patient.getResetOtp().equals(request.getOtp())) {
            throw new RuntimeException("Invalid OTP. Please check and try again.");
        }
        
        patient.setPassword(passwordEncoder.encode(request.getNewPassword()));
        patient.setResetOtp(null);
        patient.setOtpExpiresAt(null);
        patient.setUpdatedAt(LocalDateTime.now());
        patientRepository.save(patient);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Password reset successfully. You can now login with your new password.");
        
        return response;
    }

    @Override
    public Map<String, Object> getProfile(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new RuntimeException("Patient not found"));
        
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", patient.getId());
        profile.put("full_name", patient.getFullName());
        profile.put("email", patient.getEmail());
        profile.put("mobile", patient.getMobile());
        profile.put("gender", patient.getGender());
        profile.put("date_of_birth", patient.getDateOfBirth());
        profile.put("blood_group", patient.getBloodGroup());
        profile.put("address", patient.getAddress());
        profile.put("city", patient.getCity());
        profile.put("state", patient.getState());
        profile.put("zip", patient.getZip());
        profile.put("country", patient.getCountry());
        profile.put("allergies", patient.getAllergies());
        profile.put("conditions", patient.getConditions());
        profile.put("medications", patient.getMedications());
        profile.put("surgeries", patient.getSurgeries());
        profile.put("emergency_contact_name", patient.getEmergencyContactName());
        profile.put("emergency_contact_number", patient.getEmergencyContactNumber());
        profile.put("document_path", patient.getDocumentPath());
        profile.put("photo_path", patient.getPhotoPath());
        profile.put("role", patient.getRole());
        profile.put("is_active", patient.getIsActive());
        profile.put("verified", patient.getVerified());
        profile.put("created_at", patient.getCreatedAt());
        profile.put("updated_at", patient.getUpdatedAt());
        
        Map<String, Object> response = new HashMap<>();
        response.put("patient", profile);
        response.put("email", patient.getEmail());
        response.put("role", patient.getRole());
        
        return response;
    }

    @Override
    public Map<String, Object> updateProfile(Long patientId, Map<String, Object> updates) {
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new RuntimeException("Patient not found"));
        
        if (updates.containsKey("full_name")) patient.setFullName((String) updates.get("full_name"));
        if (updates.containsKey("mobile")) patient.setMobile((String) updates.get("mobile"));
        if (updates.containsKey("gender")) patient.setGender((String) updates.get("gender"));
        if (updates.containsKey("dob")) patient.setDateOfBirth((String) updates.get("dob"));
        if (updates.containsKey("blood_group")) patient.setBloodGroup((String) updates.get("blood_group"));
        if (updates.containsKey("address")) patient.setAddress((String) updates.get("address"));
        if (updates.containsKey("photo_path")) patient.setPhotoPath((String) updates.get("photo_path"));
        if (updates.containsKey("city")) patient.setCity((String) updates.get("city"));
        if (updates.containsKey("state")) patient.setState((String) updates.get("state"));
        if (updates.containsKey("zip")) patient.setZip((String) updates.get("zip"));
        if (updates.containsKey("country")) patient.setCountry((String) updates.get("country"));
        if (updates.containsKey("allergies")) patient.setAllergies((String) updates.get("allergies"));
        if (updates.containsKey("conditions")) patient.setConditions((String) updates.get("conditions"));
        if (updates.containsKey("medications")) patient.setMedications((String) updates.get("medications"));
        if (updates.containsKey("surgeries")) patient.setSurgeries((String) updates.get("surgeries"));
        if (updates.containsKey("emergency_contact_name")) patient.setEmergencyContactName((String) updates.get("emergency_contact_name"));
        if (updates.containsKey("emergency_contact_number")) patient.setEmergencyContactNumber((String) updates.get("emergency_contact_number"));
        if (updates.containsKey("document_path")) patient.setDocumentPath((String) updates.get("document_path"));
        
        patient.setUpdatedAt(LocalDateTime.now());
        patientRepository.save(patient);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "✅ Patient profile updated successfully");
        
        return response;
    }

    @Override
    public Map<String, Object> listDoctors(String specialty, String city, String search, Long doctorId, String patientAddress, String patientLocation, String patientCity) {
        List<Doctor> doctors;
        
        if (doctorId != null) {
            doctors = doctorRepository.findAll().stream()
                .filter(d -> d.getId().equals(doctorId) && d.getApproved() && !d.getSuspended() && "ACTIVE".equals(d.getStatus()))
                .collect(Collectors.toList());
        } else {
            doctors = doctorRepository.findActiveDoctorsWithFilters(specialty, city, search, patientAddress, patientLocation, patientCity);
        }
        
        List<Map<String, Object>> formattedDoctors = new ArrayList<>();
        
        for (Doctor doctor : doctors) {
            Map<String, Object> doctorData = new HashMap<>();
            doctorData.put("id", doctor.getId());
            doctorData.put("full_name", doctor.getFullName());
            doctorData.put("email", doctor.getEmail());
            doctorData.put("mobile", doctor.getMobile());
            doctorData.put("specialty", doctor.getSpecialty());
            doctorData.put("degree", doctor.getDegree());
            doctorData.put("experience", doctor.getExperience());
            doctorData.put("clinic_name", doctor.getClinicName());
            doctorData.put("clinic_address", doctor.getClinicAddress());
            doctorData.put("city", doctor.getCity());
            doctorData.put("state", doctor.getState());
            doctorData.put("available_days", doctor.getAvailableDays());
            doctorData.put("available_from", doctor.getAvailableFrom());
            doctorData.put("available_to", doctor.getAvailableTo());
            doctorData.put("languages", doctor.getLanguages() != null ?
                Arrays.asList(doctor.getLanguages().split(",")) : new ArrayList<>());
            doctorData.put("profile_photo", doctor.getProfilePhoto());
            doctorData.put("consultation_fee", doctor.getConsultationFee() != null ? doctor.getConsultationFee() : 0.0);

            // Use rating from doctor entity instead of calculating from RatingRepository
            doctorData.put("average_rating", doctor.getRating() != null ? doctor.getRating() : 0.0);

            // Get total reviews count
            Long totalReviews = ratingRepository.countByDoctorId(doctor.getId());
            doctorData.put("total_reviews", totalReviews != null ? totalReviews : 0);
            
            formattedDoctors.add(doctorData);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("doctors", formattedDoctors);
        response.put("count", formattedDoctors.size());
        
        return response;
    }

    @Override
    public Map<String, Object> bookAppointment(Long patientId, AppointmentRequest request) {
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
            .orElseThrow(() -> new RuntimeException("Doctor not found or not available"));
        
        if (!doctor.getApproved() || doctor.getSuspended() || !"ACTIVE".equals(doctor.getStatus())) {
            throw new RuntimeException("Doctor not found or not available");
        }
        
        LocalDateTime appointmentDatetime = LocalDateTime.parse(
            request.getAppointmentDate() + " " + request.getAppointmentTime(),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        );
        
        Optional<Appointment> existingAppointment = appointmentRepository
            .findByDoctorIdAndDatetimeNotCancelled(request.getDoctorId(), appointmentDatetime);
        
        if (existingAppointment.isPresent()) {
            throw new RuntimeException("This time slot is already booked");
        }
        
        Appointment appointment = new Appointment();
        appointment.setPatientId(patientId);
        appointment.setDoctorId(request.getDoctorId());
        appointment.setAppointmentDatetime(appointmentDatetime);
        appointment.setReason(request.getReason());
        appointment.setStatus("PENDING");
        appointment.setCreatedAt(LocalDateTime.now());
        
        Appointment savedAppointment = appointmentRepository.save(appointment);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Appointment booked successfully");
        response.put("appointment_id", savedAppointment.getId());
        response.put("doctor_name", doctor.getFullName());
        
        return response;
    }

    @Override
    public Map<String, Object> updateAppointment(Long appointmentId, Long patientId, AppointmentRequest request) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        if (!appointment.getPatientId().equals(patientId)) {
            throw new RuntimeException("Appointment not found");
        }
        
        if (!"PENDING".equals(appointment.getStatus())) {
            throw new RuntimeException("Only pending appointments can be updated");
        }
        
        if (request.getAppointmentDate() == null || request.getAppointmentTime() == null) {
            throw new RuntimeException("Appointment date and time are required");
        }
        
        try {
            LocalDateTime newDateTime = LocalDateTime.parse(
                request.getAppointmentDate() + " " + request.getAppointmentTime(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            );
            
            if (newDateTime.isBefore(LocalDateTime.now())) {
                throw new RuntimeException("New appointment time must be in the future");
            }
            
            // Check for conflicts with other appointments at the same time (excluding current appointment)
            Optional<Appointment> existingAppointment = appointmentRepository
                .findByDoctorIdAndDatetimeNotCancelled(appointment.getDoctorId(), newDateTime);
            
            if (existingAppointment.isPresent() && !existingAppointment.get().getId().equals(appointmentId)) {
                throw new RuntimeException("This time slot is already booked");
            }
            
            // Update the appointment
            appointment.setAppointmentDatetime(newDateTime);
            if (request.getReason() != null) {
                appointment.setReason(request.getReason());
            }
            appointmentRepository.save(appointment);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Appointment updated successfully");
            response.put("appointment_id", appointment.getId());
            response.put("new_datetime", newDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            return response;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Invalid date or time format");
        }
    }

    @Override
    public Map<String, Object> getAppointments(Long patientId) {
        List<Appointment> appointments = appointmentRepository.findByPatientIdOrderByAppointmentDatetimeDesc(patientId);
        
        List<Map<String, Object>> appointmentList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        for (Appointment appointment : appointments) {
            Doctor doctor = doctorRepository.findById(appointment.getDoctorId()).orElse(null);
            
            Map<String, Object> apptData = new HashMap<>();
            apptData.put("id", appointment.getId());
            apptData.put("appointment_datetime", appointment.getAppointmentDatetime().format(formatter));
            apptData.put("appointment_date", appointment.getAppointmentDatetime().format(dateFormatter));
            apptData.put("appointment_time", appointment.getAppointmentDatetime().format(timeFormatter));
            apptData.put("reason", appointment.getReason());
            apptData.put("status", appointment.getStatus());
            apptData.put("created_at", appointment.getCreatedAt().format(formatter));
            
            if (doctor != null) {
                apptData.put("doctor_name", doctor.getFullName());
                apptData.put("specialty", doctor.getSpecialty());
                apptData.put("clinic_name", doctor.getClinicName());
                apptData.put("doctor_mobile", doctor.getMobile());
                apptData.put("available_days", doctor.getAvailableDays());
                apptData.put("available_from", doctor.getAvailableFrom());
                apptData.put("available_to", doctor.getAvailableTo());
            }
            
            Optional<Rating> rating = ratingRepository.findByAppointmentId(appointment.getId());
            apptData.put("has_rating", rating.isPresent() ? 1 : 0);
            
            appointmentList.add(apptData);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("appointments", appointmentList);
        
        return response;
    }

    @Override
    public Map<String, Object> cancelAppointment(Long appointmentId, Long patientId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        if (!appointment.getPatientId().equals(patientId)) {
            throw new RuntimeException("Appointment not found");
        }
        
        if ("CANCELLED".equals(appointment.getStatus())) {
            throw new RuntimeException("Appointment is already cancelled");
        }
        
        appointment.setStatus("CANCELLED");
        appointmentRepository.save(appointment);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Appointment cancelled successfully");
        
        return response;
    }

    @Override
    public Map<String, Object> rescheduleAppointment(Long appointmentId, Long patientId, AppointmentRequest request) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getPatientId().equals(patientId)) {
            throw new RuntimeException("Appointment not found");
        }

        if (request.getNewDate() == null || request.getNewTime() == null) {
            throw new RuntimeException("New date and time are required");
        }

        try {
            LocalDateTime newDateTime = LocalDateTime.parse(
                request.getNewDate() + " " + request.getNewTime() + ":00",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            );

            if (newDateTime.isBefore(LocalDateTime.now())) {
                throw new RuntimeException("New appointment time must be in the future");
            }

            // Create a new appointment instead of updating the existing one
            Appointment newAppointment = new Appointment();
            newAppointment.setPatientId(appointment.getPatientId());
            newAppointment.setDoctorId(appointment.getDoctorId());
            newAppointment.setAppointmentDatetime(newDateTime);
            newAppointment.setReason(request.getReason() != null ? request.getReason() : appointment.getReason());
            newAppointment.setStatus("PENDING");
            newAppointment.setCreatedAt(LocalDateTime.now());
            appointmentRepository.save(newAppointment);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Appointment rescheduled successfully");
            response.put("new_datetime", newDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            response.put("appointment_id", newAppointment.getId());

            return response;
        } catch (Exception e) {
            throw new RuntimeException("Invalid date or time format");
        }
    }

    @Override
    public Map<String, Object> rateAppointment(Long appointmentId, Long patientId, RatingRequest request) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getPatientId().equals(patientId)) {
            throw new RuntimeException("Appointment not found");
        }

        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new RuntimeException("Rating must be between 1 and 5");
        }

        // Check if rating already exists for this appointment
        Optional<Rating> existingRating = ratingRepository.findByAppointmentId(appointmentId);
        if (existingRating.isPresent()) {
            throw new RuntimeException("You have already rated this appointment. Ratings cannot be changed.");
        }

        // Create new rating
        Rating rating = new Rating();
        rating.setDoctorId(appointment.getDoctorId());
        rating.setPatientId(patientId);
        rating.setAppointmentId(appointmentId);
        rating.setRating(request.getRating());
        rating.setComment(request.getReview());
        ratingRepository.save(rating);

        // Recalculate and update the doctor's average rating
        Long doctorId = appointment.getDoctorId();
        Double averageRating = ratingRepository.getAverageRatingByDoctorId(doctorId);
        Doctor doctor = doctorRepository.findById(doctorId)
            .orElseThrow(() -> new RuntimeException("Doctor not found"));
        doctor.setRating(averageRating != null ? averageRating : 0.0);
        doctorRepository.save(doctor);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Rating submitted successfully");
        response.put("appointment_id", appointmentId);

        return response;
    }
}
