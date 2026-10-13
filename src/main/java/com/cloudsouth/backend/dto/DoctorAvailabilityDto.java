package com.cloudsouth.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorAvailabilityDto {
    private Long doctorId;
    private String doctorName;
    private String department;
    private String specialization;
    private LocalDate date;
    private String dayOfWeek;
    private Double consultationFee;
    private boolean isAvailable;
    private List<TimeSlotDto> availableSlots;
    private String message;
}
