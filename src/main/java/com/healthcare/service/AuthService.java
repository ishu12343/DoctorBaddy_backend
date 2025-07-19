package com.healthcare.service;


import com.healthcare.model.Doctor;
import com.healthcare.model.User;

public interface AuthService {
    Doctor registerDoctor(Doctor registration);
    User registerPatient(User registration);
}
