package com.cloudsouth.backend.controller;

import com.cloudsouth.backend.dto.ApiResponse;
import com.cloudsouth.backend.model.Patient;
import com.cloudsouth.backend.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Patient>>> getAllPatients() {
        List<Patient> patients = patientService.getAllPatients();
        return ResponseEntity.ok(ApiResponse.success(patients, "Fetched all patients successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Patient>> getPatientById(@PathVariable Long id) {
        Patient patient = patientService.getPatientById(id);
        return ResponseEntity.ok(ApiResponse.success(patient, "Fetched patient successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Patient>> createPatient(@RequestBody Patient patient) {
        Patient createdPatient = patientService.createPatient(patient);
        return new ResponseEntity<>(ApiResponse.success(createdPatient, "Patient registered successfully"), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Patient>> updatePatient(@PathVariable Long id, @RequestBody Patient patientDetails) {
        Patient updatedPatient = patientService.updatePatient(id, patientDetails);
        return ResponseEntity.ok(ApiResponse.success(updatedPatient, "Patient updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Patient deleted successfully"));
    }
}
