package com.healthcare.service;

import com.healthcare.model.Doctor;

import java.io.IOException;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface DoctorService {
    Doctor saveDoctor(Doctor doctor);
    List<Doctor> getAllDoctors();

    Doctor registerDoctor(Doctor doctor, MultipartFile idProof, MultipartFile license,
                          MultipartFile degreeCert, MultipartFile photo) throws IOException;
    
	String login(Doctor loginRequest);
	
    
}
