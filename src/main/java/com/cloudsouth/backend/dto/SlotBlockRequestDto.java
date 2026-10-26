package com.cloudsouth.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotBlockRequestDto {
    private LocalDate date;
    private String slot; // e.g. "09:00 - 09:45" or "09:00 AM"
    private String reason; // e.g. "Surgery", "Emergency duty", "Meeting"
}
