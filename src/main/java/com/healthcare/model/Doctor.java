package com.healthcare.model;

import javax.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Doctor entity representing a medical professional in the system.
 */
@Entity
@Table(name = "doctors")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@ToString(exclude = {"password"})
public class Doctor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;
    
    private String name; // Preferred name
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String mobile;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String role; // DOCTOR, ADMIN

    @Column(unique = true)
    private String registrationNumber;
    
    private String council;
    private String specialty;
    private int experience;
    private String degree;
    private String degreeCertPath;
    private String clinicName;
    
    @Column(length = 1000)
    private String clinicAddress;
    
    private String location;
    private String idProofPath;
    private String licensePath;
    private String photoPath;
    
    @Builder.Default
    private boolean approved = false;
    
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
