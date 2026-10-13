package com.cloudsouth.backend.repository;

import com.cloudsouth.backend.model.Doctor;
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
    List<Doctor> findByIsActiveTrue();

    @Query("SELECT d FROM Doctor d WHERE d.isActive = true AND " +
           "(LOWER(d.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(d.specialization) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(d.department) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Doctor> searchDoctors(@Param("query") String query);
}
