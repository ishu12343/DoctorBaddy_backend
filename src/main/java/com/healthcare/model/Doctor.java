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
    @JsonProperty("fullName")
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
    @JsonProperty("registrationNumber")
    private String registrationNumber;
    
    @Column(name = "license_number")
    @JsonProperty("licenseNumber")
    private String licenseNumber;
    
    private String council;
    private String degree;
    private String specialty;
    private String experience;
    
    @Column(name = "degree_cert_path")
    @JsonProperty("degreeCertPath")
    private String degreeCertPath;
    
    @Column(name = "id_proof_path")
    @JsonProperty("idProofPath")
    private String idProofPath;
    
    @Column(name = "license_path")
    @JsonProperty("licensePath")
    private String licensePath;
    
    @Column(name = "photo_path", columnDefinition = "LONGTEXT")
    @JsonProperty("photoPath")
    private String photoPath;
    
    @JsonProperty("clinicName")
    private String clinicName;
    
    @Column(name = "clinic_address", length = 1000)
    @JsonProperty("clinicAddress")
    private String clinicAddress;
    
    @Column(name = "profile_photo", columnDefinition = "TEXT")
    @JsonProperty("profilePhoto")
    private String profilePhoto;
    
    @Column(nullable = false)
    private String role = "DOCTOR";
    
    @Column(name = "dob")
    private String dob;
    
    @Column(name = "blood_group")
    @JsonProperty("bloodGroup")
    private String bloodGroup;
    
    @Column(name = "available_days")
    @JsonProperty("availableDays")
    private String availableDays;
    
    @Column(name = "available_from")
    @JsonProperty("availableFrom")
    private String availableFrom;
    
    @Column(name = "available_to")
    @JsonProperty("availableTo")
    private String availableTo;
    
    private String city;
    private String state;
    
    @Column(name = "zip_code")
    @JsonProperty("zipCode")
    private String zipCode;
    
    private String languages;
    private String status = "ACTIVE";
    private String documents;
    
    @Column(nullable = false)
    private Boolean approved = false;
    
    @Column(nullable = false)
    private Boolean suspended = false;
    
    @Column(name = "reset_otp")
    @JsonProperty("resetOtp")
    private String resetOtp;
    
    @Column(name = "otp_expires_at")
    @JsonProperty("otpExpiresAt")
    private LocalDateTime otpExpiresAt;
    
    @Column(name = "created_at", updatable = false)
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    @JsonProperty("updatedAt")
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
