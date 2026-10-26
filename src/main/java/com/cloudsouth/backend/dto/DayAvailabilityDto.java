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
public class DayAvailabilityDto {
    private LocalDate date;
    private String dayOfWeek;
    private boolean isAvailable;
    private boolean onLeave;
    private String leaveReason;
    private int totalSlotsCount;
    private int availableSlotsCount;
    private int bookedSlotsCount;
    private List<TimeSlotDto> slots;
}
