package com.healthcare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private Long id;
    private Long patientId;
    private String patientName;
    private String patientPhoto;
    private Long doctorId;
    private String doctorName;
    private Long appointmentId;
    private Integer rating;
    private String comment;
    private String response;
    private LocalDateTime responseDate;
    private boolean isAnonymous;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // For response
    private boolean canEdit;
    private boolean canDelete;
    private boolean canRespond;
}
