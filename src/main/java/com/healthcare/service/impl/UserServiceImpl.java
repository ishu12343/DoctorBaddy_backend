package com.healthcare.service.impl;

import com.healthcare.exception.AuthenticationException;
import com.healthcare.model.User;
import com.healthcare.repository.UserRepository;
import com.healthcare.security.JwtUtil;
import com.healthcare.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public String login(User loginRequest) {
        try {
            if (loginRequest == null) {
                throw new AuthenticationException("Login request cannot be null");
            }
            
            User user = userRepository.findByEmail(loginRequest.getEmail());
            if (user == null) {
                throw new AuthenticationException("User not found with email: " + loginRequest.getEmail());
            }
            
            if (!user.getPassword().equals(loginRequest.getPassword())) {
                throw new AuthenticationException("Invalid password");
            }

            return jwtUtil.generateToken(user.getEmail());
        } catch (Exception e) {
            // Log the error for debugging
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw to be handled by the controller
        }
    }

}