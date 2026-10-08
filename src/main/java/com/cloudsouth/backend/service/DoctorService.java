package com.cloudsouth.backend.service;

import com.cloudsouth.backend.exception.ResourceNotFoundException;
import com.cloudsouth.backend.model.Doctor;
import com.cloudsouth.backend.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    public List<Doctor> getAllActiveDoctors() {
        return doctorRepository.findByIsActiveTrue();
    }

    public Doctor getDoctorById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
    }

    public Doctor createDoctor(Doctor doctor) {
        doctor.setIsActive(true); // Default to active when created
        return doctorRepository.save(doctor);
    }

    public Doctor updateDoctor(Long id, Doctor doctorDetails) {
        Doctor doctor = getDoctorById(id);
        doctor.setName(doctorDetails.getName());
        doctor.setSpecialization(doctorDetails.getSpecialization());
        doctor.setDepartment(doctorDetails.getDepartment());
        doctor.setQualification(doctorDetails.getQualification());
        doctor.setExperienceYears(doctorDetails.getExperienceYears());
        if (doctorDetails.getIsActive() != null) {
            doctor.setIsActive(doctorDetails.getIsActive());
        }
        return doctorRepository.save(doctor);
    }

    public void deleteDoctor(Long id) {
        Doctor doctor = getDoctorById(id);
        doctorRepository.delete(doctor);
    }
}
