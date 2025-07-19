package com.healthcare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorProfileDTO {
    private Long id;
    private String fullName;
    private String name; // Preferred name
    private String email;
    private String mobile;
    private String registrationNumber;
    private String council;
    private String specialty;
    private int experience;
    private String degree;
    private String degreeCertPath;
    private String clinicName;
    private String clinicAddress;
    private String location;
    private String idProofPath;
    private String licensePath;
    private String photoPath;
    private boolean approved;
    private String role;
    private Double consultationFee;
    private String about;
    private String[] languages;
    private String[] services;
    private String[] education;
    private String[] experienceDetails;
    private String[] awards;
    private String[] memberships;
    private Double rating;
    private int totalReviews;
    private int totalPatients;
    private int totalAppointments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
