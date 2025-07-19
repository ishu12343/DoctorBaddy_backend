package com.healthcare.service.impl;

import com.healthcare.dto.*;
import com.healthcare.exception.AuthenticationException;
import com.healthcare.exception.ResourceNotFoundException;
import com.healthcare.model.*;
import com.healthcare.repository.*;
import com.healthcare.service.DoctorService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of DoctorService for doctor dashboard functionality.
 */
@Service
@Transactional
public class DoctorDashboardServiceImpl implements DoctorService {
    
    private static final String DOCTOR_NOT_FOUND_MSG = "Doctor not found with id: ";
    private static final String INVALID_DOCUMENT_TYPE_MSG = "Invalid document type. Must be one of: DEGREE, LICENSE, ID_PROOF, PHOTO";
    
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final ReviewRepository reviewRepository;
    private final AvailabilityRepository availabilityRepository;
    private final NotificationRepository notificationRepository;
    private final PatientRepository patientRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    
    @Value("${file.upload-dir}")
    private String uploadDir;
    
    @Value("${app.base-url}")
    private String baseUrl;
    
    @Autowired
    public DoctorDashboardServiceImpl(
            DoctorRepository doctorRepository,
            AppointmentRepository appointmentRepository,
            ReviewRepository reviewRepository,
            AvailabilityRepository availabilityRepository,
            NotificationRepository notificationRepository,
            PatientRepository patientRepository,
            PaymentRepository paymentRepository,
            PasswordEncoder passwordEncoder,
            ModelMapper modelMapper) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.reviewRepository = reviewRepository;
        this.availabilityRepository = availabilityRepository;
        this.notificationRepository = notificationRepository;
        this.patientRepository = patientRepository;
        this.paymentRepository = paymentRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
    }

    @Override
    public Doctor saveDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    @Override
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    @Override
    public Doctor registerDoctor(Doctor doctor, MultipartFile idProof, MultipartFile license,
                               MultipartFile degreeCert, MultipartFile photo) throws IOException {
        // Implementation for doctor registration with file uploads
        String basePath = uploadDir + "/doctors/" + doctor.getEmail().replaceAll("[^a-zA-Z0-9]", "_");
        Path path = Paths.get(basePath);
        Files.createDirectories(path);
        
        if (idProof != null && !idProof.isEmpty()) {
            String idProofPath = saveFile(basePath, idProof, "id_proof");
            doctor.setIdProofPath(idProofPath);
        }
        
        if (license != null && !license.isEmpty()) {
            String licensePath = saveFile(basePath, license, "license");
            doctor.setLicensePath(licensePath);
        }
        
        if (degreeCert != null && !degreeCert.isEmpty()) {
            String degreePath = saveFile(basePath, degreeCert, "degree");
            doctor.setDegreeCertPath(degreePath);
        }
        
        if (photo != null && !photo.isEmpty()) {
            String photoPath = saveFile(basePath, photo, "photo");
            doctor.setPhotoPath(photoPath);
        }
        
        doctor.setPassword(passwordEncoder.encode(doctor.getPassword()));
        doctor.setRole("DOCTOR");
        doctor.setActive(true);
        doctor.setApproved(false); // Admin approval required
        
        return doctorRepository.save(doctor);
    }
    
    private String saveFile(String basePath, MultipartFile file, String prefix) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        String fileName = prefix + "_" + System.currentTimeMillis() + fileExtension;
        Path filePath = Paths.get(basePath, fileName);
        
        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + Paths.get(basePath).getFileName().toString() + "/" + fileName;
        } catch (IOException e) {
            throw new IOException("Could not save file: " + originalFilename, e);
        }
    }

    @Override
    public String login(Doctor loginRequest) {
        Doctor doctor = doctorRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new AuthenticationException("Invalid email or password"));
        
        if (!passwordEncoder.matches(loginRequest.getPassword(), doctor.getPassword())) {
            throw new AuthenticationException("Invalid email or password");
        }
        
        if (!doctor.isActive()) {
            throw new AuthenticationException("Account is deactivated. Please contact support.");
        }
        
        if (!doctor.isApproved()) {
            throw new AuthenticationException("Your account is pending approval. Please wait for admin approval.");
        }
        
        // Update last login
        doctor.setLastLogin(LocalDateTime.now());
        doctorRepository.save(doctor);
        
        // Generate JWT token
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", doctor.getId());
        claims.put("email", doctor.getEmail());
        claims.put("role", doctor.getRole());
        claims.put("name", doctor.getName() != null ? doctor.getName() : doctor.getFullName());
        
        // You'll need to implement JwtUtil or use your existing implementation
        // For now, we'll return a placeholder token
        return "jwt.placeholder.token";
    }
    
    @Override
    public DoctorProfileDTO getDoctorProfile(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(DOCTOR_NOT_FOUND_MSG + doctorId));
                
        DoctorProfileDTO profileDTO = modelMapper.map(doctor, DoctorProfileDTO.class);
        
        // Calculate statistics
        profileDTO.setTotalAppointments(appointmentRepository.countByDoctorId(doctorId));
        profileDTO.setTotalPatients(patientRepository.countDistinctByDoctorId(doctorId));
        
        // Get average rating
        Double avgRating = reviewRepository.getAverageRatingByDoctorId(doctorId);
        profileDTO.setRating(avgRating != null ? avgRating : 0.0);
        
        return profileDTO;
    }
    
    @Override
    public DoctorProfileDTO updateDoctorProfile(Long doctorId, DoctorProfileDTO profileDTO) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(DOCTOR_NOT_FOUND_MSG + doctorId));
                
        // Update basic info
        doctor.setName(profileDTO.getName());
        doctor.setFullName(profileDTO.getFullName());
        doctor.setMobile(profileDTO.getMobile());
        doctor.setClinicName(profileDTO.getClinicName());
        doctor.setClinicAddress(profileDTO.getClinicAddress());
        doctor.setLocation(profileDTO.getLocation());
        doctor.setConsultationFee(profileDTO.getConsultationFee());
        doctor.setAbout(profileDTO.getAbout());
        doctor.setSpecialty(profileDTO.getSpecialty());
        doctor.setExperience(profileDTO.getExperience());
        doctor.setEducation(profileDTO.getEducation());
        doctor.setAwards(profileDTO.getAwards());
        doctor.setMemberships(profileDTO.getMemberships());
        
        Doctor updatedDoctor = doctorRepository.save(doctor);
        return modelMapper.map(updatedDoctor, DoctorProfileDTO.class);
    }
    
    @Override
    public String updateDoctorPassword(Long doctorId, String currentPassword, String newPassword) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(DOCTOR_NOT_FOUND_MSG + doctorId));
                
        if (!passwordEncoder.matches(currentPassword, doctor.getPassword())) {
            throw new AuthenticationException("Current password is incorrect");
        }
        
        doctor.setPassword(passwordEncoder.encode(newPassword));
        doctorRepository.save(doctor);
        
        return "Password updated successfully";
    }
    
    @Override
    public String uploadDocument(Long doctorId, String documentType, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(DOCTOR_NOT_FOUND_MSG + doctorId));
                
        String fileName = "doctor_" + doctorId + "_" + documentType.toLowerCase() + "_" + 
                         System.currentTimeMillis() + "." + getFileExtension(file.getOriginalFilename());
        
        Path uploadPath = Paths.get(uploadDir, "doctors", doctorId.toString());
        Files.createDirectories(uploadPath);
        
        try (var inputStream = file.getInputStream()) {
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            
            String fileUrl = "/uploads/doctors/" + doctorId + "/" + fileName;
            
            // Update doctor's document path based on type
            switch(documentType.toUpperCase()) {
                case "DEGREE":
                    doctor.setDegreeCertPath(fileUrl);
                    break;
                case "LICENSE":
                    doctor.setLicensePath(fileUrl);
                    break;
                case "ID_PROOF":
                    doctor.setIdProofPath(fileUrl);
                    break;
                case "PHOTO":
                    doctor.setPhotoPath(fileUrl);
                    break;
                default:
                    throw new IllegalArgumentException(INVALID_DOCUMENT_TYPE_MSG);
            }
            
            doctorRepository.save(doctor);
            return fileUrl;
        }
    }
    
    private String getFileExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int lastDot = fileName.lastIndexOf('.');
        return lastDot == -1 ? "" : fileName.substring(lastDot + 1);
    }
    
    @Override
    public Map<String, Object> getDashboardStats(Long doctorId) {
        Map<String, Object> stats = new HashMap<>();
        
        // Today's appointments
        List<Appointment> todaysAppointments = appointmentRepository
                .findByDoctorIdAndAppointmentDateBetween(
                    doctorId, 
                    LocalDateTime.now().withHour(0).withMinute(0),
                    LocalDateTime.now().withHour(23).withMinute(59)
                );
        
        // Upcoming appointments
        Long upcomingAppointments = appointmentRepository
                .countByDoctorIdAndAppointmentDateAfter(doctorId, LocalDateTime.now());
        
        // Total patients
        Long totalPatients = patientRepository.countDistinctByDoctorId(doctorId);
        
        // Total earnings
        Double totalEarnings = paymentRepository.getTotalEarningsByDoctorId(doctorId);
        
        // Unread notifications
        Long unreadNotifications = notificationRepository.countByUserIdAndIsReadFalse(doctorId);
        
        // Recent reviews
        List<Review> recentReviews = reviewRepository
                .findTop5ByDoctorIdOrderByCreatedAtDesc(doctorId);
        
        // Next appointment
        Optional<Appointment> nextAppointment = appointmentRepository
                .findTopByDoctorIdAndAppointmentDateAfterOrderByAppointmentDateAsc(
                    doctorId, LocalDateTime.now());
        
        stats.put("todaysAppointments", todaysAppointments.size());
        stats.put("upcomingAppointments", upcomingAppointments);
        stats.put("totalPatients", totalPatients);
        stats.put("totalEarnings", totalEarnings != null ? totalEarnings : 0.0);
        stats.put("unreadNotifications", unreadNotifications);
        
        // Map reviews to DTOs if needed
        stats.put("recentReviews", recentReviews.stream()
                .map(r -> modelMapper.map(r, ReviewDTO.class))
                .collect(Collectors.toList()));
                
        stats.put("nextAppointment", nextAppointment
                .map(a -> modelMapper.map(a, AppointmentDTO.class))
                .orElse(null));
                
        return stats;
    }
    
    // Implementation of other required methods from DoctorService interface
    // These are placeholders - implement them based on your requirements
    
    @Override
    public byte[] getDocument(Long doctorId, String documentType) throws IOException {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(DOCTOR_NOT_FOUND_MSG + doctorId));
        
        String filePath = null;
        switch(documentType.toUpperCase()) {
            case "DEGREE":
                filePath = doctor.getDegreeCertPath();
                break;
            case "LICENSE":
                filePath = doctor.getLicensePath();
                break;
            case "ID_PROOF":
                filePath = doctor.getIdProofPath();
                break;
            case "PHOTO":
                filePath = doctor.getPhotoPath();
                break;
            default:
                throw new IllegalArgumentException(INVALID_DOCUMENT_TYPE_MSG);
        }
        
        if (filePath == null || filePath.isEmpty()) {
            throw new ResourceNotFoundException("Document not found");
        }
        
        Path path = Paths.get(uploadDir).resolve(filePath.replaceFirst("^/uploads/", ""));
        return Files.readAllBytes(path);
    }
    
    @Override
    public void deleteDocument(Long doctorId, String documentType) throws IOException {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(DOCTOR_NOT_FOUND_MSG + doctorId));
        
        String filePath = null;
        switch(documentType.toUpperCase()) {
            case "DEGREE":
                filePath = doctor.getDegreeCertPath();
                doctor.setDegreeCertPath(null);
                break;
            case "LICENSE":
                filePath = doctor.getLicensePath();
                doctor.setLicensePath(null);
                break;
            case "ID_PROOF":
                filePath = doctor.getIdProofPath();
                doctor.setIdProofPath(null);
                break;
            case "PHOTO":
                filePath = doctor.getPhotoPath();
                doctor.setPhotoPath(null);
                break;
            default:
                throw new IllegalArgumentException(INVALID_DOCUMENT_TYPE_MSG);
        }
        
        if (filePath != null && !filePath.isEmpty()) {
            try {
                Path path = Paths.get(uploadDir).resolve(filePath.replaceFirst("^/uploads/", ""));
                Files.deleteIfExists(path);
            } catch (NoSuchFileException e) {
                // File doesn't exist, which is fine
            }
        }
        
        doctorRepository.save(doctor);
    }
    
    @Override
    public boolean requestVerification(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(DOCTOR_NOT_FOUND_MSG + doctorId));
        
        // Check if all required documents are uploaded
        if (doctor.getDegreeCertPath() == null || doctor.getLicensePath() == null || 
            doctor.getIdProofPath() == null) {
            throw new IllegalStateException("Please upload all required documents before verification");
        }
        
        doctor.setVerificationRequested(true);
        doctor.setVerificationStatus("PENDING");
        doctorRepository.save(doctor);
        
        // TODO: Notify admin about verification request
        
        return true;
    }
    
    @Override
    public boolean isProfileComplete(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(DOCTOR_NOT_FOUND_MSG + doctorId));
                
        return doctor.getFullName() != null && !doctor.getFullName().isEmpty() &&
               doctor.getEmail() != null && !doctor.getEmail().isEmpty() &&
               doctor.getMobile() != null && !doctor.getMobile().isEmpty() &&
               doctor.getSpecialty() != null && !doctor.getSpecialty().isEmpty() &&
               doctor.getDegree() != null && !doctor.getDegree().isEmpty() &&
               doctor.getClinicName() != null && !doctor.getClinicName().isEmpty() &&
               doctor.getConsultationFee() != null && doctor.getConsultationFee() > 0 &&
               doctor.getDegreeCertPath() != null && !doctor.getDegreeCertPath().isEmpty() &&
               doctor.getLicensePath() != null && !doctor.getLicensePath().isEmpty() &&
               doctor.getIdProofPath() != null && !doctor.getIdProofPath().isEmpty();
    }
    
    @Override
    public boolean updateConsultationFee(Long doctorId, double fee) {
        if (fee <= 0) {
            throw new IllegalArgumentException("Consultation fee must be greater than 0");
        }
        
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(DOCTOR_NOT_FOUND_MSG + doctorId));
                
        doctor.setConsultationFee(fee);
        doctorRepository.save(doctor);
        return true;
    }
    
    @Override
    public boolean updateAboutSection(Long doctorId, String about) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(DOCTOR_NOT_FOUND_MSG + doctorId));
                
        doctor.setAbout(about);
        doctorRepository.save(doctor);
        return true;
    }
    
    @Override
    public boolean updateOnlineStatus(Long doctorId, boolean isOnline) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(DOCTOR_NOT_FOUND_MSG + doctorId));
                
        doctor.setOnline(isOnline);
        doctor.setLastActive(LocalDateTime.now());
        doctorRepository.save(doctor);
        return true;
    }
    
    @Override
    public boolean isDoctorAvailable(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(DOCTOR_NOT_FOUND_MSG + doctorId));
                
        if (!doctor.isActive() || !doctor.isApproved() || !doctor.isOnline()) {
            return false;
        }
        
        // Check if doctor is available now based on their schedule
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek dayOfWeek = now.getDayOfWeek();
        LocalTime currentTime = now.toLocalTime();
        
        // Check if current time is within any availability slot
        return availabilityRepository.existsByDoctorIdAndDayOfWeekAndStartTimeBeforeAndEndTimeAfterAndIsAvailableTrue(
            doctorId, dayOfWeek, currentTime, currentTime);
    }
    
    @Override
    public List<DoctorProfileDTO> searchDoctors(String query, String specialty, String location, 
                                              Double minRating, Integer minExperience) {
        // Implement search logic based on parameters
        // This is a simplified implementation - adjust based on your requirements
        
        List<Doctor> doctors;
        
        if (query != null && !query.isEmpty()) {
            // Search by name, specialty, or location
            doctors = doctorRepository.searchDoctors(query, specialty, location);
        } else {
            // Filter by criteria
            doctors = doctorRepository.findByCriteria(specialty, location, minRating, minExperience);
        }
        
        // Filter out inactive or unapproved doctors
        doctors = doctors.stream()
                .filter(Doctor::isActive)
                .filter(Doctor::isApproved)
                .collect(Collectors.toList());
        
        // Map to DTOs
        return doctors.stream()
                .map(doctor -> modelMapper.map(doctor, DoctorProfileDTO.class))
                .collect(Collectors.toList());
    }
    
    // Admin methods
    @Override
    public boolean approveDoctor(Long doctorId, Long adminId, String comments) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(DOCTOR_NOT_FOUND_MSG + doctorId));
                
        doctor.setApproved(true);
        doctor.setApprovedBy(adminId);
        doctor.setApprovalDate(LocalDateTime.now());
        doctor.setApprovalComments(comments);
        
        // TODO: Send notification to doctor about approval
        
        doctorRepository.save(doctor);
        return true;
    }
    
    @Override
    public boolean rejectDoctor(Long doctorId, Long adminId, String reason) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(DOCTOR_NOT_FOUND_MSG + doctorId));
                
        doctor.setApproved(false);
        doctor.setApprovalComments(reason);
        
        // TODO: Send notification to doctor about rejection with reason
        
        doctorRepository.save(doctor);
        return true;
    }
    
    @Override
    public List<DoctorProfileDTO> getPendingApprovals() {
        return doctorRepository.findByApprovedFalseAndVerificationRequestedTrue().stream()
                .map(doctor -> modelMapper.map(doctor, DoctorProfileDTO.class))
                .collect(Collectors.toList());
    }
    
    @Override
    public Map<String, Object> getAppointmentAnalytics(Long doctorId, String period) {
        // Implement appointment analytics based on period (day, week, month, year)
        // This is a simplified implementation
        
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate;
        
        switch (period.toUpperCase()) {
            case "WEEK":
                startDate = endDate.minusWeeks(1);
                break;
            case "MONTH":
                startDate = endDate.minusMonths(1);
                break;
            case "YEAR":
                startDate = endDate.minusYears(1);
                break;
            case "DAY":
            default:
                startDate = endDate.minusDays(1);
        }
        
        // Get appointments in the period
        List<Appointment> appointments = appointmentRepository
                .findByDoctorIdAndAppointmentDateBetween(doctorId, startDate, endDate);
        
        // Group by status
        Map<String, Long> statusCount = appointments.stream()
                .collect(Collectors.groupingBy(Appointment::getStatus, Collectors.counting()));
        
        // Calculate completion rate
        long totalAppointments = appointments.size();
        long completedAppointments = statusCount.getOrDefault("COMPLETED", 0L);
        double completionRate = totalAppointments > 0 ? 
                (double) completedAppointments / totalAppointments * 100 : 0;
        
        // Calculate average rating
        Double avgRating = reviewRepository.getAverageRatingByDoctorId(doctorId);
        
        // Prepare response
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalAppointments", totalAppointments);
        analytics.put("completedAppointments", completedAppointments);
        analytics.put("cancelledAppointments", statusCount.getOrDefault("CANCELLED", 0L));
        analytics.put("noShowAppointments", statusCount.getOrDefault("NO_SHOW", 0L));
        analytics.put("completionRate", Math.round(completionRate * 100) / 100.0);
        analytics.put("averageRating", avgRating != null ? avgRating : 0.0);
        analytics.put("periodStart", startDate);
        analytics.put("periodEnd", endDate);
        
        return analytics;
    }
    
    @Override
    public Map<String, Object> getEarningsAnalytics(Long doctorId, String period) {
        // Implement earnings analytics based on period (day, week, month, year)
        // This is a simplified implementation
        
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate;
        
        switch (period.toUpperCase()) {
            case "WEEK":
                startDate = endDate.minusWeeks(1);
                break;
            case "MONTH":
                startDate = endDate.minusMonths(1);
                break;
            case "YEAR":
                startDate = endDate.minusYears(1);
                break;
            case "DAY":
            default:
                startDate = endDate.minusDays(1);
        }
        
        // Get payments in the period
        List<Payment> payments = paymentRepository
                .findByDoctorIdAndPaymentDateBetween(doctorId, startDate, endDate);
        
        // Calculate total earnings
        double totalEarnings = payments.stream()
                .mapToDouble(Payment::getAmount)
                .sum();
        
        // Group by status
        Map<String, Double> earningsByStatus = payments.stream()
                .collect(Collectors.groupingBy(Payment::getStatus,
                         Collectors.summingDouble(Payment::getAmount)));
        
        // Prepare response
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalEarnings", totalEarnings);
        analytics.put("earningsByStatus", earningsByStatus);
        analytics.put("totalTransactions", payments.size());
        analytics.put("periodStart", startDate);
        analytics.put("periodEnd", endDate);
        
        return analytics;
    }
    
    @Override
    public boolean updateNotificationPreferences(Long doctorId, Map<String, Boolean> preferences) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(DOCTOR_NOT_FOUND_MSG + doctorId));
                
        // Update notification preferences
        if (doctor.getNotificationPreferences() == null) {
            doctor.setNotificationPreferences(new HashMap<>());
        }
        
        preferences.forEach((key, value) -> 
            doctor.getNotificationPreferences().put(key, value));
            
        doctorRepository.save(doctor);
        return true;
    }
    
    @Override
    public Map<String, Boolean> getNotificationPreferences(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(DOCTOR_NOT_FOUND_MSG + doctorId));
                
        if (doctor.getNotificationPreferences() == null) {
            // Return default preferences if none set
            Map<String, Boolean> defaultPrefs = new HashMap<>();
            defaultPrefs.put("appointmentBooked", true);
            defaultPrefs.put("appointmentCancelled", true);
            defaultPrefs.put("appointmentReminder", true);
            defaultPrefs.put("newReview", true);
            defaultPrefs.put("promotions", true);
            return defaultPrefs;
        }
        
        return doctor.getNotificationPreferences();
    }
    
    @Override
    public boolean deactivateAccount(Long doctorId, String reason) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(DOCTOR_NOT_FOUND_MSG + doctorId));
                
        doctor.setActive(false);
        doctor.setDeactivationReason(reason);
        doctor.setDeactivationDate(LocalDateTime.now());
        
        // Cancel all upcoming appointments
        List<Appointment> upcomingAppointments = appointmentRepository
                .findByDoctorIdAndAppointmentDateAfter(doctorId, LocalDateTime.now());
                
        for (Appointment appointment : upcomingAppointments) {
            appointment.setStatus(AppointmentStatus.CANCELLED);
            appointment.setCancellationReason("Doctor account deactivated: " + reason);
            appointment.setCancelledBy(doctorId);
            appointment.setCancelledAt(LocalDateTime.now());
            
            // TODO: Notify patients about cancelled appointments
        }
        
        appointmentRepository.saveAll(upcomingAppointments);
        doctorRepository.save(doctor);
        
        return true;
    }
    
    @Override
    public boolean deleteAccount(Long doctorId, String password) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(DOCTOR_NOT_FOUND_MSG + doctorId));
                
        if (!passwordEncoder.matches(password, doctor.getPassword())) {
            throw new AuthenticationException("Invalid password");
        }
        
        // Delete all associated data (in a real app, you might want to anonymize instead of deleting)
        appointmentRepository.deleteByDoctorId(doctorId);
        availabilityRepository.deleteByDoctorId(doctorId);
        notificationRepository.deleteByUserId(doctorId);
        
        // Delete documents from storage
        deleteDocumentIfExists(doctor.getDegreeCertPath());
        deleteDocumentIfExists(doctor.getLicensePath());
        deleteDocumentIfExists(doctor.getIdProofPath());
        deleteDocumentIfExists(doctor.getPhotoPath());
        
        // Finally, delete the doctor
        doctorRepository.delete(doctor);
        
        return true;
    }
    
    private void deleteDocumentIfExists(String filePath) {
        if (filePath != null && !filePath.isEmpty()) {
            try {
                Path path = Paths.get(uploadDir).resolve(filePath.replaceFirst("^/uploads/", ""));
                Files.deleteIfExists(path);
            } catch (IOException e) {
                // Log error but don't fail the operation
                System.err.println("Error deleting file: " + filePath + ", " + e.getMessage());
            }
        }
    }
}
