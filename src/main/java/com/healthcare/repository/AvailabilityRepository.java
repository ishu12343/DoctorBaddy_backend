package com.healthcare.repository;

import com.healthcare.model.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
    
    List<Availability> findByDoctorId(Long doctorId);
    
    List<Availability> findByDoctorIdAndDayOfWeekAndActiveTrue(
        Long doctorId, 
        DayOfWeek dayOfWeek
    );
    
    @Query("SELECT a FROM Availability a " +
           "WHERE a.doctor.id = :doctorId " +
           "AND a.dayOfWeek = :dayOfWeek " +
           "AND ((a.validFrom IS NULL OR a.validFrom <= :dateTime) " +
           "AND (a.validUntil IS NULL OR a.validUntil >= :dateTime)) " +
           "AND a.active = true")
    List<Availability> findAvailableSlots(
        @Param("doctorId") Long doctorId,
        @Param("dayOfWeek") DayOfWeek dayOfWeek,
        @Param("dateTime") LocalDateTime dateTime
    );
    
    @Query("SELECT a FROM Availability a " +
           "WHERE a.doctor.id = :doctorId " +
           "AND a.dayOfWeek = :dayOfWeek " +
           "AND a.startTime <= :endTime " +
           "AND a.endTime >= :startTime " +
           "AND ((a.validFrom IS NULL OR a.validFrom <= :date) " +
           "AND (a.validUntil IS NULL OR a.validUntil >= :date)) " +
           "AND a.active = true")
    List<Availability> findOverlappingAvailability(
        @Param("doctorId") Long doctorId,
        @Param("dayOfWeek") DayOfWeek dayOfWeek,
        @Param("startTime") String startTime,
        @Param("endTime") String endTime,
        @Param("date") LocalDateTime date
    );
    
    @Query("SELECT a FROM Availability a " +
           "WHERE a.doctor.id = :doctorId " +
           "AND a.dayOfWeek = :dayOfWeek " +
           "AND ((a.validFrom <= :endDate AND (a.validUntil IS NULL OR a.validUntil >= :startDate)) " +
           "OR (a.validFrom IS NULL AND a.validUntil IS NULL)) " +
           "AND a.active = true")
    List<Availability> findAvailabilityInDateRange(
        @Param("doctorId") Long doctorId,
        @Param("dayOfWeek") DayOfWeek dayOfWeek,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT a FROM Availability a " +
           "WHERE a.doctor.id = :doctorId " +
           "AND a.dayOfWeek = :dayOfWeek " +
           "AND a.startTime = :startTime " +
           "AND a.endTime = :endTime " +
           "AND ((a.validFrom IS NULL AND :date >= CURRENT_DATE) " +
           "OR (a.validFrom IS NOT NULL AND a.validFrom <= :date)) " +
           "AND (a.validUntil IS NULL OR a.validUntil >= :date) " +
           "AND a.active = true")
    Optional<Availability> findSpecificAvailability(
        @Param("doctorId") Long doctorId,
        @Param("dayOfWeek") DayOfWeek dayOfWeek,
        @Param("startTime") String startTime,
        @Param("endTime") String endTime,
        @Param("date") LocalDateTime date
    );
    
    @Query("SELECT a FROM Availability a " +
           "WHERE a.doctor.id = :doctorId " +
           "AND a.dayOfWeek = :dayOfWeek " +
           "AND a.active = true " +
           "ORDER BY a.startTime")
    List<Availability> findDoctorDaySchedule(
        @Param("doctorId") Long doctorId,
        @Param("dayOfWeek") DayOfWeek dayOfWeek
    );
    
    @Query("SELECT DISTINCT a.dayOfWeek FROM Availability a " +
           "WHERE a.doctor.id = :doctorId AND a.active = true")
    List<DayOfWeek> findAvailableDaysForDoctor(@Param("doctorId") Long doctorId);
    
    @Query("SELECT a FROM Availability a " +
           "WHERE a.doctor.id = :doctorId " +
           "AND a.active = true " +
           "AND (a.validFrom <= CURRENT_DATE OR a.validFrom IS NULL) " +
           "AND (a.validUntil >= CURRENT_DATE OR a.validUntil IS NULL)")
    List<Availability> findCurrentAvailability(@Param("doctorId") Long doctorId);
}
