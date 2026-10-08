package com.cloudsouth.backend.controller;

import com.cloudsouth.backend.dto.ApiResponse;
import com.cloudsouth.backend.model.Doctor;
import com.cloudsouth.backend.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Doctor>>> getAllDoctors(@RequestParam(required = false, defaultValue = "true") boolean activeOnly) {
        List<Doctor> doctors = activeOnly ? doctorService.getAllActiveDoctors() : doctorService.getAllDoctors();
        return ResponseEntity.ok(ApiResponse.success(doctors, "Fetched doctors successfully"));
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
}
