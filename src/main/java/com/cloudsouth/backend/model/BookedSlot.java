package com.cloudsouth.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "booked_slots", indexes = {
    @Index(name = "idx_doctor_slot_unique", columnList = "doctorId, bookingDate, slot")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookedSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long doctorId;

    @Column(nullable = false)
    private LocalDate bookingDate;

    @Column(nullable = false)
    private String slot; // e.g. "09:00 - 09:45" or "09:00 AM"

    private Long patientId;
    private String patientName;
    private String patientEmail;

    @Builder.Default
    private String status = "CONFIRMED"; // CONFIRMED, HELD, BLOCKED, CANCELLED

    private LocalDateTime holdExpiresAt; // For pre-booking holds (e.g. 15 min expiry)
    private String blockReason; // For manually blocked slots (FR-09)

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
