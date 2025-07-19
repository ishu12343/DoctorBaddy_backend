package com.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityDTO {
    private Long id;
    private Long doctorId;
    private DayOfWeek dayOfWeek;
    
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;
    
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;
    
    private boolean isRecurring;
    private LocalDate validFrom;
    private LocalDate validUntil;
    private boolean isAvailable;
    private String reason;
    private boolean active;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    
    // For bulk operations
    private DayOfWeek[] daysOfWeek;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean applyToAllWeeks;
}
