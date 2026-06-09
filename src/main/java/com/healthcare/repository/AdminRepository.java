package com.healthcare.repository;

import com.healthcare.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByEmail(String email);
    Optional<Admin> findByMobile(String mobile);
    boolean existsByEmail(String email);
    
    @Query("SELECT a FROM Admin a WHERE a.id = :id AND a.isActive = true")
    Optional<Admin> findByIdAndIsActiveTrue(@Param("id") Long id);
}
