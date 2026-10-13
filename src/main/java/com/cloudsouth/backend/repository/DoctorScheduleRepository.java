package com.cloudsouth.backend.repository;

import com.cloudsouth.backend.model.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Long> {
    List<DoctorSchedule> findByDoctorIdAndIsActiveTrue(Long doctorId);
    List<DoctorSchedule> findByDoctorIdAndDayOfWeekIgnoreCaseAndIsActiveTrue(Long doctorId, String dayOfWeek);
    List<DoctorSchedule> findByDoctorId(Long doctorId);
}
