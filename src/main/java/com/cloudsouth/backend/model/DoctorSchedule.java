package com.cloudsouth.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "doctor_schedules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long doctorId;

    @Column(nullable = false)
    private String dayOfWeek; // e.g., "MONDAY", "TUESDAY", etc.

    @Column(nullable = false)
    private String startTime; // e.g., "09:00"

    @Column(nullable = false)
    private String endTime; // e.g., "17:00"

    @Builder.Default
    private Integer slotDurationMinutes = 60; // default 60 min slots

    @Builder.Default
    private Boolean isActive = true;
}
