package com.cloudsouth.backend.config;

import com.cloudsouth.backend.model.Department;
import com.cloudsouth.backend.model.Doctor;
import com.cloudsouth.backend.model.DoctorSchedule;
import com.cloudsouth.backend.repository.DepartmentRepository;
import com.cloudsouth.backend.repository.DoctorRepository;
import com.cloudsouth.backend.repository.DoctorScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;

    @Override
    public void run(String... args) {
        log.info("Checking and seeding hospital departments and specialized doctors...");
        seedDepartments();
        seedDoctors();
    }

    private void seedDepartments() {
        if (departmentRepository.count() == 0) {
            List<Department> departments = List.of(
                    Department.builder()
                            .name("General Physician")
                            .code("GEN")
                            .description("Comprehensive primary healthcare, routine checkups, chronic disease management, and emergency primary care.")
                            .icon("stethoscope")
                            .headDoctorName("Dr. Rajesh Sharma")
                            .totalDoctors(35)
                            .isActive(true)
                            .build(),

                    Department.builder()
                            .name("Leg Fracture / Orthopedic Care")
                            .code("ORTHO")
                            .description("Specialized diagnosis, surgical treatments, and rehabilitation for bone fractures, joints, and spine disorders.")
                            .icon("bone")
                            .headDoctorName("Dr. Vikram Malhotra")
                            .totalDoctors(10)
                            .isActive(true)
                            .build(),

                    Department.builder()
                            .name("Brain Surgery / Neurosurgery")
                            .code("NEURO")
                            .description("Cutting-edge neurosurgical treatments for brain tumors, spinal trauma, neuro-vascular disorders, and nerve repair.")
                            .icon("brain")
                            .headDoctorName("Dr. Arvind Swaminathan")
                            .totalDoctors(10)
                            .isActive(true)
                            .build(),

                    Department.builder()
                            .name("Cancer / Oncology")
                            .code("ONCO")
                            .description("Advanced medical, surgical, and radiation oncology services offering targeted therapies and cancer care programs.")
                            .icon("ribbon")
                            .headDoctorName("Dr. Elena Rostova")
                            .totalDoctors(20)
                            .isActive(true)
                            .build(),

                    Department.builder()
                            .name("Stomach / Gastroenterology")
                            .code("GASTRO")
                            .description("Comprehensive digestive healthcare, endoscopy, liver diseases, acid reflux, and gastrointestinal interventions.")
                            .icon("activity")
                            .headDoctorName("Dr. David Chen")
                            .totalDoctors(10)
                            .isActive(true)
                            .build()
            );

            departmentRepository.saveAll(departments);
            log.info("Successfully seeded 5 core hospital departments.");
        }
    }

    private void seedDoctors() {
        if (doctorRepository.count() == 0) {
            Department genDept = departmentRepository.findByCodeIgnoreCase("GEN").orElse(null);
            Department orthoDept = departmentRepository.findByCodeIgnoreCase("ORTHO").orElse(null);
            Department neuroDept = departmentRepository.findByCodeIgnoreCase("NEURO").orElse(null);
            Department oncoDept = departmentRepository.findByCodeIgnoreCase("ONCO").orElse(null);
            Department gastroDept = departmentRepository.findByCodeIgnoreCase("GASTRO").orElse(null);

            List<Doctor> doctors = List.of(
                    // 1. Dr. Rajesh Sharma
                    Doctor.builder()
                            .name("Dr. Rajesh Sharma")
                            .specialization("Internal Medicine & Preventive Adult Health")
                            .department("General Physician")
                            .departmentId(genDept != null ? genDept.getId() : null)
                            .qualification("MBBS, MD (Internal Medicine), FACP")
                            .experienceYears(18)
                            .consultationFee(50.00)
                            .returningFee(45.00)
                            .availableDays("Monday, Tuesday, Wednesday, Thursday, Friday, Saturday")
                            .timings("09:00 AM - 01:00 PM (Morning Shift)")
                            .timingShift("Morning Shift")
                            .availableTimeSlots("09:00 AM, 09:45 AM, 10:30 AM, 11:15 AM, 12:00 PM")
                            .photoUrl("https://images.unsplash.com/photo-1622253692010-333f2da6031d?w=400")
                            .email("r.sharma@cloudsouth.org")
                            .phone("+1 555-0101")
                            .isActive(true)
                            .build(),

                    // 2. Dr. Sunita Deshmukh
                    Doctor.builder()
                            .name("Dr. Sunita Deshmukh")
                            .specialization("Consultant Physician & Metabolic Specialist")
                            .department("General Physician")
                            .departmentId(genDept != null ? genDept.getId() : null)
                            .qualification("MBBS, DNB (Family Medicine)")
                            .experienceYears(12)
                            .consultationFee(50.00)
                            .returningFee(45.00)
                            .availableDays("Monday, Wednesday, Friday, Saturday")
                            .timings("02:00 PM - 06:00 PM (Evening Shift)")
                            .timingShift("Evening Shift")
                            .availableTimeSlots("02:00 PM, 02:45 PM, 03:30 PM, 04:15 PM, 05:00 PM")
                            .photoUrl("https://images.unsplash.com/photo-1594824813639-444a7f050b12?w=400")
                            .email("s.deshmukh@cloudsouth.org")
                            .phone("+1 555-0102")
                            .isActive(true)
                            .build(),

                    // 3. Dr. Amitav Kulkarni
                    Doctor.builder()
                            .name("Dr. Amitav Kulkarni")
                            .specialization("Acute Care & Emergency General Medicine")
                            .department("General Physician")
                            .departmentId(genDept != null ? genDept.getId() : null)
                            .qualification("MBBS, MD (General Medicine)")
                            .experienceYears(15)
                            .consultationFee(50.00)
                            .returningFee(45.00)
                            .availableDays("Tuesday, Thursday, Saturday, Sunday")
                            .timings("10:00 AM - 02:00 PM (Morning Shift)")
                            .timingShift("Morning Shift")
                            .availableTimeSlots("10:00 AM, 10:45 AM, 11:30 AM, 12:15 PM, 01:00 PM")
                            .photoUrl("https://images.unsplash.com/photo-1537368910025-700350fe46c7?w=400")
                            .email("a.kulkarni@cloudsouth.org")
                            .phone("+1 555-0103")
                            .isActive(true)
                            .build(),

                    // 4. Dr. Vikram Malhotra
                    Doctor.builder()
                            .name("Dr. Vikram Malhotra")
                            .specialization("Chief Orthopedic Surgeon - Complex Trauma & Limb Reconstruction")
                            .department("Leg Fracture / Orthopedic Care")
                            .departmentId(orthoDept != null ? orthoDept.getId() : null)
                            .qualification("MS (Ortho), DNB (Orthopedics), MCh (Ortho)")
                            .experienceYears(22)
                            .consultationFee(50.00)
                            .returningFee(45.00)
                            .availableDays("Monday, Wednesday, Friday")
                            .timings("09:00 AM - 01:00 PM (Morning Shift)")
                            .timingShift("Morning Shift")
                            .availableTimeSlots("09:00 AM, 09:45 AM, 10:30 AM, 11:15 AM, 12:00 PM")
                            .photoUrl("https://images.unsplash.com/photo-1612349317150-e413f6a5b16d?w=400")
                            .email("v.malhotra@cloudsouth.org")
                            .phone("+1 555-0104")
                            .isActive(true)
                            .build(),

                    // 5. Dr. Ananya Sen
                    Doctor.builder()
                            .name("Dr. Ananya Sen")
                            .specialization("Consultant Orthopedic Surgeon & Sports Knee/Ankle Reconstruction")
                            .department("Leg Fracture / Orthopedic Care")
                            .departmentId(orthoDept != null ? orthoDept.getId() : null)
                            .qualification("MS (Ortho), Fellowship in Arthroscopy & Sports Medicine")
                            .experienceYears(14)
                            .consultationFee(50.00)
                            .returningFee(45.00)
                            .availableDays("Tuesday, Thursday, Saturday")
                            .timings("02:00 PM - 06:00 PM (Evening Shift)")
                            .timingShift("Evening Shift")
                            .availableTimeSlots("02:00 PM, 02:45 PM, 03:30 PM, 04:15 PM, 05:00 PM")
                            .photoUrl("https://images.unsplash.com/photo-1559839734-2b71ea197ec2?w=400")
                            .email("a.sen@cloudsouth.org")
                            .phone("+1 555-0105")
                            .isActive(true)
                            .build(),

                    // 6. Dr. Arvind Swaminathan
                    Doctor.builder()
                            .name("Dr. Arvind Swaminathan")
                            .specialization("Director - Brain Surgery & Micro-Neurosurgery")
                            .department("Brain Surgery / Neurosurgery")
                            .departmentId(neuroDept != null ? neuroDept.getId() : null)
                            .qualification("MCh (Neurosurgery), FINR, FACS")
                            .experienceYears(24)
                            .consultationFee(50.00)
                            .returningFee(45.00)
                            .availableDays("Monday, Tuesday, Thursday")
                            .timings("10:00 AM - 03:00 PM (Morning Shift)")
                            .timingShift("Morning Shift")
                            .availableTimeSlots("10:00 AM, 11:00 AM, 12:00 PM, 01:00 PM, 02:00 PM")
                            .photoUrl("https://images.unsplash.com/photo-1582750433449-648ed127bb54?w=400")
                            .email("a.swaminathan@cloudsouth.org")
                            .phone("+1 555-0106")
                            .isActive(true)
                            .build(),

                    // 7. Dr. Priya Nair
                    Doctor.builder()
                            .name("Dr. Priya Nair")
                            .specialization("Consultant Neurosurgeon - Spine & Peripheral Nerve Surgery")
                            .department("Brain Surgery / Neurosurgery")
                            .departmentId(neuroDept != null ? neuroDept.getId() : null)
                            .qualification("MS (General Surgery), MCh (Neurosurgery)")
                            .experienceYears(11)
                            .consultationFee(50.00)
                            .returningFee(45.00)
                            .availableDays("Wednesday, Friday, Saturday")
                            .timings("01:00 PM - 05:00 PM (Evening Shift)")
                            .timingShift("Evening Shift")
                            .availableTimeSlots("01:00 PM, 01:45 PM, 02:30 PM, 03:15 PM, 04:00 PM")
                            .photoUrl("https://images.unsplash.com/photo-1651008376811-b90baee60c1f?w=400")
                            .email("p.nair@cloudsouth.org")
                            .phone("+1 555-0107")
                            .isActive(true)
                            .build(),

                    // 8. Dr. Elena Rostova
                    Doctor.builder()
                            .name("Dr. Elena Rostova")
                            .specialization("Senior Medical Oncologist & Chemotherapy Specialist")
                            .department("Cancer / Oncology")
                            .departmentId(oncoDept != null ? oncoDept.getId() : null)
                            .qualification("MD, PhD, DM (Medical Oncology)")
                            .experienceYears(16)
                            .consultationFee(50.00)
                            .returningFee(45.00)
                            .availableDays("Monday, Wednesday, Friday")
                            .timings("09:30 AM - 01:30 PM (Morning Shift)")
                            .timingShift("Morning Shift")
                            .availableTimeSlots("09:30 AM, 10:15 AM, 11:00 AM, 11:45 AM, 12:30 PM")
                            .photoUrl("https://images.unsplash.com/photo-1527613426441-4da17471b66d?w=400")
                            .email("e.rostova@cloudsouth.org")
                            .phone("+1 555-0108")
                            .isActive(true)
                            .build(),

                    // 9. Dr. Rajeshwari Sengupta
                    Doctor.builder()
                            .name("Dr. Rajeshwari Sengupta")
                            .specialization("Surgical Oncologist - Breast & Thoracic Oncology")
                            .department("Cancer / Oncology")
                            .departmentId(oncoDept != null ? oncoDept.getId() : null)
                            .qualification("MS, MCh (Surgical Oncology), FICS")
                            .experienceYears(19)
                            .consultationFee(50.00)
                            .returningFee(45.00)
                            .availableDays("Tuesday, Thursday, Saturday")
                            .timings("02:00 PM - 06:00 PM (Evening Shift)")
                            .timingShift("Evening Shift")
                            .availableTimeSlots("02:00 PM, 02:45 PM, 03:30 PM, 04:15 PM, 05:00 PM")
                            .photoUrl("https://images.unsplash.com/photo-1579684385127-1ef15d508118?w=400")
                            .email("r.sengupta@cloudsouth.org")
                            .phone("+1 555-0109")
                            .isActive(true)
                            .build(),

                    // 10. Dr. David Chen
                    Doctor.builder()
                            .name("Dr. David Chen")
                            .specialization("Chief Gastroenterologist & Interventional Endoscopist")
                            .department("Stomach / Gastroenterology")
                            .departmentId(gastroDept != null ? gastroDept.getId() : null)
                            .qualification("MD, FACG, AGAF")
                            .experienceYears(17)
                            .consultationFee(50.00)
                            .returningFee(45.00)
                            .availableDays("Monday, Tuesday, Thursday, Friday")
                            .timings("09:00 AM - 01:00 PM (Morning Shift)")
                            .timingShift("Morning Shift")
                            .availableTimeSlots("09:00 AM, 09:45 AM, 10:30 AM, 11:15 AM, 12:00 PM")
                            .photoUrl("https://images.unsplash.com/photo-1622902046580-2b47f47f5471?w=400")
                            .email("d.chen@cloudsouth.org")
                            .phone("+1 555-0110")
                            .isActive(true)
                            .build(),

                    // 11. Dr. Manoj Mathur
                    Doctor.builder()
                            .name("Dr. Manoj Mathur")
                            .specialization("Hepatologist & Digestive Disease Specialist")
                            .department("Stomach / Gastroenterology")
                            .departmentId(gastroDept != null ? gastroDept.getId() : null)
                            .qualification("MBBS, MD, DM (Gastroenterology)")
                            .experienceYears(13)
                            .consultationFee(50.00)
                            .returningFee(45.00)
                            .availableDays("Wednesday, Friday, Saturday")
                            .timings("02:00 PM - 06:00 PM (Evening Shift)")
                            .timingShift("Evening Shift")
                            .availableTimeSlots("02:00 PM, 02:45 PM, 03:30 PM, 04:15 PM, 05:00 PM")
                            .photoUrl("https://images.unsplash.com/photo-1551601651-2a8555f1a136?w=400")
                            .email("m.mathur@cloudsouth.org")
                            .phone("+1 555-0111")
                            .isActive(true)
                            .build()
            );

            List<Doctor> savedDoctors = doctorRepository.saveAll(doctors);
            log.info("Successfully seeded all 11 doctors matching the frontend screenshot.");

            // Seed recurring schedules for each doctor
            for (Doctor doc : savedDoctors) {
                seedSchedulesForDoctor(doc);
            }
        }
    }

    private void seedSchedulesForDoctor(Doctor doctor) {
        String days = doctor.getAvailableDays();
        if (days == null) return;

        List<String> allDays = List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY");
        for (String day : allDays) {
            String shortDay = day.substring(0, 3).toLowerCase();
            if (days.toLowerCase().contains(shortDay)) {
                String start = doctor.getTimingShift() != null && doctor.getTimingShift().equalsIgnoreCase("Evening Shift") ? "14:00" : "09:00";
                String end = doctor.getTimingShift() != null && doctor.getTimingShift().equalsIgnoreCase("Evening Shift") ? "18:00" : "13:00";

                doctorScheduleRepository.save(DoctorSchedule.builder()
                        .doctorId(doctor.getId())
                        .dayOfWeek(day)
                        .startTime(start)
                        .endTime(end)
                        .slotDurationMinutes(45)
                        .isActive(true)
                        .build());
            }
        }
    }
}
