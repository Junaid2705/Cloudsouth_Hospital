package com.cloudsouth.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorStatsDto {
    private String specialistDoctors; // "85+"
    private String hospitalBeds;     // "1,000+"
    private Integer totalDoctorsCount;
    private Map<String, Integer> departmentCounts;
}
