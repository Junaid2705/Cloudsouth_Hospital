package com.cloudsouth.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotDto {
    private String slot; // e.g. "09:00 - 10:00"
    private String startTime; // "09:00"
    private String endTime; // "10:00"
    private boolean isAvailable;
    private String status; // "AVAILABLE" or "BOOKED"
}
