package com.healthcare.repository;

import com.healthcare.model.Appointment;
import com.healthcare.model.Appointment.AppointmentStatus;
import com.healthcare.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    List<Appointment> findByDoctorIdAndAppointmentTimeBetween(
        Long doctorId, 
        LocalDateTime startDate, 
        LocalDateTime endDate
    );
    
    List<Appointment> findByDoctorIdAndStatus(
        Long doctorId, 
        AppointmentStatus status
    );
    
    @Query("SELECT a FROM Appointment a " +
           "WHERE a.doctor.id = :doctorId " +
           "AND DATE(a.appointmentTime) = :date " +
           "ORDER BY a.appointmentTime")
    List<Appointment> findDoctorAppointmentsByDate(
        @Param("doctorId") Long doctorId,
        @Param("date") LocalDate date
    );
    
    @Query("SELECT COUNT(a) FROM Appointment a " +
           "WHERE a.doctor.id = :doctorId " +
           "AND a.status = :status")
    long countByDoctorIdAndStatus(
        @Param("doctorId") Long doctorId,
        @Param("status") AppointmentStatus status
    );
    
    @Query("SELECT a FROM Appointment a " +
           "WHERE a.doctor.id = :doctorId " +
           "AND a.patient.id = :patientId " +
           "ORDER BY a.appointmentTime DESC")
    List<Appointment> findDoctorPatientHistory(
        @Param("doctorId") Long doctorId,
        @Param("patientId") Long patientId
    );
    
    @Query("SELECT a FROM Appointment a " +
           "WHERE a.doctor.id = :doctorId " +
           "AND a.appointmentTime >= :startDate " +
           "AND a.appointmentTime < :endDate " +
           "AND a.status = 'COMPLETED'")
    List<Appointment> findCompletedAppointmentsInDateRange(
        @Param("doctorId") Long doctorId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT a FROM Appointment a " +
           "WHERE a.doctor.id = :doctorId " +
           "AND a.appointmentTime >= :startOfDay " +
           "AND a.appointmentTime < :endOfDay " +
           "AND a.status = 'PENDING' " +
           "ORDER BY a.appointmentTime ASC")
    List<Appointment> findTodayPendingAppointments(
        @Param("doctorId") Long doctorId,
        @Param("startOfDay") LocalDateTime startOfDay,
        @Param("endOfDay") LocalDateTime endOfDay
    );
    
    @Query("SELECT COUNT(a) FROM Appointment a " +
           "WHERE a.doctor.id = :doctorId " +
           "AND a.status = 'COMPLETED'")
    long countCompletedAppointmentsByDoctorId(@Param("doctorId") Long doctorId);
    
    @Query("SELECT AVG(a.fee) FROM Appointment a " +
           "WHERE a.doctor.id = :doctorId " +
           "AND a.status = 'COMPLETED'")
    Double findAverageAppointmentFeeByDoctorId(@Param("doctorId") Long doctorId);
}