package com.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    private Long id;
    private Long appointmentId;
    private String appointmentDate;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String transactionId;
    private String paymentGateway;
    private String status; // PENDING, COMPLETED, FAILED, REFUNDED
    private String payerName;
    private String payerEmail;
    private String payerPhone;
    private String description;
    private String receiptUrl;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // For reports and summaries
    private String period; // daily, weekly, monthly, yearly
    private String paymentStatus; // For filtering
    private String sortBy; // For sorting
    private String sortOrder; // ASC or DESC
}
