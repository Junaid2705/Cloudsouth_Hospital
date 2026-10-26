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
        boolean isReturning = patient.getEmail() != null && patientRepository.findByEmail(patient.getEmail()).isPresent();
        patient.setIsReturningPatient(isReturning);
        return patientRepository.save(patient);
    }

    public Patient updatePatient(Long id, Patient details) {
        Patient patient = getPatientById(id);
        patient.setFirstName(details.getFirstName());
        patient.setLastName(details.getLastName());
        patient.setPhone(details.getPhone());
        patient.setAddress(details.getAddress());
        patient.setDateOfBirth(details.getDateOfBirth());
        patient.setBloodGroup(details.getBloodGroup());
        return patientRepository.save(patient);
    }

    public void deletePatient(Long id) {
        Patient patient = getPatientById(id);
        patientRepository.delete(patient);
    }
}
