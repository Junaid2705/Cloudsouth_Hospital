package com.cloudsouth.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorFeeDto {
    private Long doctorId;
    private String doctorName;
    private String department;
    private String patientType; // "NEW" or "RETURNING"
    private boolean isReturningPatient;
    private Double standardFee; // $50.00
    private Double discountPercentage; // 10.0%
    private Double discountAmount; // $5.00
    private Double finalPayableFee; // $45.00 or $50.00
    private String currency; // "USD"
    private String message;
}
