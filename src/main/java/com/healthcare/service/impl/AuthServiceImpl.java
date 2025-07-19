package com.healthcare.service.impl;

import com.healthcare.model.Doctor;
import com.healthcare.model.User;
import com.healthcare.repository.DoctorRepository;
import com.healthcare.repository.UserRepository;
import com.healthcare.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthServiceImpl(DoctorRepository doctorRepository, 
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public Doctor registerDoctor(Doctor registration) {
        // Check if email already exists
        if (doctorRepository.existsByEmail(registration.getEmail())) {
            throw new RuntimeException("Email is already in use");
        }

        // Map DTO to Doctor entity
        Doctor doctor = new Doctor();
        doctor.setFullName(registration.getFullName());
        doctor.setName(registration.getName());
        doctor.setEmail(registration.getEmail());
        doctor.setMobile(registration.getMobile());
        doctor.setPassword(passwordEncoder.encode(registration.getPassword()));
        doctor.setRegistrationNumber(registration.getRegistrationNumber());
        doctor.setCouncil(registration.getCouncil());
        doctor.setSpecialty(registration.getSpecialty());
        doctor.setExperience(registration.getExperience());
        doctor.setDegree(registration.getDegree());
        doctor.setDegreeCertPath(registration.getDegreeCertPath());
        doctor.setClinicName(registration.getClinicName());
        doctor.setClinicAddress(registration.getClinicAddress());
        doctor.setLocation(registration.getLocation());
        doctor.setIdProofPath(registration.getIdProofPath());
        doctor.setLicensePath(registration.getLicensePath());
        doctor.setPhotoPath(registration.getPhotoPath());
        doctor.setApproved(registration.isApproved());
        doctor.setCreatedAt(LocalDateTime.now());
        doctor.setUpdatedAt(LocalDateTime.now());

        return doctorRepository.save(doctor);
    }

    @Override
    @Transactional
    public User registerPatient(User registration) {
        // Check if email already exists
        if (userRepository.existsByEmail(registration.getEmail())) {
            throw new RuntimeException("Email is already in use");
        }

        // Map DTO to User entity
        User user = new User();
        user.setFullName(registration.getFullName());
        user.setEmail(registration.getEmail());
        user.setMobile(registration.getMobile());
        user.setPassword(passwordEncoder.encode(registration.getPassword()));
        user.setDateOfBirth(registration.getDateOfBirth());
        user.setGender(registration.getGender());
        user.setBloodGroup(registration.getBloodGroup());
        user.setAddress(registration.getAddress());
        user.setEmergencyContact(registration.getEmergencyContact());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }
}
