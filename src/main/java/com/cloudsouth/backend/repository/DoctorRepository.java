package com.cloudsouth.backend.repository;

import com.cloudsouth.backend.model.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findByDepartment(String department);
    List<Doctor> findByDepartmentIgnoreCaseAndIsActiveTrue(String department);
    List<Doctor> findByDepartmentIdAndIsActiveTrue(Long departmentId);
    List<Doctor> findBySpecializationIgnoreCaseAndIsActiveTrue(String specialization);
    List<Doctor> findByExperienceYearsGreaterThanEqualAndIsActiveTrue(Integer minExperienceYears);
    List<Doctor> findByIsActiveTrue();

    Page<Doctor> findByIsActiveTrue(Pageable pageable);

    long countByDepartmentIgnoreCaseAndIsActiveTrue(String department);
    long countByDepartmentIdAndIsActiveTrue(Long departmentId);

    @Query("SELECT DISTINCT d.specialization FROM Doctor d WHERE d.isActive = true ORDER BY d.specialization ASC")
    List<String> findDistinctSpecializations();

    @Query("SELECT DISTINCT d.department FROM Doctor d WHERE d.isActive = true ORDER BY d.department ASC")
    List<String> findDistinctDepartments();

    @Query("SELECT DISTINCT d.specialization FROM Doctor d WHERE d.departmentId = :deptId AND d.isActive = true ORDER BY d.specialization ASC")
    List<String> findSpecializationsByDepartmentId(@Param("deptId") Long deptId);

    @Query("SELECT d FROM Doctor d WHERE d.isActive = true AND " +
           "(LOWER(d.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(d.specialization) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(d.department) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Doctor> searchDoctors(@Param("query") String query);
}
