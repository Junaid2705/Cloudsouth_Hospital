package com.cloudsouth.backend.service;

import com.cloudsouth.backend.exception.ResourceNotFoundException;
import com.cloudsouth.backend.model.Department;
import com.cloudsouth.backend.model.Doctor;
import com.cloudsouth.backend.repository.DepartmentRepository;
import com.cloudsouth.backend.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;

    public List<Department> getAllDepartments(boolean activeOnly) {
        return activeOnly ? departmentRepository.findByIsActiveTrue() : departmentRepository.findAll();
    }

    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
    }

    public List<Doctor> getDoctorsByDepartmentId(Long id) {
        Department dept = getDepartmentById(id);
        // Find by department ID or by department name as fallback
        List<Doctor> doctors = doctorRepository.findByDepartmentIdAndIsActiveTrue(id);
        if (doctors.isEmpty()) {
            doctors = doctorRepository.findByDepartmentIgnoreCaseAndIsActiveTrue(dept.getName());
        }
        return doctors;
    }

    public Department createDepartment(Department department) {
        if (department.getIsActive() == null) {
            department.setIsActive(true);
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

    public void deleteDepartment(Long id) {
        Department dept = getDepartmentById(id);
        dept.setIsActive(false);
        departmentRepository.save(dept);
    }
}
