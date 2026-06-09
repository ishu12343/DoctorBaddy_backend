package com.healthcare.repository;

import com.healthcare.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByDoctorIdOrderByAppointmentDatetimeDesc(Long doctorId);
    List<Appointment> findByPatientIdOrderByAppointmentDatetimeDesc(Long patientId);
    
    @Query("SELECT a FROM Appointment a WHERE a.doctorId = :doctorId AND a.appointmentDatetime = :datetime AND a.status != 'CANCELLED'")
    Optional<Appointment> findByDoctorIdAndDatetimeNotCancelled(@Param("doctorId") Long doctorId, @Param("datetime") LocalDateTime datetime);
    
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.doctorId = :doctorId")
    Long countByDoctorId(@Param("doctorId") Long doctorId);
    
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.doctorId = :doctorId AND a.status = 'PENDING'")
    Long countPendingByDoctorId(@Param("doctorId") Long doctorId);
    
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.doctorId = :doctorId AND a.status = 'CONFIRMED'")
    Long countConfirmedByDoctorId(@Param("doctorId") Long doctorId);
    
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.doctorId = :doctorId AND a.status = 'COMPLETED'")
    Long countCompletedByDoctorId(@Param("doctorId") Long doctorId);
    
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.doctorId = :doctorId AND a.status = 'CANCELLED'")
    Long countCancelledByDoctorId(@Param("doctorId") Long doctorId);
    
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.doctorId = :doctorId AND DATE(a.appointmentDatetime) = CURRENT_DATE AND a.status = 'CONFIRMED'")
    Long countTodayByDoctorId(@Param("doctorId") Long doctorId);
    
    @Query("SELECT a FROM Appointment a WHERE a.doctorId = :doctorId AND a.createdAt >= :createdAt ORDER BY a.createdAt DESC")
    List<Appointment> findByDoctorIdAndCreatedAtAfterOrderByCreatedAtDesc(@Param("doctorId") Long doctorId, @Param("createdAt") LocalDateTime createdAt);
}