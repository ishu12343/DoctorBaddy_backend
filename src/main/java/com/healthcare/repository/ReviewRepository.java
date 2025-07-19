package com.healthcare.repository;

import com.healthcare.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    List<Review> findByDoctorId(Long doctorId);
    
    List<Review> findByDoctorIdAndRatingGreaterThanEqual(Long doctorId, int minRating);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.doctor.id = :doctorId")
    Double findAverageRatingByDoctorId(@Param("doctorId") Long doctorId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.doctor.id = :doctorId")
    long countByDoctorId(@Param("doctorId") Long doctorId);
    
    @Query("SELECT r FROM Review r " +
           "WHERE r.doctor.id = :doctorId " +
           "ORDER BY r.createdAt DESC")
    List<Review> findLatestReviewsByDoctorId(
        @Param("doctorId") Long doctorId
    );
    
    @Query("SELECT r FROM Review r " +
           "WHERE r.doctor.id = :doctorId " +
           "AND r.rating = :rating " +
           "ORDER BY r.createdAt DESC")
    List<Review> findByDoctorIdAndRating(
        @Param("doctorId") Long doctorId,
        @Param("rating") int rating
    );
    
    @Query("SELECT r FROM Review r " +
           "WHERE r.doctor.id = :doctorId " +
           "AND r.patient.id = :patientId " +
           "ORDER BY r.createdAt DESC")
    List<Review> findByDoctorIdAndPatientId(
        @Param("doctorId") Long doctorId,
        @Param("patientId") Long patientId
    );
    
    @Query("SELECT r FROM Review r " +
           "WHERE r.appointment.id = :appointmentId")
    Optional<Review> findByAppointmentId(
        @Param("appointmentId") Long appointmentId
    );
    
    @Query("SELECT r FROM Review r " +
           "WHERE r.doctor.id = :doctorId " +
           "AND r.response IS NULL " +
           "ORDER BY r.createdAt DESC")
    List<Review> findUnansweredReviewsByDoctorId(
        @Param("doctorId") Long doctorId
    );
    
    @Query("SELECT COUNT(r) FROM Review r " +
           "WHERE r.doctor.id = :doctorId " +
           "AND r.rating = :rating")
    long countByDoctorIdAndRating(
        @Param("doctorId") Long doctorId,
        @Param("rating") int rating
    );
    
    @Query("SELECT r FROM Review r " +
           "WHERE r.doctor.id = :doctorId " +
           "AND r.response IS NOT NULL " +
           "ORDER BY r.updatedAt DESC")
    List<Review> findRespondedReviewsByDoctorId(
        @Param("doctorId") Long doctorId
    );
}
