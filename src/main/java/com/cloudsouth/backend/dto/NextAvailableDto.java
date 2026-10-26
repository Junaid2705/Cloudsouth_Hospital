package com.cloudsouth.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NextAvailableDto {
    private Long doctorId;
    private String doctorName;
    private String department;
    private LocalDate nextAvailableDate;
    private String dayOfWeek;
    private String firstAvailableSlot;
    private Double consultationFee;
    private boolean isAvailable;
    private String message;
}
