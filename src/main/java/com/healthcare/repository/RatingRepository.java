package com.healthcare.repository;

import com.healthcare.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByAppointmentId(Long appointmentId);
    
    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.doctorId = :doctorId")
    Double getAverageRatingByDoctorId(@Param("doctorId") Long doctorId);
    
    @Query("SELECT COUNT(r) FROM Rating r WHERE r.doctorId = :doctorId")
    Long countByDoctorId(@Param("doctorId") Long doctorId);
}
