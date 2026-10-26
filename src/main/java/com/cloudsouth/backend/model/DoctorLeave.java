package com.cloudsouth.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "doctor_leaves", indexes = {
    @Index(name = "idx_doctor_leave_date", columnList = "doctorId, leaveDate")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorLeave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long doctorId;

    @Column(nullable = false)
    private LocalDate leaveDate;

    private String reason;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
