package com.cloudsouth.backend.controller;

import com.cloudsouth.backend.dto.*;
import com.cloudsouth.backend.service.DepartmentService;
import com.cloudsouth.backend.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private final DoctorService doctorService;
    private final DepartmentService departmentService;

    /**
     * Search available doctors hospital-wide for a specific date, department, and shift (FR-04, FR-13)
     */
    @GetMapping("/doctors")
    public ResponseEntity<ApiResponse<List<DoctorAvailabilityDto>>> getAvailableDoctors(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String shift) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        List<DoctorAvailabilityDto> available = doctorService.findAvailableDoctorsOnDate(targetDate, department, shift);
        return ResponseEntity.ok(ApiResponse.success(available, "Found " + available.size() + " available doctors"));
    }

    /**
     * Check real-time availability for an entire department on a specific date (FR-11, FR-12, FR-13)
     */
    @GetMapping("/departments/{departmentId}")
    public ResponseEntity<ApiResponse<DepartmentAvailabilityDto>> getDepartmentAvailability(
            @PathVariable Long departmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        DepartmentAvailabilityDto deptAvail = departmentService.getDepartmentAvailability(departmentId, targetDate);
        return ResponseEntity.ok(ApiResponse.success(deptAvail, "Department availability retrieved successfully"));
    }

    /**
     * Check doctor's multi-day availability calendar (FR-07)
     */
    @GetMapping("/doctors/{doctorId}/calendar")
    public ResponseEntity<ApiResponse<DoctorCalendarAvailabilityDto>> getDoctorCalendar(
            @PathVariable Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        DoctorCalendarAvailabilityDto calendar = doctorService.getDoctorCalendarAvailability(doctorId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(calendar, "Doctor availability calendar retrieved successfully"));
    }

    /**
     * Validate whether a specific slot is available (BR-04, VR-02)
     */
    @PostMapping("/check-slot")
    public ResponseEntity<ApiResponse<SlotCheckResponseDto>> checkSlot(
            @RequestBody SlotCheckRequestDto request) {
        SlotCheckResponseDto response = doctorService.checkSpecificSlotAvailability(
                request.getDoctorId(), request.getDate(), request.getSlot());
        return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
    }

    /**
     * Get earliest next available slot for a doctor
     */
    @GetMapping("/next")
    public ResponseEntity<ApiResponse<NextAvailableDto>> getNextAvailable(
            @RequestParam Long doctorId) {
        NextAvailableDto next = doctorService.getNextAvailableSlot(doctorId);
        return ResponseEntity.ok(ApiResponse.success(next, next.getMessage()));
    }
}
