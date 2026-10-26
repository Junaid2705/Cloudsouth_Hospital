package com.cloudsouth.backend.controller;

import com.cloudsouth.backend.dto.*;
import com.cloudsouth.backend.model.BookedSlot;
import com.cloudsouth.backend.model.Doctor;
import com.cloudsouth.backend.model.DoctorLeave;
import com.cloudsouth.backend.model.DoctorSchedule;
import com.cloudsouth.backend.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    // ==========================================
    // DOCTOR DIRECTORY & FILTERING (FR-04, NFR-06)
    // ==========================================

    /**
     * Get doctors with full multi-criteria filtering matching the frontend UI:
     * - department: "General Physician", "Leg Fracture / Orthopedic Care", etc.
     * - day: "Monday", "Tuesday", etc.
     * - shift: "Morning Shift", "Evening Shift", "Available Today"
     * - query: text search (e.g. "Sharma", "Cardiology", "Trauma")
     * - minExperience: minimum years of experience (e.g. 15 for senior specialists)
     * - maxFee: maximum consultation fee filter
     * - activeOnly: filter active doctors only (default true)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Doctor>>> getDoctors(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String day,
            @RequestParam(required = false) String shift,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Integer minExperience,
            @RequestParam(required = false) Double maxFee,
            @RequestParam(required = false, defaultValue = "true") boolean activeOnly) {

        List<Doctor> doctors = doctorService.filterDoctors(department, day, shift, query, minExperience, maxFee, activeOnly);
        return ResponseEntity.ok(ApiResponse.success(doctors, "Fetched doctors successfully"));
    }

    /**
     * Paginated doctor directory search for large datasets (NFR-06)
     */
    @GetMapping("/page")
    public ResponseEntity<ApiResponse<PageResponse<Doctor>>> getDoctorsPaged(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String day,
            @RequestParam(required = false) String shift,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Integer minExperience,
            @RequestParam(required = false) Double maxFee,
            @RequestParam(required = false, defaultValue = "true") boolean activeOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        PageResponse<Doctor> response = doctorService.getDoctorsPaged(
                department, day, shift, query, minExperience, maxFee, activeOnly,
                page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.success(response, "Fetched paginated doctors successfully"));
    }

    /**
     * Hospital stats header & department pill counts:
     * - Specialist Doctors: 85+
     * - Hospital Beds: 1,000+
     * - Department breakdown counts
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DoctorStatsDto>> getDoctorStats() {
        DoctorStatsDto stats = doctorService.getDoctorStats();
        return ResponseEntity.ok(ApiResponse.success(stats, "Fetched doctor directory statistics"));
    }

    @GetMapping("/specializations")
    public ResponseEntity<ApiResponse<List<String>>> getAllSpecializations() {
        List<String> specs = doctorService.getAllSpecializations();
        return ResponseEntity.ok(ApiResponse.success(specs, "Fetched doctor specializations successfully"));
    }

    @GetMapping("/departments")
    public ResponseEntity<ApiResponse<List<String>>> getAllDoctorDepartments() {
        List<String> depts = doctorService.getAllDepartmentsList();
        return ResponseEntity.ok(ApiResponse.success(depts, "Fetched distinct doctor departments successfully"));
    }

    @GetMapping("/specialization/{specialization}")
    public ResponseEntity<ApiResponse<List<Doctor>>> getDoctorsBySpecialization(@PathVariable String specialization) {
        List<Doctor> doctors = doctorService.getDoctorsBySpecialization(specialization);
        return ResponseEntity.ok(ApiResponse.success(doctors, "Fetched doctors for specialization: " + specialization));
    }

    @GetMapping("/experience")
    public ResponseEntity<ApiResponse<List<Doctor>>> getDoctorsByMinExperience(
            @RequestParam(defaultValue = "10") Integer minYears) {
        List<Doctor> doctors = doctorService.getDoctorsByMinExperience(minYears);
        return ResponseEntity.ok(ApiResponse.success(doctors, "Fetched doctors with at least " + minYears + " years experience"));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<DoctorAvailabilityDto>>> getAvailableDoctors(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String shift) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        List<DoctorAvailabilityDto> availableDoctors = doctorService.findAvailableDoctorsOnDate(targetDate, department, shift);
        return ResponseEntity.ok(ApiResponse.success(availableDoctors, "Found " + availableDoctors.size() + " available doctors on " + targetDate));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Doctor>>> searchDoctors(@RequestParam String query) {
        List<Doctor> doctors = doctorService.searchDoctors(query);
        return ResponseEntity.ok(ApiResponse.success(doctors, "Doctor search completed"));
    }

    @GetMapping("/department/{departmentName}")
    public ResponseEntity<ApiResponse<List<Doctor>>> getDoctorsByDepartment(@PathVariable String departmentName) {
        List<Doctor> doctors = doctorService.getDoctorsByDepartment(departmentName);
        return ResponseEntity.ok(ApiResponse.success(doctors, "Fetched doctors by department successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Doctor>> getDoctorById(@PathVariable Long id) {
        Doctor doctor = doctorService.getDoctorById(id);
        return ResponseEntity.ok(ApiResponse.success(doctor, "Fetched doctor successfully"));
    }

    // ==========================================
    // DOCTOR FEE & RETURNING DISCOUNT (BR-05, BR-06, FR-11, FR-16)
    // ==========================================

    @GetMapping("/{id}/fee")
    public ResponseEntity<ApiResponse<DoctorFeeDto>> getDoctorFee(
            @PathVariable Long id,
            @RequestParam(required = false) Boolean isReturning,
            @RequestParam(required = false) Long patientId) {
        DoctorFeeDto feeInfo = doctorService.calculateDoctorFee(id, isReturning, patientId);
        return ResponseEntity.ok(ApiResponse.success(feeInfo, feeInfo.getMessage()));
    }

    // ==========================================
    // DOCTOR MANAGEMENT & LIFECYCLE (FR-08, BR-12)
    // ==========================================

    @PostMapping
    public ResponseEntity<ApiResponse<Doctor>> createDoctor(@RequestBody Doctor doctor) {
        Doctor createdDoctor = doctorService.createDoctor(doctor);
        return new ResponseEntity<>(ApiResponse.success(createdDoctor, "Doctor created successfully"), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Doctor>> updateDoctor(@PathVariable Long id, @RequestBody Doctor doctorDetails) {
        Doctor updatedDoctor = doctorService.updateDoctor(id, doctorDetails);
        return ResponseEntity.ok(ApiResponse.success(updatedDoctor, "Doctor updated successfully"));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Doctor>> toggleDoctorStatus(@PathVariable Long id, @RequestParam boolean active) {
        Doctor updated = doctorService.toggleDoctorStatus(id, active);
        return ResponseEntity.ok(ApiResponse.success(updated, "Doctor status updated to " + (active ? "ACTIVE" : "INACTIVE")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Doctor deleted successfully"));
    }

    // ==========================================
    // AVAILABILITY & REAL-TIME SLOTS (FR-07, FR-12, FR-13, BR-04)
    // ==========================================

    @GetMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<DoctorAvailabilityDto>> getDoctorAvailability(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        DoctorAvailabilityDto availability = doctorService.getDoctorAvailability(id, targetDate);
        return ResponseEntity.ok(ApiResponse.success(availability, availability.getMessage()));
    }

    @GetMapping("/{id}/calendar")
    public ResponseEntity<ApiResponse<DoctorCalendarAvailabilityDto>> getDoctorCalendarAvailability(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        DoctorCalendarAvailabilityDto calendar = doctorService.getDoctorCalendarAvailability(id, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(calendar, "Doctor availability calendar retrieved successfully"));
    }

    @GetMapping("/{id}/next-available")
    public ResponseEntity<ApiResponse<NextAvailableDto>> getNextAvailable(@PathVariable Long id) {
        NextAvailableDto nextAvailable = doctorService.getNextAvailableSlot(id);
        return ResponseEntity.ok(ApiResponse.success(nextAvailable, nextAvailable.getMessage()));
    }

    @PostMapping("/{id}/check-slot")
    public ResponseEntity<ApiResponse<SlotCheckResponseDto>> checkSpecificSlot(
            @PathVariable Long id,
            @RequestBody SlotCheckRequestDto request) {
        SlotCheckResponseDto result = doctorService.checkSpecificSlotAvailability(id, request.getDate(), request.getSlot());
        return ResponseEntity.ok(ApiResponse.success(result, result.getMessage()));
    }

    @PostMapping("/{id}/pre-book-slot")
    public ResponseEntity<ApiResponse<BookedSlot>> preBookSlot(
            @PathVariable Long id,
            @RequestBody SlotCheckRequestDto request,
            @RequestParam(required = false, defaultValue = "15") Integer holdMinutes) {
        request.setDoctorId(id);
        BookedSlot held = doctorService.preBookSlot(request, holdMinutes);
        return new ResponseEntity<>(ApiResponse.success(held, "Slot temporarily held for " + holdMinutes + " minutes for payment (FR-12)"), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/book-slot")
    public ResponseEntity<ApiResponse<BookedSlot>> bookOrReserveSlot(
            @PathVariable Long id,
            @RequestBody SlotCheckRequestDto request) {
        request.setDoctorId(id);
        BookedSlot booked = doctorService.reserveSlot(request);
        return new ResponseEntity<>(ApiResponse.success(booked, "Slot reserved successfully (Double booking prevented)"), HttpStatus.CREATED);
    }

    @PostMapping("/slots/{slotId}/confirm")
    public ResponseEntity<ApiResponse<BookedSlot>> confirmSlot(@PathVariable Long slotId) {
        BookedSlot confirmed = doctorService.confirmSlot(slotId);
        return ResponseEntity.ok(ApiResponse.success(confirmed, "Slot reservation confirmed successfully"));
    }

    @PostMapping("/slots/{slotId}/release")
    public ResponseEntity<ApiResponse<Void>> releaseSlot(@PathVariable Long slotId) {
        doctorService.releaseSlot(slotId);
        return ResponseEntity.ok(ApiResponse.success(null, "Slot reservation released successfully"));
    }

    @PostMapping("/{id}/release-slot")
    public ResponseEntity<ApiResponse<Void>> releaseSlotByDetails(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String slot) {
        doctorService.releaseSlotByDoctorAndDate(id, date, slot);
        return ResponseEntity.ok(ApiResponse.success(null, "Slot released successfully"));
    }

    // ==========================================
    // MANUAL SLOT BLOCKING / EXCEPTIONS (FR-09, BR-11)
    // ==========================================

    @PostMapping("/{id}/block-slot")
    public ResponseEntity<ApiResponse<BookedSlot>> blockSlot(
            @PathVariable Long id,
            @RequestBody SlotBlockRequestDto request) {
        BookedSlot blocked = doctorService.blockSlot(id, request.getDate(), request.getSlot(), request.getReason());
        return new ResponseEntity<>(ApiResponse.success(blocked, "Slot blocked successfully (FR-09)"), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/unblock-slot")
    public ResponseEntity<ApiResponse<Void>> unblockSlot(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String slot) {
        doctorService.unblockSlot(id, date, slot);
        return ResponseEntity.ok(ApiResponse.success(null, "Slot unblocked successfully"));
    }

    @GetMapping("/{id}/blocked-slots")
    public ResponseEntity<ApiResponse<List<BookedSlot>>> getBlockedSlots(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<BookedSlot> blocked = doctorService.getBlockedSlots(id, date);
        return ResponseEntity.ok(ApiResponse.success(blocked, "Fetched blocked slots"));
    }

    // ==========================================
    // SCHEDULES ENDPOINTS (FR-09, BR-11)
    // ==========================================

    @GetMapping("/{id}/schedules")
    public ResponseEntity<ApiResponse<List<DoctorSchedule>>> getDoctorSchedules(@PathVariable Long id) {
        List<DoctorSchedule> schedules = doctorService.getDoctorSchedules(id);
        return ResponseEntity.ok(ApiResponse.success(schedules, "Fetched doctor schedules successfully"));
    }

    @PostMapping("/{id}/schedules")
    public ResponseEntity<ApiResponse<DoctorSchedule>> addDoctorSchedule(
            @PathVariable Long id,
            @RequestBody DoctorSchedule schedule) {
        DoctorSchedule created = doctorService.addDoctorSchedule(id, schedule);
        return new ResponseEntity<>(ApiResponse.success(created, "Schedule added successfully"), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/schedules/bulk")
    public ResponseEntity<ApiResponse<List<DoctorSchedule>>> saveDoctorSchedulesBulk(
            @PathVariable Long id,
            @RequestBody BulkScheduleRequestDto request) {
        List<DoctorSchedule> created = doctorService.saveDoctorSchedulesBulk(id, request.getSchedules());
        return new ResponseEntity<>(ApiResponse.success(created, "Weekly schedules saved successfully in bulk"), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/schedules/{scheduleId}")
    public ResponseEntity<ApiResponse<DoctorSchedule>> updateDoctorSchedule(
            @PathVariable Long id,
            @PathVariable Long scheduleId,
            @RequestBody DoctorSchedule schedule) {
        DoctorSchedule updated = doctorService.updateDoctorSchedule(scheduleId, schedule);
        return ResponseEntity.ok(ApiResponse.success(updated, "Schedule updated successfully"));
    }

    @DeleteMapping("/{id}/schedules/{scheduleId}")
    public ResponseEntity<ApiResponse<Void>> deleteDoctorSchedule(
            @PathVariable Long id,
            @PathVariable Long scheduleId) {
        doctorService.deleteDoctorSchedule(scheduleId);
        return ResponseEntity.ok(ApiResponse.success(null, "Schedule deleted successfully"));
    }

    // ==========================================
    // DOCTOR LEAVES / TIME-OFF (FR-09)
    // ==========================================

    @GetMapping("/{id}/leaves")
    public ResponseEntity<ApiResponse<List<DoctorLeave>>> getDoctorLeaves(@PathVariable Long id) {
        List<DoctorLeave> leaves = doctorService.getDoctorLeaves(id);
        return ResponseEntity.ok(ApiResponse.success(leaves, "Fetched doctor leaves successfully"));
    }

    @PostMapping("/{id}/leaves")
    public ResponseEntity<ApiResponse<DoctorLeave>> addDoctorLeave(
            @PathVariable Long id,
            @RequestBody DoctorLeave leave) {
        DoctorLeave created = doctorService.addDoctorLeave(id, leave);
        return new ResponseEntity<>(ApiResponse.success(created, "Doctor leave scheduled successfully"), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/leaves/range")
    public ResponseEntity<ApiResponse<List<DoctorLeave>>> addDoctorLeaveRange(
            @PathVariable Long id,
            @RequestBody DoctorLeaveRangeRequestDto request) {
        List<DoctorLeave> created = doctorService.addDoctorLeaveRange(id, request.getStartDate(), request.getEndDate(), request.getReason());
        return new ResponseEntity<>(ApiResponse.success(created, "Doctor leave range scheduled successfully"), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}/leaves/{leaveId}")
    public ResponseEntity<ApiResponse<Void>> deleteDoctorLeave(
            @PathVariable Long id,
            @PathVariable Long leaveId) {
        doctorService.deleteDoctorLeave(leaveId);
        return ResponseEntity.ok(ApiResponse.success(null, "Doctor leave removed successfully"));
    }

    @GetMapping("/leaves")
    public ResponseEntity<ApiResponse<List<DoctorLeave>>> getHospitalLeavesOnDate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate target = (date != null) ? date : LocalDate.now();
        List<DoctorLeave> leaves = doctorService.getHospitalLeavesOnDate(target);
        return ResponseEntity.ok(ApiResponse.success(leaves, "Fetched hospital leaves for " + target));
    }
}
