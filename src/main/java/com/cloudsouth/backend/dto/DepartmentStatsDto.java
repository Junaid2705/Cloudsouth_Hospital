package com.cloudsouth.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentStatsDto {
    private int totalDepartments;
    private String hospitalCapacityBeds; // "1,000+"
    private int totalTargetDoctorCapacity; // 85
    private int totalActiveDoctors;
    private List<DepartmentSummaryDto> departments;
}
