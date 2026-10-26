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
public class DepartmentAvailabilityDto {
    private Long departmentId;
    private String departmentName;
    private String departmentCode;
    private LocalDate date;
    private String dayOfWeek;
    private int totalDoctorsInDepartment;
    private int availableDoctorsCount;
    private int totalAvailableSlotsCount;
    private List<DoctorAvailabilityDto> availableDoctors;
}
