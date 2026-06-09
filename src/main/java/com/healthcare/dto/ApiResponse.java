package com.healthcare.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {
    private Boolean success;
    private String message;
    private String error;
    private Object data;
    private String details;
    
    public static ApiResponse success(String message) {
        return new ApiResponse(true, message, null, null, null);
    }
    
    public static ApiResponse success(String message, Object data) {
        return new ApiResponse(true, message, null, data, null);
    }
    
    public static ApiResponse error(String error) {
        return new ApiResponse(false, null, error, null, null);
    }
    
    public static ApiResponse error(String error, String details) {
        return new ApiResponse(false, null, error, null, details);
    }
}
