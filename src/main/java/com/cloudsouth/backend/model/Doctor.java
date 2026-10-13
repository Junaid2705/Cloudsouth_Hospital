package com.cloudsouth.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    private String photoUrl;
    private String availableDays;
    private String availableTimeSlots;
    private String email;
    private String phone;

    @Builder.Default
    private Boolean isActive = true;
}
