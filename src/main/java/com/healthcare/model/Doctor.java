package com.healthcare.model;

import jakarta.persistence.*;
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
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRegistrationNumber() {
		return registrationNumber;
	}

	public void setRegistrationNumber(String registrationNumber) {
		this.registrationNumber = registrationNumber;
	}

	public String getCouncil() {
		return council;
	}

	public void setCouncil(String council) {
		this.council = council;
	}

	public String getSpecialty() {
		return specialty;
	}

	public void setSpecialty(String specialty) {
		this.specialty = specialty;
	}

	public int getExperience() {
		return experience;
	}

	public void setExperience(int experience) {
		this.experience = experience;
	}

	public String getDegree() {
		return degree;
	}

	public void setDegree(String degree) {
		this.degree = degree;
	}

	public String getDegreeCertPath() {
		return degreeCertPath;
	}

	public void setDegreeCertPath(String degreeCertPath) {
		this.degreeCertPath = degreeCertPath;
	}

	public String getClinicName() {
		return clinicName;
	}

	public void setClinicName(String clinicName) {
		this.clinicName = clinicName;
	}

	public String getClinicAddress() {
		return clinicAddress;
	}

	public void setClinicAddress(String clinicAddress) {
		this.clinicAddress = clinicAddress;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getIdProofPath() {
		return idProofPath;
	}

	public void setIdProofPath(String idProofPath) {
		this.idProofPath = idProofPath;
	}

	public String getLicensePath() {
		return licensePath;
	}

	public void setLicensePath(String licensePath) {
		this.licensePath = licensePath;
	}

	public String getPhotoPath() {
		return photoPath;
	}

	public void setPhotoPath(String photoPath) {
		this.photoPath = photoPath;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public boolean isApproved() {
		return approved;
	}

	public void setApproved(boolean approved) {
		this.approved = approved;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Doctor(Long id, String fullName, String name, String email, String mobile, String password,
			String registrationNumber, String council, String specialty, int experience, String degree,
			String degreeCertPath, String clinicName, String clinicAddress, String location, String idProofPath,
			String licensePath, String photoPath, boolean approved, LocalDateTime createdAt, LocalDateTime updatedAt) {
		super();
		this.id = id;
		this.fullName = fullName;
		this.name = name;
		this.email = email;
		this.mobile = mobile;
		this.password = password;
		this.registrationNumber = registrationNumber;
		this.council = council;
		this.specialty = specialty;
		this.experience = experience;
		this.degree = degree;
		this.degreeCertPath = degreeCertPath;
		this.clinicName = clinicName;
		this.clinicAddress = clinicAddress;
		this.location = location;
		this.idProofPath = idProofPath;
		this.licensePath = licensePath;
		this.photoPath = photoPath;
		this.approved = approved;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public Doctor() {
		super();
		// TODO Auto-generated constructor stub
	}
    
}
