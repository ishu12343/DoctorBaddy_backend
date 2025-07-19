package com.healthcare.service.impl;

import com.healthcare.dto.DoctorProfileDTO;
import com.healthcare.exception.AuthenticationException;
import com.healthcare.exception.ResourceNotFoundException;
import com.healthcare.model.Appointment;
import com.healthcare.model.Doctor;
import com.healthcare.model.Notification;
import com.healthcare.model.Review;
import com.healthcare.repository.*;
import com.healthcare.security.JwtUtil;
import com.healthcare.service.DoctorService;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {
    private static final String DOCTOR_NOT_FOUND_MSG = "Doctor not found with id: ";
    
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final ReviewRepository reviewRepository;
    private final AvailabilityRepository availabilityRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    
    @Value("${file.upload-dir}")
    private String uploadDir;
    
    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    public Doctor saveDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    @Override
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }
    @Override
    public Doctor registerDoctor(Doctor doctor, MultipartFile idProof, MultipartFile license,
                                 MultipartFile degreeCert, MultipartFile photo) throws IOException {

        String basePath = "uploads/" + doctor.getEmail().replaceAll("[^a-zA-Z0-9]", "_");
        new File(basePath).mkdirs();

        doctor.setIdProofPath(saveFile(basePath, idProof));
        doctor.setLicensePath(saveFile(basePath, license));
        doctor.setDegreeCertPath(saveFile(basePath, degreeCert));
        doctor.setPhotoPath(saveFile(basePath, photo));

        return doctorRepository.save(doctor);
    }

    private String saveFile(String basePath, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;
        Path path = Path.of(basePath, file.getOriginalFilename());
        Files.write(path, file.getBytes());
        return path.toString();
    }

    @Override
    public String login(Doctor loginRequest) {
            throw new AuthenticationException("Password is required");
        }
        
        // Use Optional to handle the case where doctor might not be found
        Doctor doctor = doctorRepository.findByEmail(loginRequest.getEmail())
            .orElseThrow(() -> {
                System.err.println("Login failed: No doctor found with email: " + loginRequest.getEmail());
                return new AuthenticationException("Invalid email or password");
            });
        
        if (!doctor.isApproved()) {
            System.err.println("Login failed: Doctor account not approved: " + loginRequest.getEmail());
            throw new AuthenticationException("Your account is pending approval. Please contact support.");
        }
        
        if (!doctor.getPassword().equals(loginRequest.getPassword())) {
            System.err.println("Login failed: Invalid password for email: " + loginRequest.getEmail());
            throw new AuthenticationException("Invalid email or password");
        }
        
        String token = jwtUtil.generateToken(doctor.getEmail());
        System.out.println("Login successful, generated token for: " + loginRequest.getEmail());
        return token;
    }

}