package com.cloudsouth.backend.controller;

import com.cloudsouth.backend.dto.ApiResponse;
import com.cloudsouth.backend.dto.DepartmentAvailabilityDto;
import com.cloudsouth.backend.dto.DepartmentStatsDto;
import com.cloudsouth.backend.dto.DepartmentSummaryDto;
import com.cloudsouth.backend.model.Department;
import com.cloudsouth.backend.model.Doctor;
import com.cloudsouth.backend.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    // ==========================================
    // DEPARTMENT RETRIEVAL & SUMMARIES (FR-10, FR-11)
    // ==========================================

    @GetMapping
    public ResponseEntity<ApiResponse<List<Department>>> getAllDepartments(
            @RequestParam(required = false, defaultValue = "true") boolean activeOnly) {
        List<Department> departments = departmentService.getAllDepartments(activeOnly);
        return ResponseEntity.ok(ApiResponse.success(departments, "Fetched departments successfully"));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<List<DepartmentSummaryDto>>> getDepartmentSummaries() {
        List<DepartmentSummaryDto> summaries = departmentService.getDepartmentSummaries();
        return ResponseEntity.ok(ApiResponse.success(summaries, "Fetched department summaries successfully"));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DepartmentStatsDto>> getDepartmentStats() {
        DepartmentStatsDto stats = departmentService.getDepartmentStats();
        return ResponseEntity.ok(ApiResponse.success(stats, "Fetched hospital department statistics successfully"));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<Department>> getDepartmentByCode(@PathVariable String code) {
        Department department = departmentService.getDepartmentByCode(code);
        return ResponseEntity.ok(ApiResponse.success(department, "Fetched department by code successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Department>> getDepartmentById(@PathVariable Long id) {
        Department department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(ApiResponse.success(department, "Fetched department successfully"));
    }

    @GetMapping("/{id}/doctors")
    public ResponseEntity<ApiResponse<List<Doctor>>> getDoctorsByDepartment(@PathVariable Long id) {
        List<Doctor> doctors = departmentService.getDoctorsByDepartmentId(id);
        return ResponseEntity.ok(ApiResponse.success(doctors, "Fetched doctors for department successfully"));
    }

    @GetMapping("/{id}/doctors/search")
    public ResponseEntity<ApiResponse<List<Doctor>>> searchDoctorsInDepartment(
            @PathVariable Long id,
            @RequestParam String query) {
        List<Doctor> doctors = departmentService.searchDoctorsInDepartment(id, query);
        return ResponseEntity.ok(ApiResponse.success(doctors, "Found " + doctors.size() + " matching doctors in department"));
    }

    @GetMapping("/{id}/specializations")
    public ResponseEntity<ApiResponse<List<String>>> getDepartmentSpecializations(@PathVariable Long id) {
        List<String> specializations = departmentService.getSpecializationsByDepartment(id);
        return ResponseEntity.ok(ApiResponse.success(specializations, "Fetched department specializations successfully"));
    }

    // ==========================================
    // DEPARTMENT AVAILABILITY (FR-11, FR-12, FR-13)
    // ==========================================

    @GetMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<DepartmentAvailabilityDto>> getDepartmentAvailability(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        DepartmentAvailabilityDto availability = departmentService.getDepartmentAvailability(id, targetDate);
        return ResponseEntity.ok(ApiResponse.success(availability, "Fetched department availability successfully"));
    }

    // ==========================================
    // DEPARTMENT MANAGEMENT (FR-19, FR-23)
    // ==========================================

    @PostMapping
    public ResponseEntity<ApiResponse<Department>> createDepartment(@RequestBody Department department) {
        Department created = departmentService.createDepartment(department);
        return new ResponseEntity<>(ApiResponse.success(created, "Department created successfully"), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Department>> updateDepartment(@PathVariable Long id, @RequestBody Department details) {
        Department updated = departmentService.updateDepartment(id, details);
        return ResponseEntity.ok(ApiResponse.success(updated, "Department updated successfully"));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Department>> toggleDepartmentStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        Department updated = departmentService.toggleDepartmentStatus(id, active);
        return ResponseEntity.ok(ApiResponse.success(updated, "Department status updated to " + (active ? "ACTIVE" : "INACTIVE")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Department deactivated successfully"));
    }
}
