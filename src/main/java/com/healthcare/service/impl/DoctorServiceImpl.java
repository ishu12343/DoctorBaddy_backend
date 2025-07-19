package com.healthcare.service.impl;

import com.healthcare.exception.AuthenticationException;
import com.healthcare.model.Doctor;
import com.healthcare.repository.DoctorRepository;
import com.healthcare.security.JwtUtil;
import com.healthcare.service.DoctorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {
    private final DoctorRepository doctorRepository;

    public DoctorServiceImpl(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

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

//	public Doctor login(Doctor loginRequest) {
//	        Doctor doctor = doctorRepository.findByEmail(loginRequest.getEmail());
//	        if (doctor == null) {
//	            throw new ResourceNotFoundException("Doctor with this email does not exist.");
//	        }
//
//	        if (!doctor.getPassword().equals(loginRequest.getPassword())) {
//	            throw new AuthenticationException("Invalid credentials.");
//	        }
//
//	        if (!doctor.isApproved()) {
//	            throw new AuthenticationException("Doctor account not approved yet.");
//	        }
//
//	        return doctor;
//	    }
    
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public String login(Doctor loginRequest) {
        Doctor doctor = doctorRepository.findByEmail(loginRequest.getEmail());
        if (doctor == null || !doctor.getPassword().equals(loginRequest.getPassword())) {
            throw new AuthenticationException("Invalid credentials");
        }

        return jwtUtil.generateToken(doctor.getEmail());
    }

}