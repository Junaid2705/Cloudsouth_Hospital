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
public class SlotCheckResponseDto {
    private Long doctorId;
    private String doctorName;
    private LocalDate date;
    private String slot;
    private boolean isAvailable;
    private Double consultationFee;
    private String message;
}
