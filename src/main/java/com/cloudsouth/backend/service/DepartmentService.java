package com.cloudsouth.backend.service;

import com.cloudsouth.backend.dto.DepartmentAvailabilityDto;
import com.cloudsouth.backend.dto.DepartmentStatsDto;
import com.cloudsouth.backend.dto.DepartmentSummaryDto;
import com.cloudsouth.backend.dto.DoctorAvailabilityDto;
import com.cloudsouth.backend.dto.TimeSlotDto;
import com.cloudsouth.backend.exception.ResourceNotFoundException;
import com.cloudsouth.backend.model.Department;
import com.cloudsouth.backend.model.Doctor;
import com.cloudsouth.backend.repository.DepartmentRepository;
import com.cloudsouth.backend.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorService doctorService;

    // ==========================================
    // DEPARTMENT RETRIEVAL & SUMMARIES (FR-10, FR-11)
    // ==========================================

    public List<Department> getAllDepartments(boolean activeOnly) {
        return activeOnly ? departmentRepository.findByIsActiveTrue() : departmentRepository.findAll();
    }

    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
    }

    public Department getDepartmentByCode(String code) {
        return departmentRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with code: " + code));
    }

    public List<DepartmentSummaryDto> getDepartmentSummaries() {
        List<Department> departments = departmentRepository.findByIsActiveTrue();
        List<DepartmentSummaryDto> summaries = new ArrayList<>();

        for (Department dept : departments) {
            List<Doctor> doctors = getDoctorsByDepartmentId(dept.getId());
            List<String> specs = doctors.stream()
                    .map(Doctor::getSpecialization)
                    .filter(s -> s != null && !s.isBlank())
                    .distinct()
                    .toList();

            summaries.add(DepartmentSummaryDto.builder()
                    .id(dept.getId())
                    .name(dept.getName())
                    .code(dept.getCode())
                    .icon(dept.getIcon())
                    .headDoctorName(dept.getHeadDoctorName())
                    .targetCapacity(dept.getTotalDoctors())
                    .activeDoctorCount(doctors.size())
                    .specializations(specs)
                    .build());
        }

        return summaries;
    }
 
    public DepartmentStatsDto getDepartmentStats() {
        List<DepartmentSummaryDto> summaries = getDepartmentSummaries();
        int totalActiveDoctors = summaries.stream()
                .mapToInt(d -> d.getActiveDoctorCount() != null ? d.getActiveDoctorCount() : 0)
                .sum();
        int totalCapacity = summaries.stream()
                .mapToInt(d -> d.getTargetCapacity() != null ? d.getTargetCapacity() : 0)
                .sum();

        return DepartmentStatsDto.builder()
                .totalDepartments(summaries.size())
                .hospitalCapacityBeds("1,000+")
                .totalTargetDoctorCapacity(totalCapacity > 0 ? totalCapacity : 85)
                .totalActiveDoctors(totalActiveDoctors)
                .departments(summaries)
                .build();
    }

    public List<Doctor> getDoctorsByDepartmentId(Long id) {
        Department dept = getDepartmentById(id);
        List<Doctor> doctors = doctorRepository.findByDepartmentIdAndIsActiveTrue(id);
        if (doctors.isEmpty()) {
            doctors = doctorRepository.findByDepartmentIgnoreCaseAndIsActiveTrue(dept.getName());
        }
        return doctors;
    }

    public List<String> getSpecializationsByDepartment(Long id) {
        List<Doctor> doctors = getDoctorsByDepartmentId(id);
        return doctors.stream()
                .map(Doctor::getSpecialization)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .sorted()
                .toList();
    }

    public List<Doctor> searchDoctorsInDepartment(Long departmentId, String query) {
        List<Doctor> allInDept = getDoctorsByDepartmentId(departmentId);
        if (query == null || query.isBlank()) {
            return allInDept;
        }
        String q = query.trim().toLowerCase();
        return allInDept.stream()
                .filter(d -> (d.getName() != null && d.getName().toLowerCase().contains(q)) ||
                             (d.getSpecialization() != null && d.getSpecialization().toLowerCase().contains(q)) ||
                             (d.getQualification() != null && d.getQualification().toLowerCase().contains(q)))
                .toList();
    }

    // ==========================================
    // DEPARTMENT REAL-TIME AVAILABILITY (FR-11, FR-12, FR-13)
    // ==========================================

    public DepartmentAvailabilityDto getDepartmentAvailability(Long departmentId, LocalDate targetDate) {
        Department dept = getDepartmentById(departmentId);
        LocalDate date = (targetDate != null) ? targetDate : LocalDate.now();

        List<Doctor> doctors = getDoctorsByDepartmentId(departmentId);
        List<DoctorAvailabilityDto> availableDoctors = new ArrayList<>();
        int totalOpenSlots = 0;

        for (Doctor doc : doctors) {
            DoctorAvailabilityDto avail = doctorService.getDoctorAvailability(doc.getId(), date);
            if (avail.isAvailable()) {
                availableDoctors.add(avail);
                totalOpenSlots += (int) avail.getAvailableSlots().stream().filter(TimeSlotDto::isAvailable).count();
            }
        }

        return DepartmentAvailabilityDto.builder()
                .departmentId(dept.getId())
                .departmentName(dept.getName())
                .departmentCode(dept.getCode())
                .date(date)
                .dayOfWeek(date.getDayOfWeek().name())
                .totalDoctorsInDepartment(doctors.size())
                .availableDoctorsCount(availableDoctors.size())
                .totalAvailableSlotsCount(totalOpenSlots)
                .availableDoctors(availableDoctors)
                .build();
    }

    // ==========================================
    // DEPARTMENT MANAGEMENT & LIFECYCLE (FR-19, FR-23)
    // ==========================================

    public Department createDepartment(Department department) {
        if (department.getName() == null || department.getName().isBlank()) {
            throw new IllegalArgumentException("Department name is required");
        }
        if (department.getCode() == null || department.getCode().isBlank()) {
            throw new IllegalArgumentException("Department code is required");
        }
        if (department.getIsActive() == null) {
            department.setIsActive(true);
        }
        if (departmentRepository.findByCodeIgnoreCase(department.getCode()).isPresent()) {
            throw new IllegalArgumentException("Department with code " + department.getCode() + " already exists");
        }
        return departmentRepository.save(department);
    }

    public Department updateDepartment(Long id, Department details) {
        Department dept = getDepartmentById(id);
        dept.setName(details.getName());
        dept.setCode(details.getCode());
        dept.setDescription(details.getDescription());
        dept.setIcon(details.getIcon());
        dept.setHeadDoctorName(details.getHeadDoctorName());
        dept.setTotalDoctors(details.getTotalDoctors());
        if (details.getIsActive() != null) {
            dept.setIsActive(details.getIsActive());
        }
        return departmentRepository.save(dept);
    }

    public Department toggleDepartmentStatus(Long id, boolean active) {
        Department dept = getDepartmentById(id);
        dept.setIsActive(active);
        return departmentRepository.save(dept);
    }

    public void deleteDepartment(Long id) {
        Department dept = getDepartmentById(id);
        dept.setIsActive(false);
        departmentRepository.save(dept);
    }
}
