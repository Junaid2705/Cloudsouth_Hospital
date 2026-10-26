package com.cloudsouth.backend.repository;

import com.cloudsouth.backend.model.BookedSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookedSlotRepository extends JpaRepository<BookedSlot, Long> {
    List<BookedSlot> findByDoctorIdAndBookingDateAndStatusNot(Long doctorId, LocalDate bookingDate, String status);
    List<BookedSlot> findByDoctorIdAndBookingDate(Long doctorId, LocalDate bookingDate);
    List<BookedSlot> findByDoctorIdAndBookingDateAndStatus(Long doctorId, LocalDate bookingDate, String status);
    Optional<BookedSlot> findByDoctorIdAndBookingDateAndSlotAndStatusNot(Long doctorId, LocalDate bookingDate, String slot, String status);
    Optional<BookedSlot> findByDoctorIdAndBookingDateAndSlot(Long doctorId, LocalDate bookingDate, String slot);
    boolean existsByDoctorIdAndBookingDateAndSlotAndStatusNot(Long doctorId, LocalDate bookingDate, String slot, String status);
}
