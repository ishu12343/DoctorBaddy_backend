package com.healthcare.repository;

import com.healthcare.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByEmail(String email);
    Optional<Doctor> findByMobile(String mobile);
    boolean existsByEmail(String email);
    boolean existsByMobile(String mobile);
    
    @Query("SELECT d FROM Doctor d WHERE d.approved = true AND d.suspended = false AND d.status = 'ACTIVE'")
    List<Doctor> findActiveDoctors();
    
    @Query("SELECT d FROM Doctor d WHERE d.approved = true AND d.suspended = false AND d.status = 'ACTIVE' AND (:specialty IS NULL OR d.specialty LIKE %:specialty%) AND (:city IS NULL OR d.city LIKE %:city%) AND (:search IS NULL OR d.fullName LIKE %:search% OR d.specialty LIKE %:search%)")
    List<Doctor> findActiveDoctorsWithFilters(@Param("specialty") String specialty, @Param("city") String city, @Param("search") String search);
    
    @Query("SELECT d FROM Doctor d WHERE d.id = :id")
    Optional<Doctor> findByIdWithDetails(@Param("id") Long id);
}