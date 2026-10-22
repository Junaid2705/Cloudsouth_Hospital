package com.cloudsouth.backend.controller;

import com.cloudsouth.backend.dto.ApiResponse;
import com.cloudsouth.backend.dto.DoctorAvailabilityDto;
import com.cloudsouth.backend.dto.DoctorStatsDto;
import com.cloudsouth.backend.model.Doctor;
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

    /**
     * Get doctors with full filtering support matching the frontend UI:
     * - department: "General Physician", "Leg Fracture / Orthopedic Care", etc.
     * - day: "Monday", "Tuesday", etc.
     * - shift: "Morning Shift", "Evening Shift", "Available Today"
     * - query: text search (e.g. "Sharma", "Cardiology", "Trauma")
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Doctor>>> getDoctors(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String day,
            @RequestParam(required = false) String shift,
            @RequestParam(required = false) String query,
            @RequestParam(required = false, defaultValue = "true") boolean activeOnly) {

        List<Doctor> doctors = doctorService.filterDoctors(department, day, shift, query, activeOnly);
        return ResponseEntity.ok(ApiResponse.success(doctors, "Fetched doctors successfully"));
    }

    /**
     * Returns hospital specialist stats & department counts for the frontend header & pills:
     * - Specialist Doctors: 85+
     * - Hospital Beds: 1,000+
     * - Department breakdown counts
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DoctorStatsDto>> getDoctorStats() {
        DoctorStatsDto stats = doctorService.getDoctorStats();
        return ResponseEntity.ok(ApiResponse.success(stats, "Fetched doctor directory statistics"));
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

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Doctor deleted successfully"));
    }

    // ==========================================
    // AVAILABILITY & SCHEDULE ENDPOINTS
    // ==========================================

    @GetMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<DoctorAvailabilityDto>> getDoctorAvailability(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        DoctorAvailabilityDto availability = doctorService.getDoctorAvailability(id, targetDate);
        return ResponseEntity.ok(ApiResponse.success(availability, "Doctor availability calculated successfully"));
    }

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
}
