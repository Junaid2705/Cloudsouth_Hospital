package com.cloudsouth.backend.service;

import com.cloudsouth.backend.dto.DoctorAvailabilityDto;
import com.cloudsouth.backend.dto.DoctorStatsDto;
import com.cloudsouth.backend.dto.TimeSlotDto;
import com.cloudsouth.backend.exception.ResourceNotFoundException;
import com.cloudsouth.backend.model.Department;
import com.cloudsouth.backend.model.Doctor;
import com.cloudsouth.backend.model.DoctorSchedule;
import com.cloudsouth.backend.repository.DepartmentRepository;
import com.cloudsouth.backend.repository.DoctorRepository;
import com.cloudsouth.backend.repository.DoctorScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
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

    /**
     * Comprehensive multi-criteria filtering matching the React frontend filters:
     * - Department pill filter
     * - Available Day dropdown (All Days, Monday, Tuesday, etc.)
     * - Availability / Shift dropdown (All Doctors, Available Today, Morning Shift, Evening Shift)
     * - Keyword search bar (Doctor Name or Specialization)
     */
    public List<Doctor> filterDoctors(String department, String day, String shift, String query, Boolean activeOnly) {
        List<Doctor> doctors = (activeOnly != null && activeOnly) ? getAllActiveDoctors() : getAllDoctors();

        // 1. Filter by Department
        if (department != null && !department.trim().isEmpty() && !department.equalsIgnoreCase("All") && !department.equalsIgnoreCase("All Departments")) {
            String deptFilter = department.trim().toLowerCase();
            doctors = doctors.stream()
                    .filter(d -> d.getDepartment() != null && (
                            d.getDepartment().toLowerCase().contains(deptFilter) ||
                            deptFilter.contains(d.getDepartment().toLowerCase())
                    ))
                    .collect(Collectors.toList());
        }

        // 2. Filter by Day of Week
        if (day != null && !day.trim().isEmpty() && !day.equalsIgnoreCase("All") && !day.equalsIgnoreCase("All Days")) {
            String dayFilter = day.trim().toLowerCase();
            doctors = doctors.stream()
                    .filter(d -> d.getAvailableDays() != null && d.getAvailableDays().toLowerCase().contains(dayFilter))
                    .collect(Collectors.toList());
        }

        // 3. Filter by Shift or Availability (Morning Shift / Evening Shift / Available Today)
        if (shift != null && !shift.trim().isEmpty() && !shift.equalsIgnoreCase("All") && !shift.equalsIgnoreCase("All Doctors")) {
            if (shift.equalsIgnoreCase("Available Today")) {
                String todayName = LocalDate.now().getDayOfWeek().name().toLowerCase();
                String todayShort = todayName.substring(0, 3);
                doctors = doctors.stream()
                        .filter(d -> d.getAvailableDays() != null && (
                                d.getAvailableDays().toLowerCase().contains(todayName) ||
                                d.getAvailableDays().toLowerCase().contains(todayShort)
                        ))
                        .collect(Collectors.toList());
            } else {
                String shiftFilter = shift.trim().toLowerCase();
                doctors = doctors.stream()
                        .filter(d -> (d.getTimingShift() != null && d.getTimingShift().toLowerCase().contains(shiftFilter)) ||
                                     (d.getTimings() != null && d.getTimings().toLowerCase().contains(shiftFilter)))
                        .collect(Collectors.toList());
            }
        }

        // 4. Keyword Search (Name, Specialization, Qualification)
        if (query != null && !query.trim().isEmpty()) {
            String q = query.trim().toLowerCase();
            doctors = doctors.stream()
                    .filter(d -> (d.getName() != null && d.getName().toLowerCase().contains(q)) ||
                                 (d.getSpecialization() != null && d.getSpecialization().toLowerCase().contains(q)) ||
                                 (d.getQualification() != null && d.getQualification().toLowerCase().contains(q)) ||
                                 (d.getDepartment() != null && d.getDepartment().toLowerCase().contains(q)))
                    .collect(Collectors.toList());
        }

        return doctors;
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

    public DoctorStatsDto getDoctorStats() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("All Departments", 85);

        List<Department> depts = departmentRepository.findByIsActiveTrue();
        for (Department d : depts) {
            counts.put(d.getName(), d.getTotalDoctors() != null ? d.getTotalDoctors() : 10);
        }

        return DoctorStatsDto.builder()
                .specialistDoctors("85+")
                .hospitalBeds("1,000+")
                .totalDoctorsCount(85)
                .departmentCounts(counts)
                .build();
    }

    public Doctor createDoctor(Doctor doctor) {
        if (doctor.getIsActive() == null) {
            doctor.setIsActive(true);
        }
        if (doctor.getConsultationFee() == null) {
            doctor.setConsultationFee(50.00); // Standard $50 as per BRD
        }
        if (doctor.getReturningFee() == null) {
            doctor.setReturningFee(45.00); // 10% discount for returning patients
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
        if (doctorDetails.getReturningFee() != null) {
            doctor.setReturningFee(doctorDetails.getReturningFee());
        }
        if (doctorDetails.getPhotoUrl() != null) {
            doctor.setPhotoUrl(doctorDetails.getPhotoUrl());
        }
        if (doctorDetails.getAvailableDays() != null) {
            doctor.setAvailableDays(doctorDetails.getAvailableDays());
        }
        if (doctorDetails.getTimings() != null) {
            doctor.setTimings(doctorDetails.getTimings());
        }
        if (doctorDetails.getTimingShift() != null) {
            doctor.setTimingShift(doctorDetails.getTimingShift());
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
        getDoctorById(doctorId);
        return doctorScheduleRepository.findByDoctorId(doctorId);
    }

    public DoctorSchedule addDoctorSchedule(Long doctorId, DoctorSchedule schedule) {
        getDoctorById(doctorId);
        schedule.setDoctorId(doctorId);
        if (schedule.getIsActive() == null) {
            schedule.setIsActive(true);
        }
        if (schedule.getSlotDurationMinutes() == null || schedule.getSlotDurationMinutes() <= 0) {
            schedule.setSlotDurationMinutes(45); // 45-min slots match the frontend intervals
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
            boolean dayMatches = doctor.getAvailableDays() != null &&
                    doctor.getAvailableDays().toUpperCase().contains(dayOfWeek.substring(0, 3));

            if (dayMatches || (!dayOfWeek.equals("SUNDAY") && schedules.isEmpty())) {
                slots.addAll(generateSlotsBetween(LocalTime.of(9, 0), LocalTime.of(13, 0), 45, date));
                slots.addAll(generateSlotsBetween(LocalTime.of(14, 0), LocalTime.of(18, 0), 45, date));
            }
        } else {
            for (DoctorSchedule schedule : schedules) {
                try {
                    LocalTime start = LocalTime.parse(schedule.getStartTime(), TIME_FORMATTER);
                    LocalTime end = LocalTime.parse(schedule.getEndTime(), TIME_FORMATTER);
                    int duration = schedule.getSlotDurationMinutes() != null ? schedule.getSlotDurationMinutes() : 45;
                    slots.addAll(generateSlotsBetween(start, end, duration, date));
                } catch (Exception e) {
                    // skip malformed schedule
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
