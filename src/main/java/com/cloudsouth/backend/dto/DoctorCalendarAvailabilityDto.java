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
public class DoctorCalendarAvailabilityDto {
    private Long doctorId;
    private String doctorName;
    private String department;
    private String specialization;
    private LocalDate startDate;
    private LocalDate endDate;
    private int totalAvailableDays;
    private List<DayAvailabilityDto> days;
}
