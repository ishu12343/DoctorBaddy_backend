package com.healthcare.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Doctor entity representing a medical professional in the system.
 */
@Entity
@Table(name = "doctors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"password"})
public class Doctor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false)
    @JsonProperty("full_name")
    private String fullName;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String mobile;
    
    @Column(nullable = false)
    private String password;
    
    private String gender;
    private String location;
    
    @Column(name = "registration_number", unique = true)
    @JsonProperty("registration_number")
    private String registrationNumber;
    
    @Column(name = "license_number")
    @JsonProperty("license_number")
    private String licenseNumber;
    
    private String council;
    private String degree;
    private String specialty;
    private String experience;
    @JsonProperty("clinic_name")
    private String clinicName;
    
    @Column(name = "clinic_address", length = 1000)
    @JsonProperty("clinic_address")
    private String clinicAddress;
    
    @Column(name = "profile_photo", columnDefinition = "TEXT")
    @JsonProperty("profile_photo")
    private String profilePhoto;
    
    @Column(nullable = false)
    private String role = "DOCTOR";
    
    @Column(name = "dob")
    private String dob;
    
    @Column(name = "blood_group")
    @JsonProperty("blood_group")
    private String bloodGroup;
    
    @Column(name = "available_days")
    @JsonProperty("available_days")
    private String availableDays;
    
    @Column(name = "available_from")
    @JsonProperty("available_from")
    private String availableFrom;
    
    @Column(name = "available_to")
    @JsonProperty("available_to")
    private String availableTo;
    
    private String city;
    private String state;
    
    @Column(name = "zip_code")
    @JsonProperty("zip_code")
    private String zipCode;
    
    private String languages;
    private String status = "ACTIVE";
    private String documents;
    
    @Column(nullable = false)
    private Boolean approved = false;
    
    @Column(nullable = false)
    private Boolean suspended = false;
    
    @Column(name = "reset_otp")
    @JsonProperty("reset_otp")
    private String resetOtp;
    
    @Column(name = "otp_expires_at")
    @JsonProperty("otp_expires_at")
    private LocalDateTime otpExpiresAt;
    
    @Column(name = "created_at", updatable = false)
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
