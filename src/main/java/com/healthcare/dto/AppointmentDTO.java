package com.healthcare.dto;

import com.healthcare.model.Appointment.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDTO {
    private Long id;
    private Long patientId;
    private String patientName;
    private String patientPhoto;
    private String patientEmail;
    private String patientMobile;
    private Long doctorId;
    private String doctorName;
    private String doctorPhoto;
    private String doctorSpecialty;
    private LocalDateTime appointmentTime;
    private LocalDateTime endTime;
    private AppointmentStatus status;
    private String reason;
    private String notes;
    private String cancellationReason;
    private String meetingLink;
    private boolean isPaid;
    private Double fee;
    private String paymentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean hasMedicalHistory;
    private boolean hasPrescription;
    private boolean hasTestResults;
    private Double rating;
    private String review;
    private String response;
}
