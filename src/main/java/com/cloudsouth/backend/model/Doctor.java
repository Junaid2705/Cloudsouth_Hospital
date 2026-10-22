package com.cloudsouth.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "doctors")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String specialization;

    @Column(nullable = false)
    private String department;

    private Long departmentId;

    private String qualification;
    private Integer experienceYears;

    @Builder.Default
    private Double consultationFee = 50.00; // Standard $50 as specified in BRD

    @Builder.Default
    private Double returningFee = 45.00; // Standard $45 (10% returning patient discount)

    private String photoUrl;
    private String availableDays;
    
    private String timings; // e.g. "09:00 AM - 01:00 PM (Morning Shift)"
    private String timingShift; // e.g. "Morning Shift" or "Evening Shift"

    @Column(length = 1000)
    private String availableTimeSlots; // Comma-separated: "09:00 AM, 09:45 AM, 10:30 AM, 11:15 AM, 12:00 PM"

    private String email;
    private String phone;

    @Builder.Default
    private Boolean isActive = true;

    @JsonProperty("timeSlots")
    public List<String> getTimeSlotsList() {
        if (availableTimeSlots == null || availableTimeSlots.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(availableTimeSlots.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
