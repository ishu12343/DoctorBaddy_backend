package com.healthcare.controller;


import com.healthcare.model.Doctor;
import com.healthcare.service.DoctorService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
//@CrossOrigin(origins = "http://localhost:5173")
public class DoctorController {
    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @PostMapping
    public Doctor createDoctor(@RequestBody Doctor doctor) {
        return doctorService.saveDoctor(doctor);
    }

    @GetMapping
    public List<Doctor> getAllDoctors() {
        return doctorService.getAllDoctors();
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerDoctor(@RequestParam("fullName") String fullName,
                                            @RequestParam("email") String email,
                                            @RequestParam("mobile") String mobile,
                                            @RequestParam("password") String password,
                                            @RequestParam("confirmPassword") String confirmPassword,
                                            @RequestParam("registrationNumber") String registrationNumber,
                                            @RequestParam("council") String council,
                                            @RequestParam("specialty") String specialty,
                                            @RequestParam("experience") int experience,
                                            @RequestParam("degree") String degree,
                                            @RequestParam("clinicName") String clinicName,
                                            @RequestParam("clinicAddress") String clinicAddress,
                                            @RequestParam("idProof") MultipartFile idProof,
                                            @RequestParam("license") MultipartFile license,
                                            @RequestParam("degreeCert") MultipartFile degreeCert,
                                            @RequestParam("photo") MultipartFile photo) throws IOException {

    	Doctor doctor = new Doctor();
    	doctor.setFullName(fullName);
    	doctor.setEmail(email);
    	doctor.setMobile(mobile);
    	doctor .setPassword(password);
    	doctor.setRegistrationNumber(registrationNumber);
    	doctor .setCouncil(council);
    	doctor  .setSpecialty(specialty);
    	doctor.setExperience(experience);
    	doctor  .setDegree(degree);
    	doctor  .setClinicName(clinicName);
    	doctor .setClinicAddress(clinicAddress);

        return ResponseEntity.ok(doctorService.registerDoctor(doctor, idProof, license, degreeCert, photo));
    }
    
//    @PostMapping("/login")
//    public ResponseEntity<?> loginDoctor(@RequestBody Doctor loginRequest) {
//        try {
//            Doctor doctor = doctorService.login(loginRequest);
//            return ResponseEntity.ok(doctor);
//        } catch (ResourceNotFoundException | AuthenticationException ex) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
//        }
//    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Doctor loginRequest) {
        String token = doctorService.login(loginRequest);
        System.out.println("Generated Token: " + token);

        return ResponseEntity.ok(Collections.singletonMap("token", token));

    }
    

}