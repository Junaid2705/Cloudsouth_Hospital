package com.cloudsouth.backend.dto;

import com.cloudsouth.backend.model.DoctorSchedule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkScheduleRequestDto {
    private List<DoctorSchedule> schedules;
}
