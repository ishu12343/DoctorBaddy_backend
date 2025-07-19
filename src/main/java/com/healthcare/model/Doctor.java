package com.healthcare.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString(exclude = {"password"})
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String mobile;
    
    @Column(nullable = false)
    private String password;

    @Column(unique = true)
    private String registrationNumber;
    
    private String council;
    private String specialty;
    private int experience;
    private String degree;

    private String clinicName;
    
    @Column(length = 1000)
    private String clinicAddress;

    private String idProofPath;
    private String licensePath;
    private String degreeCertPath;
    private String photoPath;
    
    @Builder.Default
    private boolean approved = false;
}