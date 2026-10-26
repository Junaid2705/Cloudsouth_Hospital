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
public class SlotCheckRequestDto {
    private Long doctorId;
    private LocalDate date;
    private String slot;
    private Long patientId;
    private String patientName;
    private String patientEmail;
}
