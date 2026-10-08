package com.cloudsouth.backend.service;

import com.cloudsouth.backend.exception.ResourceNotFoundException;
import com.cloudsouth.backend.model.Patient;
import com.cloudsouth.backend.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public Patient getPatientById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
    }

    public Patient createPatient(Patient patient) {
        // Business Rule from PDF: Check if returning patient based on email (simplified)
        if (patientRepository.findByEmail(patient.getEmail()).isPresent()) {
            patient.setIsReturningPatient(true);
        } else {
            patient.setIsReturningPatient(false);
        }
        return patientRepository.save(patient);
    }

    public Patient updatePatient(Long id, Patient patientDetails) {
        Patient patient = getPatientById(id);
        patient.setFirstName(patientDetails.getFirstName());
        patient.setLastName(patientDetails.getLastName());
        patient.setPhone(patientDetails.getPhone());
        patient.setAddress(patientDetails.getAddress());
        patient.setDateOfBirth(patientDetails.getDateOfBirth());
        patient.setBloodGroup(patientDetails.getBloodGroup());
        return patientRepository.save(patient);
    }

    public void deletePatient(Long id) {
        Patient patient = getPatientById(id);
        patientRepository.delete(patient);
    }
}
