package com.cloudsouth.backend.repository;

import com.cloudsouth.backend.model.DoctorLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorLeaveRepository extends JpaRepository<DoctorLeave, Long> {
    List<DoctorLeave> findByDoctorId(Long doctorId);
    List<DoctorLeave> findByDoctorIdAndLeaveDateGreaterThanEqual(Long doctorId, LocalDate fromDate);
    List<DoctorLeave> findByDoctorIdAndLeaveDateBetween(Long doctorId, LocalDate startDate, LocalDate endDate);
    List<DoctorLeave> findByLeaveDate(LocalDate leaveDate);
    Optional<DoctorLeave> findByDoctorIdAndLeaveDate(Long doctorId, LocalDate leaveDate);
    boolean existsByDoctorIdAndLeaveDate(Long doctorId, LocalDate leaveDate);
}
