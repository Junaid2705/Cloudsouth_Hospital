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
import java.util.regex.Pattern;

@Entity
@Table(name = "doctors", indexes = {
    @Index(name = "idx_doctor_department", columnList = "department"),
    @Index(name = "idx_doctor_dept_id", columnList = "departmentId"),
    @Index(name = "idx_doctor_active", columnList = "isActive")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {

    private static final Pattern COMMA_SEPARATOR = Pattern.compile("\\s*,\\s*");

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
    private Double consultationFee = 50.00;

    @Builder.Default
    private Double returningFee = 45.00;

    private String photoUrl;
    private String availableDays;
    private String timings;
    private String timingShift;

    @Column(length = 1000)
    private String availableTimeSlots;

    private String email;
    private String phone;

    @Builder.Default
    private Boolean isActive = true;

    @JsonProperty("timeSlots")
    public List<String> getTimeSlotsList() {
        if (availableTimeSlots == null || availableTimeSlots.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.asList(COMMA_SEPARATOR.split(availableTimeSlots.trim()));
    }
}
