package com.cloudsouth.backend.controller;

import com.cloudsouth.backend.dto.ApiResponse;
import com.cloudsouth.backend.model.Department;
import com.cloudsouth.backend.model.Doctor;
import com.cloudsouth.backend.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Department>>> getAllDepartments(
            @RequestParam(required = false, defaultValue = "true") boolean activeOnly) {
        List<Department> departments = departmentService.getAllDepartments(activeOnly);
        return ResponseEntity.ok(ApiResponse.success(departments, "Fetched departments successfully"));
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

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Department deactivated successfully"));
    }
}
