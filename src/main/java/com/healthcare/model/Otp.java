package com.healthcare.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * OTP entity for storing registration OTPs
 */
@Entity
@Table(name = "otp_verification")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Otp {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String mobile;
    
    @Column(nullable = false)
    private String otp;
    
    @Column(nullable = false)
    private String userType; // DOCTOR or PATIENT
    
    @Column(nullable = false)
    private Boolean verified = false;
    
    @Column(name = "expires_at", nullable = false)
    @JsonProperty("expiresAt")
    private LocalDateTime expiresAt;
    
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
