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
public class DepartmentSummaryDto {
    private Long id;
    private String name;
    private String code;
    private String icon;
    private String headDoctorName;
    private Integer targetCapacity;
    private Integer activeDoctorCount;
    private List<String> specializations;
}
