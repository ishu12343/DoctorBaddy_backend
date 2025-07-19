package com.healthcare.controller;

import com.healthcare.dto.*;
import com.healthcare.model.Appointment;
import com.healthcare.model.Notification;
import com.healthcare.model.Review;
import com.healthcare.security.JwtUtil;
import com.healthcare.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controller for handling doctor dashboard related operations.
 * All endpoints are prefixed with /api/doctors/dashboard
 */
@RestController
@RequestMapping("/api/doctors/dashboard")
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorDashboardController {

    private final DoctorService doctorService;
    private final JwtUtil jwtUtil;

    @Autowired
    public DoctorDashboardController(DoctorService doctorService, JwtUtil jwtUtil) {
        this.doctorService = doctorService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Helper method to get the current doctor's ID from the JWT token
     */
    private Long getCurrentDoctorId(HttpServletRequest request) {
        final String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            return jwtUtil.extractUserId(token);
        }
        throw new SecurityException("Invalid or missing JWT token");
    }

    // ==================== Dashboard ====================

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats(HttpServletRequest request) {
        Long doctorId = getCurrentDoctorId(request);
        return ResponseEntity.ok(doctorService.getDashboardStats(doctorId));
    }

    // ==================== Profile Management ====================

    @GetMapping("/profile")
    public ResponseEntity<DoctorProfileDTO> getProfile(HttpServletRequest request) {
        Long doctorId = getCurrentDoctorId(request);
        return ResponseEntity.ok(doctorService.getDoctorProfile(doctorId));
    }

    @PutMapping("/profile")
    public ResponseEntity<DoctorProfileDTO> updateProfile(
            HttpServletRequest request,
            @RequestBody DoctorProfileDTO profileDTO) {
        Long doctorId = getCurrentDoctorId(request);
        return ResponseEntity.ok(doctorService.updateDoctorProfile(doctorId, profileDTO));
    }

    @PostMapping("/profile/password")
    public ResponseEntity<String> updatePassword(
            HttpServletRequest request,
            @RequestParam String currentPassword,
            @RequestParam String newPassword) {
        Long doctorId = getCurrentDoctorId(request);
        return ResponseEntity.ok(doctorService.updateDoctorPassword(doctorId, currentPassword, newPassword));
    }

    @PostMapping("/documents/upload")
    public ResponseEntity<String> uploadDocument(
            HttpServletRequest request,
            @RequestParam String documentType,
            @RequestParam("file") MultipartFile file) throws IOException {
        Long doctorId = getCurrentDoctorId(request);
        return ResponseEntity.ok(doctorService.uploadDocument(doctorId, documentType, file));
    }

    // ==================== Appointment Management ====================

    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentDTO>> getAppointments(
            HttpServletRequest request,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String status) {
        Long doctorId = getCurrentDoctorId(request);
        
        if (date != null) {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(23, 59, 59);
            return ResponseEntity.ok(doctorService.getAppointmentsByDate(doctorId, startOfDay, endOfDay));
        } else if (status != null) {
            return ResponseEntity.ok(doctorService.getAppointmentsByStatus(doctorId, status));
        } else {
            return ResponseEntity.ok(doctorService.getUpcomingAppointments(doctorId));
        }
    }

    @PutMapping("/appointments/{appointmentId}/status")
    public ResponseEntity<AppointmentDTO> updateAppointmentStatus(
            HttpServletRequest request,
            @PathVariable Long appointmentId,
            @RequestParam String status,
            @RequestParam(required = false) String reason) {
        Long doctorId = getCurrentDoctorId(request);
        return ResponseEntity.ok(doctorService.updateAppointmentStatus(doctorId, appointmentId, status, reason));
    }

    @PostMapping("/appointments/{appointmentId}/reschedule")
    public ResponseEntity<AppointmentDTO> rescheduleAppointment(
            HttpServletRequest request,
            @PathVariable Long appointmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newDateTime) {
        Long doctorId = getCurrentDoctorId(request);
        return ResponseEntity.ok(doctorService.rescheduleAppointment(doctorId, appointmentId, newDateTime));
    }

    // ==================== Availability Management ====================

    @GetMapping("/availability")
    public ResponseEntity<List<AvailabilityDTO>> getAvailability(HttpServletRequest request) {
        Long doctorId = getCurrentDoctorId(request);
        return ResponseEntity.ok(doctorService.getDoctorAvailability(doctorId));
    }

    @PostMapping("/availability")
    public ResponseEntity<AvailabilityDTO> addAvailability(
            HttpServletRequest request,
            @RequestBody AvailabilityDTO availabilityDTO) {
        Long doctorId = getCurrentDoctorId(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(doctorService.addAvailability(doctorId, availabilityDTO));
    }

    @PutMapping("/availability/{availabilityId}")
    public ResponseEntity<AvailabilityDTO> updateAvailability(
            HttpServletRequest request,
            @PathVariable Long availabilityId,
            @RequestBody AvailabilityDTO availabilityDTO) {
        Long doctorId = getCurrentDoctorId(request);
        availabilityDTO.setId(availabilityId);
        return ResponseEntity.ok(doctorService.updateAvailability(doctorId, availabilityDTO));
    }

    @DeleteMapping("/availability/{availabilityId}")
    public ResponseEntity<Void> deleteAvailability(
            HttpServletRequest request,
            @PathVariable Long availabilityId) {
        Long doctorId = getCurrentDoctorId(request);
        doctorService.deleteAvailability(doctorId, availabilityId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Patient Management ====================

    @GetMapping("/patients")
    public ResponseEntity<List<PatientDTO>> getPatients(HttpServletRequest request) {
        Long doctorId = getCurrentDoctorId(request);
        return ResponseEntity.ok(doctorService.getDoctorPatients(doctorId));
    }

    @GetMapping("/patients/{patientId}")
    public ResponseEntity<PatientDTO> getPatientDetails(
            HttpServletRequest request,
            @PathVariable Long patientId) {
        Long doctorId = getCurrentDoctorId(request);
        return ResponseEntity.ok(doctorService.getPatientDetails(doctorId, patientId));
    }

    // ==================== Reviews & Ratings ====================

    @GetMapping("/reviews")
    public ResponseEntity<List<ReviewDTO>> getReviews(
            HttpServletRequest request,
            @RequestParam(required = false) Integer minRating) {
        Long doctorId = getCurrentDoctorId(request);
        if (minRating != null) {
            return ResponseEntity.ok(doctorService.getReviewsByRating(doctorId, minRating));
        }
        return ResponseEntity.ok(doctorService.getDoctorReviews(doctorId));
    }

    @PostMapping("/reviews/{reviewId}/respond")
    public ResponseEntity<ReviewDTO> respondToReview(
            HttpServletRequest request,
            @PathVariable Long reviewId,
            @RequestParam String response) {
        Long doctorId = getCurrentDoctorId(request);
        return ResponseEntity.ok(doctorService.respondToReview(doctorId, reviewId, response));
    }

    // ==================== Notifications ====================

    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationDTO>> getNotifications(
            HttpServletRequest request,
            @RequestParam(required = false) Boolean unreadOnly) {
        Long doctorId = getCurrentDoctorId(request);
        if (unreadOnly != null && unreadOnly) {
            return ResponseEntity.ok(doctorService.getUnreadNotifications(doctorId));
        }
        return ResponseEntity.ok(doctorService.getAllNotifications(doctorId));
    }

    @PutMapping("/notifications/{notificationId}/read")
    public ResponseEntity<Void> markNotificationAsRead(
            HttpServletRequest request,
            @PathVariable Long notificationId) {
        Long doctorId = getCurrentDoctorId(request);
        doctorService.markNotificationAsRead(doctorId, notificationId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/notifications/read-all")
    public ResponseEntity<Void> markAllNotificationsAsRead(HttpServletRequest request) {
        Long doctorId = getCurrentDoctorId(request);
        doctorService.markAllNotificationsAsRead(doctorId);
        return ResponseEntity.ok().build();
    }

    // ==================== Settings ====================

    @PutMapping("/settings/notifications")
    public ResponseEntity<Void> updateNotificationPreferences(
            HttpServletRequest request,
            @RequestBody Map<String, Boolean> preferences) {
        Long doctorId = getCurrentDoctorId(request);
        doctorService.updateNotificationPreferences(doctorId, preferences);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/settings/notifications")
    public ResponseEntity<Map<String, Boolean>> getNotificationPreferences(HttpServletRequest request) {
        Long doctorId = getCurrentDoctorId(request);
        return ResponseEntity.ok(doctorService.getNotificationPreferences(doctorId));
    }

    @PostMapping("/settings/deactivate")
    public ResponseEntity<Void> deactivateAccount(
            HttpServletRequest request,
            @RequestParam String reason) {
        Long doctorId = getCurrentDoctorId(request);
        doctorService.deactivateAccount(doctorId, reason);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/settings/account")
    public ResponseEntity<Void> deleteAccount(
            HttpServletRequest request,
            @RequestParam String password) {
        Long doctorId = getCurrentDoctorId(request);
        doctorService.deleteAccount(doctorId, password);
        return ResponseEntity.noContent().build();
    }

    // ==================== Analytics ====================

    @GetMapping("/analytics/appointments")
    public ResponseEntity<Map<String, Object>> getAppointmentAnalytics(
            HttpServletRequest request,
            @RequestParam(defaultValue = "MONTH") String period) {
        Long doctorId = getCurrentDoctorId(request);
        return ResponseEntity.ok(doctorService.getAppointmentAnalytics(doctorId, period));
    }

    @GetMapping("/analytics/earnings")
    public ResponseEntity<Map<String, Object>> getEarningsAnalytics(
            HttpServletRequest request,
            @RequestParam(defaultValue = "MONTH") String period) {
        Long doctorId = getCurrentDoctorId(request);
        return ResponseEntity.ok(doctorService.getEarningsAnalytics(doctorId, period));
    }
}
