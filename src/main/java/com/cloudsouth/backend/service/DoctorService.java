package com.cloudsouth.backend.service;

import com.cloudsouth.backend.dto.DoctorAvailabilityDto;
import com.cloudsouth.backend.dto.TimeSlotDto;
import com.cloudsouth.backend.exception.ResourceNotFoundException;
import com.cloudsouth.backend.model.Doctor;
import com.cloudsouth.backend.model.DoctorSchedule;
import com.cloudsouth.backend.repository.DoctorRepository;
import com.cloudsouth.backend.repository.DoctorScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

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

    public List<Doctor> getDoctorsByDepartment(String department) {
        return doctorRepository.findByDepartmentIgnoreCaseAndIsActiveTrue(department);
    }

    public List<Doctor> getDoctorsByDepartmentId(Long departmentId) {
        return doctorRepository.findByDepartmentIdAndIsActiveTrue(departmentId);
    }

    public List<Doctor> searchDoctors(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllActiveDoctors();
        }
        return doctorRepository.searchDoctors(query.trim());
    }

    public Doctor createDoctor(Doctor doctor) {
        if (doctor.getIsActive() == null) {
            doctor.setIsActive(true);
        }
        if (doctor.getConsultationFee() == null) {
            doctor.setConsultationFee(50.00); // Standard $50 as per BRD
        }
        return doctorRepository.save(doctor);
    }

    public Doctor updateDoctor(Long id, Doctor doctorDetails) {
        Doctor doctor = getDoctorById(id);
        doctor.setName(doctorDetails.getName());
        doctor.setSpecialization(doctorDetails.getSpecialization());
        doctor.setDepartment(doctorDetails.getDepartment());
        if (doctorDetails.getDepartmentId() != null) {
            doctor.setDepartmentId(doctorDetails.getDepartmentId());
        }
        doctor.setQualification(doctorDetails.getQualification());
        doctor.setExperienceYears(doctorDetails.getExperienceYears());
        if (doctorDetails.getConsultationFee() != null) {
            doctor.setConsultationFee(doctorDetails.getConsultationFee());
        }
        if (doctorDetails.getPhotoUrl() != null) {
            doctor.setPhotoUrl(doctorDetails.getPhotoUrl());
        }
        if (doctorDetails.getAvailableDays() != null) {
            doctor.setAvailableDays(doctorDetails.getAvailableDays());
        }
        if (doctorDetails.getAvailableTimeSlots() != null) {
            doctor.setAvailableTimeSlots(doctorDetails.getAvailableTimeSlots());
        }
        if (doctorDetails.getEmail() != null) {
            doctor.setEmail(doctorDetails.getEmail());
        }
        if (doctorDetails.getPhone() != null) {
            doctor.setPhone(doctorDetails.getPhone());
        }
        if (doctorDetails.getIsActive() != null) {
            doctor.setIsActive(doctorDetails.getIsActive());
        }
        return doctorRepository.save(doctor);
    }

    public void deleteDoctor(Long id) {
        Doctor doctor = getDoctorById(id);
        doctor.setIsActive(false);
        doctorRepository.save(doctor);
    }

    // ==========================================
    // SCHEDULE & AVAILABILITY LOGIC
    // ==========================================

    public List<DoctorSchedule> getDoctorSchedules(Long doctorId) {
        getDoctorById(doctorId); // Validate doctor exists
        return doctorScheduleRepository.findByDoctorId(doctorId);
    }

    public DoctorSchedule addDoctorSchedule(Long doctorId, DoctorSchedule schedule) {
        getDoctorById(doctorId); // Validate doctor exists
        schedule.setDoctorId(doctorId);
        if (schedule.getIsActive() == null) {
            schedule.setIsActive(true);
        }
        if (schedule.getSlotDurationMinutes() == null || schedule.getSlotDurationMinutes() <= 0) {
            schedule.setSlotDurationMinutes(60);
        }
        return doctorScheduleRepository.save(schedule);
    }

    public DoctorSchedule updateDoctorSchedule(Long scheduleId, DoctorSchedule scheduleDetails) {
        DoctorSchedule schedule = doctorScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor schedule not found with id: " + scheduleId));

        schedule.setDayOfWeek(scheduleDetails.getDayOfWeek().toUpperCase());
        schedule.setStartTime(scheduleDetails.getStartTime());
        schedule.setEndTime(scheduleDetails.getEndTime());
        if (scheduleDetails.getSlotDurationMinutes() != null) {
            schedule.setSlotDurationMinutes(scheduleDetails.getSlotDurationMinutes());
        }
        if (scheduleDetails.getIsActive() != null) {
            schedule.setIsActive(scheduleDetails.getIsActive());
        }
        return doctorScheduleRepository.save(schedule);
    }

    public void deleteDoctorSchedule(Long scheduleId) {
        DoctorSchedule schedule = doctorScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor schedule not found with id: " + scheduleId));
        doctorScheduleRepository.delete(schedule);
    }

    /**
     * Checks doctor availability for a target date and generates time slots
     */
    public DoctorAvailabilityDto getDoctorAvailability(Long doctorId, LocalDate targetDate) {
        Doctor doctor = getDoctorById(doctorId);
        LocalDate date = (targetDate != null) ? targetDate : LocalDate.now();
        String dayOfWeek = date.getDayOfWeek().name();

        if (Boolean.FALSE.equals(doctor.getIsActive())) {
            return DoctorAvailabilityDto.builder()
                    .doctorId(doctor.getId())
                    .doctorName(doctor.getName())
                    .department(doctor.getDepartment())
                    .specialization(doctor.getSpecialization())
                    .date(date)
                    .dayOfWeek(dayOfWeek)
                    .consultationFee(doctor.getConsultationFee())
                    .isAvailable(false)
                    .availableSlots(new ArrayList<>())
                    .message("Doctor is currently inactive")
                    .build();
        }

        if (date.isBefore(LocalDate.now())) {
            return DoctorAvailabilityDto.builder()
                    .doctorId(doctor.getId())
                    .doctorName(doctor.getName())
                    .department(doctor.getDepartment())
                    .specialization(doctor.getSpecialization())
                    .date(date)
                    .dayOfWeek(dayOfWeek)
                    .consultationFee(doctor.getConsultationFee())
                    .isAvailable(false)
                    .availableSlots(new ArrayList<>())
                    .message("Cannot check availability for past dates")
                    .build();
        }

        // Query active schedules for this day of week
        List<DoctorSchedule> schedules = doctorScheduleRepository
                .findByDoctorIdAndDayOfWeekIgnoreCaseAndIsActiveTrue(doctorId, dayOfWeek);

        List<TimeSlotDto> slots = new ArrayList<>();

        if (schedules.isEmpty()) {
            // Check fallback: if Sunday and no schedule, doctor off
            // Else, if doctor has availableDays mentioning this day, provide standard hospital shifts
            boolean dayMatches = doctor.getAvailableDays() != null && 
                    doctor.getAvailableDays().toUpperCase().contains(dayOfWeek.substring(0, 3));

            if (dayMatches || (!dayOfWeek.equals("SUNDAY") && schedules.isEmpty())) {
                // Generate default shifts 09:00 - 13:00, 14:00 - 17:00
                slots.addAll(generateSlotsBetween(LocalTime.of(9, 0), LocalTime.of(13, 0), 60, date));
                slots.addAll(generateSlotsBetween(LocalTime.of(14, 0), LocalTime.of(17, 0), 60, date));
            }
        } else {
            for (DoctorSchedule schedule : schedules) {
                try {
                    LocalTime start = LocalTime.parse(schedule.getStartTime(), TIME_FORMATTER);
                    LocalTime end = LocalTime.parse(schedule.getEndTime(), TIME_FORMATTER);
                    int duration = schedule.getSlotDurationMinutes() != null ? schedule.getSlotDurationMinutes() : 60;
                    slots.addAll(generateSlotsBetween(start, end, duration, date));
                } catch (Exception e) {
                    // ignore formatting issues
                }
            }
        }

        boolean hasAvailableSlots = slots.stream().anyMatch(TimeSlotDto::isAvailable);

        return DoctorAvailabilityDto.builder()
                .doctorId(doctor.getId())
                .doctorName(doctor.getName())
                .department(doctor.getDepartment())
                .specialization(doctor.getSpecialization())
                .date(date)
                .dayOfWeek(dayOfWeek)
                .consultationFee(doctor.getConsultationFee())
                .isAvailable(hasAvailableSlots)
                .availableSlots(slots)
                .message(hasAvailableSlots ? "Available time slots retrieved successfully" : "No available slots on this date")
                .build();
    }

    private List<TimeSlotDto> generateSlotsBetween(LocalTime start, LocalTime end, int durationMinutes, LocalDate date) {
        List<TimeSlotDto> slotList = new ArrayList<>();
        LocalTime current = start;
        LocalTime now = LocalTime.now();
        boolean isToday = date.equals(LocalDate.now());

        while (current.plusMinutes(durationMinutes).isBefore(end) || current.plusMinutes(durationMinutes).equals(end)) {
            LocalTime next = current.plusMinutes(durationMinutes);
            String slotLabel = current.format(TIME_FORMATTER) + " - " + next.format(TIME_FORMATTER);

            // If appointment is today and the slot is already in the past, mark as unavailable
            boolean isSlotPast = isToday && current.isBefore(now);
            boolean available = !isSlotPast;

            slotList.add(TimeSlotDto.builder()
                    .slot(slotLabel)
                    .startTime(current.format(TIME_FORMATTER))
                    .endTime(next.format(TIME_FORMATTER))
                    .isAvailable(available)
                    .status(available ? "AVAILABLE" : "UNAVAILABLE")
                    .build());

            current = next;
        }

        return slotList;
    }
}
