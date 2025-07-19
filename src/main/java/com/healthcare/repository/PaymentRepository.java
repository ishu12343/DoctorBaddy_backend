package com.healthcare.repository;

import com.healthcare.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    List<Payment> findByDoctorId(Long doctorId);
    
    @Query("SELECT p FROM Payment p " +
           "WHERE p.doctor.id = :doctorId " +
           "AND p.paymentDate BETWEEN :startDate AND :endDate " +
           "ORDER BY p.paymentDate DESC")
    List<Payment> findByDoctorIdAndDateRange(
        @Param("doctorId") Long doctorId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
           "WHERE p.doctor.id = :doctorId " +
           "AND p.status = 'COMPLETED'")
    BigDecimal getTotalEarnings(@Param("doctorId") Long doctorId);
    
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
           "WHERE p.doctor.id = :doctorId " +
           "AND p.paymentDate BETWEEN :startDate AND :endDate " +
           "AND p.status = 'COMPLETED'")
    BigDecimal getEarningsInDateRange(
        @Param("doctorId") Long doctorId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    @Query("SELECT p FROM Payment p " +
           "WHERE p.doctor.id = :doctorId " +
           "AND p.status = 'COMPLETED' " +
           "ORDER BY p.paymentDate DESC")
    List<Payment> findCompletedPayments(@Param("doctorId") Long doctorId);
    
    @Query("SELECT p FROM Payment p " +
           "WHERE p.doctor.id = :doctorId " +
           "AND p.status = 'PENDING' " +
           "ORDER BY p.paymentDate DESC")
    List<Payment> findPendingPayments(@Param("doctorId") Long doctorId);
    
    @Query("SELECT p FROM Payment p " +
           "WHERE p.doctor.id = :doctorId " +
           "AND p.appointment.id = :appointmentId")
    Optional<Payment> findByAppointmentId(
        @Param("doctorId") Long doctorId,
        @Param("appointmentId") Long appointmentId
    );
    
    @Query("SELECT p FROM Payment p " +
           "WHERE p.doctor.id = :doctorId " +
           "AND p.transactionId = :transactionId")
    Optional<Payment> findByTransactionId(
        @Param("doctorId") Long doctorId,
        @Param("transactionId") String transactionId
    );
    
    @Query("SELECT p FROM Payment p " +
           "WHERE p.doctor.id = :doctorId " +
           "AND p.paymentDate >= :startDate " +
           "AND p.paymentDate < :endDate " +
           "AND p.status = 'COMPLETED'")
    List<Payment> findCompletedPaymentsInDateRange(
        @Param("doctorId") Long doctorId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT p FROM Payment p " +
           "WHERE p.doctor.id = :doctorId " +
           "AND p.paymentDate >= :startDate " +
           "AND p.paymentDate < :endDate " +
           "AND p.status = 'PENDING'")
    List<Payment> findPendingPaymentsInDateRange(
        @Param("doctorId") Long doctorId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT COUNT(p) FROM Payment p " +
           "WHERE p.doctor.id = :doctorId " +
           "AND p.status = 'COMPLETED'")
    long countCompletedPayments(@Param("doctorId") Long doctorId);
    
    @Query("SELECT COUNT(p) FROM Payment p " +
           "WHERE p.doctor.id = :doctorId " +
           "AND p.status = 'PENDING'")
    long countPendingPayments(@Param("doctorId") Long doctorId);
}
