package com.healthcare.repository;

import com.healthcare.model.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {
    
    Optional<Otp> findByEmailAndMobileAndUserType(String email, String mobile, String userType);
    
    Optional<Otp> findByEmailAndMobileAndUserTypeAndVerifiedFalse(String email, String mobile, String userType);
    
    void deleteByEmailAndMobileAndUserType(String email, String mobile, String userType);
    
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
