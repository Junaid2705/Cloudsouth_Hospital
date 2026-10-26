package com.cloudsouth.backend.service;

import com.cloudsouth.backend.dto.*;
import com.cloudsouth.backend.exception.ResourceNotFoundException;
import com.cloudsouth.backend.model.*;
import com.cloudsouth.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final DoctorLeaveRepository doctorLeaveRepository;
    private final BookedSlotRepository bookedSlotRepository;
    private final PatientRepository patientRepository;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DISPLAY_TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a",
            Locale.ENGLISH);

    // ==========================================
    // BASIC DOCTOR QUERIES
    // ==========================================

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

    public List<String> getAllSpecializations() {
        return doctorRepository.findDistinctSpecializations();
    }

    public List<String> getAllDepartmentsList() {
        return doctorRepository.findDistinctDepartments();
    }

    public List<Doctor> getDoctorsBySpecialization(String specialization) {
        return doctorRepository.findBySpecializationIgnoreCaseAndIsActiveTrue(specialization);
    }

    public List<Doctor> getDoctorsByMinExperience(Integer minYears) {
        int years = (minYears != null && minYears > 0) ? minYears : 10;
        return doctorRepository.findByExperienceYearsGreaterThanEqualAndIsActiveTrue(years);
    }

    // ==========================================
    // DOCTOR FILTERING & PAGINATION (FR-04, NFR-06)
    // ==========================================

    /**
     * Highly optimized single-pass multi-criteria doctor filtering matching the
     * frontend UI:
     * - department: "General Physician", "Leg Fracture / Orthopedic Care", etc.
     * - day: "Monday", "Tuesday", etc.
     * - shift: "Morning Shift", "Evening Shift", "Available Today"
     * - query: text search (e.g. "Sharma", "Cardiology", "Trauma")
     * - minExperience: minimum years of experience
     */
    public List<Doctor> filterDoctors(String department, String day, String shift, String query,
            Integer minExperience, Double maxFee, Boolean activeOnly) {
        List<Doctor> baseList = (activeOnly != null && activeOnly) ? getAllActiveDoctors() : getAllDoctors();

        final String deptFilter = (department != null && !department.isBlank() && !department.equalsIgnoreCase("All")
                && !department.equalsIgnoreCase("All Departments"))
                        ? department.trim().toLowerCase()
                        : null;

        final String dayFilter = (day != null && !day.isBlank() && !day.equalsIgnoreCase("All")
                && !day.equalsIgnoreCase("All Days"))
                        ? day.trim().toLowerCase()
                        : null;

        final boolean isShiftFilter = (shift != null && !shift.isBlank() && !shift.equalsIgnoreCase("All")
                && !shift.equalsIgnoreCase("All Doctors"));
        final boolean isTodayFilter = isShiftFilter && shift.equalsIgnoreCase("Available Today");
        final String shiftFilter = isShiftFilter && !isTodayFilter ? shift.trim().toLowerCase() : null;

        final String todayName = isTodayFilter ? LocalDate.now().getDayOfWeek().name().toLowerCase() : null;
        final String todayShort = isTodayFilter ? todayName.substring(0, 3) : null;

        final String qFilter = (query != null && !query.isBlank()) ? query.trim().toLowerCase() : null;

        if (deptFilter == null && dayFilter == null && !isShiftFilter && qFilter == null && minExperience == null
                && maxFee == null) {
            return baseList;
        }

        return baseList.stream()
                .filter(d -> deptFilter == null
                        || (d.getDepartment() != null && d.getDepartment().toLowerCase().contains(deptFilter)))
                .filter(d -> dayFilter == null
                        || (d.getAvailableDays() != null && d.getAvailableDays().toLowerCase().contains(dayFilter)))
                .filter(d -> {
                    if (!isShiftFilter)
                        return true;
                    if (isTodayFilter) {
                        return d.getAvailableDays() != null
                                && (d.getAvailableDays().toLowerCase().contains(todayName) ||
                                        d.getAvailableDays().toLowerCase().contains(todayShort));
                    }
                    return (d.getTimingShift() != null && d.getTimingShift().toLowerCase().contains(shiftFilter)) ||
                            (d.getTimings() != null && d.getTimings().toLowerCase().contains(shiftFilter));
                })
                .filter(d -> {
                    if (qFilter == null)
                        return true;
                    return (d.getName() != null && d.getName().toLowerCase().contains(qFilter)) ||
                            (d.getSpecialization() != null && d.getSpecialization().toLowerCase().contains(qFilter)) ||
                            (d.getQualification() != null && d.getQualification().toLowerCase().contains(qFilter)) ||
                            (d.getDepartment() != null && d.getDepartment().toLowerCase().contains(qFilter));
                })
                .filter(d -> minExperience == null
                        || (d.getExperienceYears() != null && d.getExperienceYears() >= minExperience))
                .filter(d -> maxFee == null || (d.getConsultationFee() != null && d.getConsultationFee() <= maxFee))
                .toList();
    }

    public List<Doctor> filterDoctors(String department, String day, String shift, String query, Boolean activeOnly) {
        return filterDoctors(department, day, shift, query, null, null, activeOnly);
    }

    /**
     * Paginated and sorted doctor search (NFR-06 Scalability)
     */
    public PageResponse<Doctor> getDoctorsPaged(String department, String day, String shift, String query,
            Integer minExperience, Double maxFee, Boolean activeOnly,
            int page, int size, String sortBy, String sortDirection) {
        List<Doctor> filtered = filterDoctors(department, day, shift, query, minExperience, maxFee, activeOnly);

        // Sorting
        Comparator<Doctor> comparator = Comparator.comparing(Doctor::getId);
        if ("experience".equalsIgnoreCase(sortBy) || "experienceYears".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(d -> d.getExperienceYears() != null ? d.getExperienceYears() : 0);
        } else if ("name".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(d -> d.getName() != null ? d.getName() : "");
        } else if ("fee".equalsIgnoreCase(sortBy) || "consultationFee".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(d -> d.getConsultationFee() != null ? d.getConsultationFee() : 0.0);
        }

        if ("desc".equalsIgnoreCase(sortDirection)) {
            comparator = comparator.reversed();
        }

        List<Doctor> sorted = filtered.stream().sorted(comparator).toList();

        int totalElements = sorted.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int fromIndex = Math.min(page * size, totalElements);
        int toIndex = Math.min(fromIndex + size, totalElements);

        List<Doctor> pageContent = (fromIndex < totalElements) ? sorted.subList(fromIndex, toIndex)
                : Collections.emptyList();

        return PageResponse.<Doctor>builder()
                .content(pageContent)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .isFirst(page == 0)
                .isLast(page >= totalPages - 1)
                .hasNext(page < totalPages - 1)
                .hasPrevious(page > 0)
                .build();
    }

    public List<Doctor> getDoctorsByDepartment(String department) {
        return doctorRepository.findByDepartmentIgnoreCaseAndIsActiveTrue(department);
    }

    public List<Doctor> getDoctorsByDepartmentId(Long departmentId) {
        return doctorRepository.findByDepartmentIdAndIsActiveTrue(departmentId);
    }

    public List<Doctor> searchDoctors(String query) {
        if (query == null || query.isBlank()) {
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

    // ==========================================
    // DOCTOR FEE & RETURNING PATIENT DISCOUNT (BR-05, BR-06, FR-11, FR-16)
    // ==========================================

    public DoctorFeeDto calculateDoctorFee(Long doctorId, Boolean isReturning, Long patientId) {
        Doctor doctor = getDoctorById(doctorId);

        boolean isReturningPatient = false;
        if (Boolean.TRUE.equals(isReturning)) {
            isReturningPatient = true;
        } else if (patientId != null) {
            Optional<Patient> patientOpt = patientRepository.findById(patientId);
            if (patientOpt.isPresent() && Boolean.TRUE.equals(patientOpt.get().getIsReturningPatient())) {
                isReturningPatient = true;
            }
        }

        Double standardFee = doctor.getConsultationFee() != null ? doctor.getConsultationFee() : 50.00;
        Double returningFee = doctor.getReturningFee() != null ? doctor.getReturningFee() : 45.00;

        if (isReturningPatient) {
            double discountAmount = standardFee - returningFee;
            double discountPercentage = (discountAmount / standardFee) * 100.0;

            return DoctorFeeDto.builder()
                    .doctorId(doctor.getId())
                    .doctorName(doctor.getName())
                    .department(doctor.getDepartment())
                    .patientType("RETURNING")
                    .isReturningPatient(true)
                    .standardFee(standardFee)
                    .discountPercentage(Math.round(discountPercentage * 10.0) / 10.0)
                    .discountAmount(discountAmount)
                    .finalPayableFee(returningFee)
                    .currency("USD")
                    .message("Returning Patient 10% ($" + String.format("%.0f", discountAmount)
                            + ") discount automatically applied")
                    .build();
        } else {
            return DoctorFeeDto.builder()
                    .doctorId(doctor.getId())
                    .doctorName(doctor.getName())
                    .department(doctor.getDepartment())
                    .patientType("NEW")
                    .isReturningPatient(false)
                    .standardFee(standardFee)
                    .discountPercentage(0.0)
                    .discountAmount(0.0)
                    .finalPayableFee(standardFee)
                    .currency("USD")
                    .message("Standard consultation fee for new patient")
                    .build();
        }
    }

    // ==========================================
    // DOCTOR MANAGEMENT & LIFECYCLE (FR-08, BR-12)
    // ==========================================

    public Doctor createDoctor(Doctor doctor) {
        if (doctor.getName() == null || doctor.getName().isBlank()) {
            throw new IllegalArgumentException("Doctor name is required");
        }
        if (doctor.getDepartment() == null || doctor.getDepartment().isBlank()) {
            throw new IllegalArgumentException("Doctor department is required");
        }
        if (doctor.getSpecialization() == null || doctor.getSpecialization().isBlank()) {
            throw new IllegalArgumentException("Doctor specialization is required");
        }

        if (doctor.getIsActive() == null)
            doctor.setIsActive(true);
        if (doctor.getConsultationFee() == null)
            doctor.setConsultationFee(50.00);
        if (doctor.getReturningFee() == null)
            doctor.setReturningFee(45.00);

        // Link departmentId if available
        if (doctor.getDepartmentId() == null && doctor.getDepartment() != null) {
            departmentRepository.findByNameIgnoreCase(doctor.getDepartment())
                    .ifPresent(d -> doctor.setDepartmentId(d.getId()));
        }

        return doctorRepository.save(doctor);
    }

    public Doctor updateDoctor(Long id, Doctor details) {
        Doctor doctor = getDoctorById(id);
        doctor.setName(details.getName());
        doctor.setSpecialization(details.getSpecialization());
        doctor.setDepartment(details.getDepartment());
        if (details.getDepartmentId() != null)
            doctor.setDepartmentId(details.getDepartmentId());
        doctor.setQualification(details.getQualification());
        doctor.setExperienceYears(details.getExperienceYears());
        if (details.getConsultationFee() != null)
            doctor.setConsultationFee(details.getConsultationFee());
        if (details.getReturningFee() != null)
            doctor.setReturningFee(details.getReturningFee());
        if (details.getPhotoUrl() != null)
            doctor.setPhotoUrl(details.getPhotoUrl());
        if (details.getAvailableDays() != null)
            doctor.setAvailableDays(details.getAvailableDays());
        if (details.getTimings() != null)
            doctor.setTimings(details.getTimings());
        if (details.getTimingShift() != null)
            doctor.setTimingShift(details.getTimingShift());
        if (details.getAvailableTimeSlots() != null)
            doctor.setAvailableTimeSlots(details.getAvailableTimeSlots());
        if (details.getEmail() != null)
            doctor.setEmail(details.getEmail());
        if (details.getPhone() != null)
            doctor.setPhone(details.getPhone());
        if (details.getIsActive() != null)
            doctor.setIsActive(details.getIsActive());
        return doctorRepository.save(doctor);
    }

    public Doctor toggleDoctorStatus(Long id, boolean active) {
        Doctor doctor = getDoctorById(id);
        doctor.setIsActive(active);
        return doctorRepository.save(doctor);
    }

    public void deleteDoctor(Long id) {
        Doctor doctor = getDoctorById(id);
        doctor.setIsActive(false);
        doctorRepository.save(doctor);
    }

    // ==========================================
    // SCHEDULE MANAGEMENT (FR-09, BR-11)
    // ==========================================

    public List<DoctorSchedule> getDoctorSchedules(Long doctorId) {
        getDoctorById(doctorId);
        return doctorScheduleRepository.findByDoctorId(doctorId);
    }

    public DoctorSchedule addDoctorSchedule(Long doctorId, DoctorSchedule schedule) {
        getDoctorById(doctorId);
        schedule.setDoctorId(doctorId);
        if (schedule.getIsActive() == null)
            schedule.setIsActive(true);
        if (schedule.getSlotDurationMinutes() == null || schedule.getSlotDurationMinutes() <= 0) {
            schedule.setSlotDurationMinutes(45);
        }
        return doctorScheduleRepository.save(schedule);
    }

    @Transactional
    public List<DoctorSchedule> saveDoctorSchedulesBulk(Long doctorId, List<DoctorSchedule> schedules) {
        getDoctorById(doctorId);
        for (DoctorSchedule s : schedules) {
            s.setDoctorId(doctorId);
            if (s.getIsActive() == null)
                s.setIsActive(true);
            if (s.getSlotDurationMinutes() == null || s.getSlotDurationMinutes() <= 0) {
                s.setSlotDurationMinutes(45);
            }
        }
        return doctorScheduleRepository.saveAll(schedules);
    }

    public DoctorSchedule updateDoctorSchedule(Long scheduleId, DoctorSchedule details) {
        DoctorSchedule schedule = doctorScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor schedule not found with id: " + scheduleId));

        if (details.getDayOfWeek() != null)
            schedule.setDayOfWeek(details.getDayOfWeek().toUpperCase());
        if (details.getStartTime() != null)
            schedule.setStartTime(details.getStartTime());
        if (details.getEndTime() != null)
            schedule.setEndTime(details.getEndTime());
        if (details.getSlotDurationMinutes() != null) {
            schedule.setSlotDurationMinutes(details.getSlotDurationMinutes());
        }
        if (details.getIsActive() != null) {
            schedule.setIsActive(details.getIsActive());
        }
        return doctorScheduleRepository.save(schedule);
    }

    public void deleteDoctorSchedule(Long scheduleId) {
        DoctorSchedule schedule = doctorScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor schedule not found with id: " + scheduleId));
        doctorScheduleRepository.delete(schedule);
    }

    // ==========================================
    // DOCTOR LEAVE MANAGEMENT (FR-09)
    // ==========================================

    public List<DoctorLeave> getDoctorLeaves(Long doctorId) {
        getDoctorById(doctorId);
        return doctorLeaveRepository.findByDoctorId(doctorId);
    }

    public DoctorLeave addDoctorLeave(Long doctorId, DoctorLeave leave) {
        getDoctorById(doctorId);
        leave.setDoctorId(doctorId);
        if (leave.getLeaveDate() == null) {
            throw new IllegalArgumentException("Leave date must be specified");
        }
        return doctorLeaveRepository.save(leave);
    }

    @Transactional
    public List<DoctorLeave> addDoctorLeaveRange(Long doctorId, LocalDate startDate, LocalDate endDate, String reason) {
        getDoctorById(doctorId);
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Valid startDate and endDate are required");
        }

        List<DoctorLeave> leaves = new ArrayList<>();
        LocalDate curr = startDate;
        while (!curr.isAfter(endDate)) {
            if (!doctorLeaveRepository.existsByDoctorIdAndLeaveDate(doctorId, curr)) {
                leaves.add(DoctorLeave.builder()
                        .doctorId(doctorId)
                        .leaveDate(curr)
                        .reason(reason != null ? reason : "Doctor Leave")
                        .build());
            }
            curr = curr.plusDays(1);
        }
        return doctorLeaveRepository.saveAll(leaves);
    }

    public void deleteDoctorLeave(Long leaveId) {
        DoctorLeave leave = doctorLeaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor leave not found with id: " + leaveId));
        doctorLeaveRepository.delete(leave);
    }

    public List<DoctorLeave> getHospitalLeavesOnDate(LocalDate date) {
        LocalDate target = (date != null) ? date : LocalDate.now();
        return doctorLeaveRepository.findByLeaveDate(target);
    }

    // ==========================================
    // REAL-TIME AVAILABILITY & CALENDAR (FR-07, FR-12, FR-13, BR-03, VR-02)
    // ==========================================

    public DoctorAvailabilityDto getDoctorAvailability(Long doctorId, LocalDate targetDate) {
        Doctor doctor = getDoctorById(doctorId);
        LocalDate date = (targetDate != null) ? targetDate : LocalDate.now();
        String dayOfWeek = date.getDayOfWeek().name();

        if (Boolean.FALSE.equals(doctor.getIsActive())) {
            return buildAvailability(doctor, date, dayOfWeek, false, Collections.emptyList(),
                    "Doctor is currently inactive");
        }

        if (date.isBefore(LocalDate.now())) {
            return buildAvailability(doctor, date, dayOfWeek, false, Collections.emptyList(),
                    "Cannot check availability for past dates");
        }

        // Check if doctor is on leave
        Optional<DoctorLeave> leaveOpt = doctorLeaveRepository.findByDoctorIdAndLeaveDate(doctorId, date);
        if (leaveOpt.isPresent()) {
            String reason = leaveOpt.get().getReason() != null ? leaveOpt.get().getReason() : "Scheduled Leave";
            return buildAvailability(doctor, date, dayOfWeek, false, Collections.emptyList(),
                    "Doctor is on leave on this date: " + reason);
        }

        // Fetch active booked, held, and blocked slots
        cleanupExpiredHolds();
        List<BookedSlot> bookedSlots = bookedSlotRepository.findByDoctorIdAndBookingDateAndStatusNot(doctorId, date,
                "CANCELLED");
        Map<String, String> slotStatusMap = new HashMap<>();
        for (BookedSlot bs : bookedSlots) {
            slotStatusMap.put(bs.getSlot().trim(), bs.getStatus());
        }

        List<DoctorSchedule> schedules = doctorScheduleRepository
                .findByDoctorIdAndDayOfWeekIgnoreCaseAndIsActiveTrue(doctorId, dayOfWeek);

        List<TimeSlotDto> slots = new ArrayList<>();

        if (schedules.isEmpty()) {
            boolean dayMatches = doctor.getAvailableDays() != null &&
                    doctor.getAvailableDays().toUpperCase().contains(dayOfWeek.substring(0, 3));

            if (dayMatches || (!dayOfWeek.equals("SUNDAY") && schedules.isEmpty())) {
                slots.addAll(generateSlotsBetween(LocalTime.of(9, 0), LocalTime.of(13, 0), 45, date, slotStatusMap));
                slots.addAll(generateSlotsBetween(LocalTime.of(14, 0), LocalTime.of(18, 0), 45, date, slotStatusMap));
            }
        } else {
            for (DoctorSchedule schedule : schedules) {
                try {
                    LocalTime start = LocalTime.parse(schedule.getStartTime(), TIME_FORMATTER);
                    LocalTime end = LocalTime.parse(schedule.getEndTime(), TIME_FORMATTER);
                    int duration = schedule.getSlotDurationMinutes() != null ? schedule.getSlotDurationMinutes() : 45;
                    slots.addAll(generateSlotsBetween(start, end, duration, date, slotStatusMap));
                } catch (Exception ignored) {
                }
            }
        }

        boolean hasAvailable = slots.stream().anyMatch(TimeSlotDto::isAvailable);
        String msg = hasAvailable ? "Available time slots retrieved successfully" : "No available slots on this date";
        return buildAvailability(doctor, date, dayOfWeek, hasAvailable, slots, msg);
    }

    /**
     * Multi-day Availability Calendar for doctor (e.g. 14 to 30 days view for date
     * picker UI)
     */
    public DoctorCalendarAvailabilityDto getDoctorCalendarAvailability(Long doctorId, LocalDate startDate,
            LocalDate endDate) {
        Doctor doctor = getDoctorById(doctorId);
        LocalDate start = (startDate != null && !startDate.isBefore(LocalDate.now())) ? startDate : LocalDate.now();
        LocalDate end = (endDate != null && !endDate.isBefore(start)) ? endDate : start.plusDays(29);

        // Limit to 60 days max
        if (start.plusDays(60).isBefore(end)) {
            end = start.plusDays(60);
        }

        List<DayAvailabilityDto> days = new ArrayList<>();
        LocalDate curr = start;
        int availableDaysCount = 0;

        while (!curr.isAfter(end)) {
            DoctorAvailabilityDto daily = getDoctorAvailability(doctorId, curr);

            Optional<DoctorLeave> leave = doctorLeaveRepository.findByDoctorIdAndLeaveDate(doctorId, curr);
            boolean onLeave = leave.isPresent();
            String leaveReason = onLeave ? leave.get().getReason() : null;

            int totalSlots = daily.getAvailableSlots().size();
            int openSlots = (int) daily.getAvailableSlots().stream().filter(TimeSlotDto::isAvailable).count();
            int bookedSlots = totalSlots - openSlots;

            if (daily.isAvailable()) {
                availableDaysCount++;
            }

            days.add(DayAvailabilityDto.builder()
                    .date(curr)
                    .dayOfWeek(curr.getDayOfWeek().name())
                    .isAvailable(daily.isAvailable())
                    .onLeave(onLeave)
                    .leaveReason(leaveReason)
                    .totalSlotsCount(totalSlots)
                    .availableSlotsCount(openSlots)
                    .bookedSlotsCount(bookedSlots)
                    .slots(daily.getAvailableSlots())
                    .build());

            curr = curr.plusDays(1);
        }

        return DoctorCalendarAvailabilityDto.builder()
                .doctorId(doctor.getId())
                .doctorName(doctor.getName())
                .department(doctor.getDepartment())
                .specialization(doctor.getSpecialization())
                .startDate(start)
                .endDate(end)
                .totalAvailableDays(availableDaysCount)
                .days(days)
                .build();
    }

    /**
     * Finds doctors who have available slots on a specific date (FR-04, FR-13)
     */
    public List<DoctorAvailabilityDto> findAvailableDoctorsOnDate(LocalDate date, String department, String shift) {
        LocalDate target = (date != null) ? date : LocalDate.now();
        List<Doctor> candidates = filterDoctors(department, null, shift, null, true);

        List<DoctorAvailabilityDto> availableDoctors = new ArrayList<>();
        for (Doctor doc : candidates) {
            DoctorAvailabilityDto avail = getDoctorAvailability(doc.getId(), target);
            if (avail.isAvailable()) {
                availableDoctors.add(avail);
            }
        }
        return availableDoctors;
    }

    /**
     * Finds the doctor's next available date and earliest open slot within the next
     * 30 days
     */
    public NextAvailableDto getNextAvailableSlot(Long doctorId) {
        Doctor doctor = getDoctorById(doctorId);
        LocalDate today = LocalDate.now();

        for (int i = 0; i < 30; i++) {
            LocalDate checkDate = today.plusDays(i);
            DoctorAvailabilityDto avail = getDoctorAvailability(doctorId, checkDate);
            if (avail.isAvailable()) {
                Optional<TimeSlotDto> firstOpen = avail.getAvailableSlots().stream()
                        .filter(TimeSlotDto::isAvailable)
                        .findFirst();

                if (firstOpen.isPresent()) {
                    return NextAvailableDto.builder()
                            .doctorId(doctor.getId())
                            .doctorName(doctor.getName())
                            .department(doctor.getDepartment())
                            .nextAvailableDate(checkDate)
                            .dayOfWeek(checkDate.getDayOfWeek().name())
                            .firstAvailableSlot(
                                    firstOpen.get().getDisplayTime() != null ? firstOpen.get().getDisplayTime()
                                            : firstOpen.get().getSlot())
                            .consultationFee(doctor.getConsultationFee())
                            .isAvailable(true)
                            .message("Next available slot found")
                            .build();
                }
            }
        }

        return NextAvailableDto.builder()
                .doctorId(doctor.getId())
                .doctorName(doctor.getName())
                .department(doctor.getDepartment())
                .isAvailable(false)
                .message("No available slots found within the next 30 days")
                .build();
    }

    // ==========================================
    // SLOT CHECK & RESERVATION LIFECYCLE (FR-12, BR-04, BR-07, BR-08)
    // ==========================================

    /**
     * Checks if a specific slot is available before appointment booking (VR-02 /
     * BR-04)
     */
    public SlotCheckResponseDto checkSpecificSlotAvailability(Long doctorId, LocalDate date, String slot) {
        Doctor doctor = getDoctorById(doctorId);

        if (date.isBefore(LocalDate.now())) {
            return SlotCheckResponseDto.builder()
                    .doctorId(doctorId)
                    .doctorName(doctor.getName())
                    .date(date)
                    .slot(slot)
                    .isAvailable(false)
                    .message("Selected date is in the past")
                    .build();
        }

        if (doctorLeaveRepository.existsByDoctorIdAndLeaveDate(doctorId, date)) {
            return SlotCheckResponseDto.builder()
                    .doctorId(doctorId)
                    .doctorName(doctor.getName())
                    .date(date)
                    .slot(slot)
                    .isAvailable(false)
                    .message("Doctor is on leave on this date")
                    .build();
        }

        cleanupExpiredHolds();
        String trimmedSlot = slot.trim();

        // Check if matching slot is booked/held/blocked
        List<BookedSlot> bookedList = bookedSlotRepository.findByDoctorIdAndBookingDateAndStatusNot(doctorId, date,
                "CANCELLED");
        for (BookedSlot bs : bookedList) {
            if (isSlotMatching(bs.getSlot(), trimmedSlot)) {
                String reason = "BLOCKED".equalsIgnoreCase(bs.getStatus())
                        ? "This slot has been blocked by the hospital: "
                                + (bs.getBlockReason() != null ? bs.getBlockReason() : "Unavailable")
                        : "This slot is already booked or held (No Double Booking)";
                return SlotCheckResponseDto.builder()
                        .doctorId(doctorId)
                        .doctorName(doctor.getName())
                        .date(date)
                        .slot(slot)
                        .isAvailable(false)
                        .message(reason)
                        .build();
            }
        }

        return SlotCheckResponseDto.builder()
                .doctorId(doctorId)
                .doctorName(doctor.getName())
                .date(date)
                .slot(slot)
                .isAvailable(true)
                .consultationFee(doctor.getConsultationFee())
                .message("Slot is available for booking")
                .build();
    }

    /**
     * Pre-books / holds a slot temporarily for 15 minutes during the
     * checkout/payment step (FR-12, BR-04)
     */
    public BookedSlot preBookSlot(SlotCheckRequestDto request, Integer holdMinutes) {
        SlotCheckResponseDto check = checkSpecificSlotAvailability(request.getDoctorId(), request.getDate(),
                request.getSlot());
        if (!check.isAvailable()) {
            throw new IllegalArgumentException(check.getMessage());
        }

        int duration = (holdMinutes != null && holdMinutes > 0) ? holdMinutes : 15;

        BookedSlot heldSlot = BookedSlot.builder()
                .doctorId(request.getDoctorId())
                .bookingDate(request.getDate())
                .slot(request.getSlot().trim())
                .patientId(request.getPatientId())
                .patientName(request.getPatientName())
                .patientEmail(request.getPatientEmail())
                .status("HELD")
                .holdExpiresAt(LocalDateTime.now().plusMinutes(duration))
                .build();

        return bookedSlotRepository.save(heldSlot);
    }

    /**
     * Direct confirmed reservation (BR-04 No Double Booking)
     */
    public BookedSlot reserveSlot(SlotCheckRequestDto request) {
        SlotCheckResponseDto check = checkSpecificSlotAvailability(request.getDoctorId(), request.getDate(),
                request.getSlot());
        if (!check.isAvailable()) {
            throw new IllegalArgumentException(check.getMessage());
        }

        BookedSlot bookedSlot = BookedSlot.builder()
                .doctorId(request.getDoctorId())
                .bookingDate(request.getDate())
                .slot(request.getSlot().trim())
                .patientId(request.getPatientId())
                .patientName(request.getPatientName())
                .patientEmail(request.getPatientEmail())
                .status("CONFIRMED")
                .build();

        return bookedSlotRepository.save(bookedSlot);
    }

    /**
     * Confirms a previously held/pre-booked slot upon successful payment (BR-07)
     */
    public BookedSlot confirmSlot(Long bookedSlotId) {
        BookedSlot slot = bookedSlotRepository.findById(bookedSlotId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Slot reservation not found with id: " + bookedSlotId));

        slot.setStatus("CONFIRMED");
        slot.setHoldExpiresAt(null);
        return bookedSlotRepository.save(slot);
    }

    /**
     * Releases or cancels a slot reservation
     */
    public void releaseSlot(Long bookedSlotId) {
        BookedSlot slot = bookedSlotRepository.findById(bookedSlotId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Slot reservation not found with id: " + bookedSlotId));
        slot.setStatus("CANCELLED");
        bookedSlotRepository.save(slot);
    }

    public void releaseSlotByDoctorAndDate(Long doctorId, LocalDate date, String slot) {
        List<BookedSlot> list = bookedSlotRepository.findByDoctorIdAndBookingDateAndStatusNot(doctorId, date,
                "CANCELLED");
        for (BookedSlot bs : list) {
            if (isSlotMatching(bs.getSlot(), slot.trim())) {
                bs.setStatus("CANCELLED");
                bookedSlotRepository.save(bs);
                return;
            }
        }
        throw new ResourceNotFoundException("No active booking found for slot " + slot + " on " + date);
    }

    // ==========================================
    // MANUAL SLOT BLOCKING / EXCEPTIONS (FR-09, BR-11)
    // ==========================================

    public BookedSlot blockSlot(Long doctorId, LocalDate date, String slot, String reason) {
        getDoctorById(doctorId);
        BookedSlot blocked = BookedSlot.builder()
                .doctorId(doctorId)
                .bookingDate(date)
                .slot(slot.trim())
                .status("BLOCKED")
                .blockReason(reason != null ? reason : "Doctor Unavailable")
                .build();

        return bookedSlotRepository.save(blocked);
    }

    public void unblockSlot(Long doctorId, LocalDate date, String slot) {
        List<BookedSlot> list = bookedSlotRepository.findByDoctorIdAndBookingDateAndStatus(doctorId, date, "BLOCKED");
        for (BookedSlot bs : list) {
            if (isSlotMatching(bs.getSlot(), slot.trim())) {
                bookedSlotRepository.delete(bs);
                return;
            }
        }
        throw new ResourceNotFoundException("No blocked slot found for " + slot + " on " + date);
    }

    public List<BookedSlot> getBlockedSlots(Long doctorId, LocalDate date) {
        getDoctorById(doctorId);
        return bookedSlotRepository.findByDoctorIdAndBookingDateAndStatus(doctorId, date, "BLOCKED");
    }

    // ==========================================
    // HELPER & CLEANUP METHODS
    // ==========================================

    private void cleanupExpiredHolds() {
        // Any HELD slot with holdExpiresAt < now is reset to CANCELLED
        try {
            List<BookedSlot> all = bookedSlotRepository.findAll();
            for (BookedSlot bs : all) {
                if ("HELD".equalsIgnoreCase(bs.getStatus()) && bs.getHoldExpiresAt() != null
                        && bs.getHoldExpiresAt().isBefore(LocalDateTime.now())) {
                    bs.setStatus("CANCELLED");
                    bookedSlotRepository.save(bs);
                }
            }
        } catch (Exception e) {
            log.warn("Cleanup expired holds warning: {}", e.getMessage());
        }
    }

    private boolean isSlotMatching(String bookedSlot, String targetSlot) {
        if (bookedSlot.equalsIgnoreCase(targetSlot))
            return true;
        // Also match display time e.g. "09:00 AM" against "09:00 - 09:45"
        String bClean = bookedSlot.replace(" ", "").toLowerCase();
        String tClean = targetSlot.replace(" ", "").toLowerCase();
        return bClean.contains(tClean) || tClean.contains(bClean);
    }

    private DoctorAvailabilityDto buildAvailability(Doctor doc, LocalDate date, String day, boolean available,
            List<TimeSlotDto> slots, String message) {
        return DoctorAvailabilityDto.builder()
                .doctorId(doc.getId())
                .doctorName(doc.getName())
                .department(doc.getDepartment())
                .specialization(doc.getSpecialization())
                .date(date)
                .dayOfWeek(day)
                .consultationFee(doc.getConsultationFee())
                .isAvailable(available)
                .availableSlots(slots)
                .message(message)
                .build();
    }

    private List<TimeSlotDto> generateSlotsBetween(LocalTime start, LocalTime end, int durationMinutes, LocalDate date,
            Map<String, String> slotStatusMap) {
        List<TimeSlotDto> slotList = new ArrayList<>();
        LocalTime current = start;
        LocalTime now = LocalTime.now();
        boolean isToday = date.equals(LocalDate.now());

        while (true) {
            LocalTime next = current.plusMinutes(durationMinutes);
            if (next.isAfter(end))
                break;

            String startStr = current.format(TIME_FORMATTER);
            String endStr = next.format(TIME_FORMATTER);
            String slotLabel = startStr + " - " + endStr;
            String displayTime = current.format(DISPLAY_TIME_FORMATTER);

            boolean isSlotPast = isToday && current.isBefore(now);

            // Determine if slot is booked, held, or blocked
            String status = "AVAILABLE";
            boolean available = !isSlotPast;

            if (isSlotPast) {
                status = "UNAVAILABLE";
            } else if (slotStatusMap != null) {
                for (Map.Entry<String, String> entry : slotStatusMap.entrySet()) {
                    if (isSlotMatching(entry.getKey(), slotLabel) || isSlotMatching(entry.getKey(), displayTime)
                            || isSlotMatching(entry.getKey(), startStr)) {
                        status = entry.getValue();
                        available = false;
                        break;
                    }
                }
            }

            slotList.add(TimeSlotDto.builder()
                    .slot(slotLabel)
                    .displayTime(displayTime)
                    .startTime(startStr)
                    .endTime(endStr)
                    .isAvailable(available)
                    .status(status)
                    .build());

            current = next;
        }

        return slotList;
    }
}
