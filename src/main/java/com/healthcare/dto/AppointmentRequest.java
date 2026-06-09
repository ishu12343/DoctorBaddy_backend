package com.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentRequest {
    @JsonProperty("doctor_id")
    private Long doctorId;
    
    @JsonProperty("appointment_date")
    private String appointmentDate;
    
    @JsonProperty("appointment_time")
    private String appointmentTime;
    
    private String reason;
    
    @JsonProperty("new_date")
    private String newDate;
    
    @JsonProperty("new_time")
    private String newTime;
}
