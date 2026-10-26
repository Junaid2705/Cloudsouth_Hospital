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
    private String slot; // e.g. "09:00 - 09:45"
    private String displayTime; // e.g. "09:00 AM" (matching frontend chips)
    private String startTime; // "09:00"
    private String endTime; // "09:45"
    private boolean isAvailable;
    private String status; // "AVAILABLE", "BOOKED", "HELD", "BLOCKED", "UNAVAILABLE"
}
