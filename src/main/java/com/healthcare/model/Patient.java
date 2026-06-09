package com.healthcare.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Patient entity representing a patient in the system.
 */
@Entity
@Table(name = "patient")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"password"})
public class Patient {
    
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
    
    @Column(name = "date_of_birth")
    @JsonProperty("dateOfBirth")
    private String dateOfBirth;
    
    @Column(name = "blood_group")
    @JsonProperty("bloodGroup")
    private String bloodGroup;
    
    private String address;
    private String city;
    private String state;
    
    @Column(name = "zip")
    private String zip;
    
    private String country;
    private String allergies;
    private String conditions;
    private String medications;
    private String surgeries;
    
    @Column(name = "emergency_contact_name")
    @JsonProperty("emergencyContact")
    private String emergencyContactName;
    
    @Column(name = "emergency_contact_number")
    @JsonProperty("emergency_contact_number")
    private String emergencyContactNumber;
    
    @Column(name = "document_path")
    @JsonProperty("document_path")
    private String documentPath;
    
    @Column(name = "photo_path", columnDefinition = "LONGTEXT")
    @JsonProperty("photo_path")
    private String photoPath;
    
    @Column(nullable = false)
    private String role = "PATIENT";
    
    @Column(name = "is_active", nullable = false)
    @JsonProperty("is_active")
    private Boolean isActive = true;
    
    @Column(nullable = false)
    private Boolean verified = false;
    
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
