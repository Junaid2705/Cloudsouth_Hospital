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
        if (departmentRepository.count() == 0) {
            log.info("Seeding hospital departments from Business Requirement Document...");
            seedDepartments();
        }

        if (doctorRepository.count() == 0) {
            log.info("Seeding initial doctors across hospital departments...");
            seedDoctors();
        }
    }

    private void seedDepartments() {
        List<Department> departments = List.of(
                Department.builder()
                        .name("General Physicians")
                        .code("GEN")
                        .description("Comprehensive primary healthcare, routine checkups, chronic disease management, and emergency primary care.")
                        .icon("stethoscope")
                        .headDoctorName("Dr. Sarah Jenkins")
                        .totalDoctors(35)
                        .isActive(true)
                        .build(),

                Department.builder()
                        .name("Orthopedic Care & Leg Fracture")
                        .code("ORTHO")
                        .description("Specialized diagnosis, surgical treatments, and rehabilitation for bone fractures, joints, and spine disorders.")
                        .icon("bone")
                        .headDoctorName("Dr. Michael Roberts")
                        .totalDoctors(10)
                        .isActive(true)
                        .build(),

                Department.builder()
                        .name("Brain Surgery & Neurosurgery")
                        .code("NEURO")
                        .description("Cutting-edge neurosurgical treatments for brain tumors, spinal trauma, neuro-vascular disorders, and nerve repair.")
                        .icon("brain")
                        .headDoctorName("Dr. Alan Vance")
                        .totalDoctors(10)
                        .isActive(true)
                        .build(),

                Department.builder()
                        .name("Cancer & Oncology Care")
                        .code("ONCO")
                        .description("Advanced medical, surgical, and radiation oncology services offering targeted therapies and cancer care programs.")
                        .icon("ribbon")
                        .headDoctorName("Dr. Elena Rostova")
                        .totalDoctors(20)
                        .isActive(true)
                        .build(),

                Department.builder()
                        .name("Stomach & Gastroenterology")
                        .code("GASTRO")
                        .description("Comprehensive digestive healthcare, endoscopy, liver diseases, acid reflux, and gastrointestinal interventions.")
                        .icon("activity")
                        .headDoctorName("Dr. David Chen")
                        .totalDoctors(10)
                        .isActive(true)
                        .build()
        );

        departmentRepository.saveAll(departments);
    }

    private void seedDoctors() {
        Department genDept = departmentRepository.findByCodeIgnoreCase("GEN").orElse(null);
        Department orthoDept = departmentRepository.findByCodeIgnoreCase("ORTHO").orElse(null);
        Department neuroDept = departmentRepository.findByCodeIgnoreCase("NEURO").orElse(null);
        Department oncoDept = departmentRepository.findByCodeIgnoreCase("ONCO").orElse(null);
        Department gastroDept = departmentRepository.findByCodeIgnoreCase("GASTRO").orElse(null);

        List<Doctor> doctors = List.of(
                Doctor.builder()
                        .name("Dr. Sarah Jenkins")
                        .specialization("Internal Medicine & Primary Care")
                        .department("General Physicians")
                        .departmentId(genDept != null ? genDept.getId() : null)
                        .qualification("MD, FACP")
                        .experienceYears(14)
                        .consultationFee(50.00)
                        .availableDays("Monday, Tuesday, Wednesday, Thursday, Friday")
                        .availableTimeSlots("09:00 - 13:00, 14:00 - 17:00")
                        .email("s.jenkins@cloudsouth.org")
                        .phone("+1 555-0101")
                        .isActive(true)
                        .build(),

                Doctor.builder()
                        .name("Dr. Michael Roberts")
                        .specialization("Trauma & Joint Replacement")
                        .department("Orthopedic Care & Leg Fracture")
                        .departmentId(orthoDept != null ? orthoDept.getId() : null)
                        .qualification("MS (Ortho), FRCS")
                        .experienceYears(16)
                        .consultationFee(50.00)
                        .availableDays("Monday, Wednesday, Friday, Saturday")
                        .availableTimeSlots("10:00 - 13:00, 14:00 - 18:00")
                        .email("m.roberts@cloudsouth.org")
                        .phone("+1 555-0102")
                        .isActive(true)
                        .build(),

                Doctor.builder()
                        .name("Dr. Alan Vance")
                        .specialization("Complex Brain & Spine Surgery")
                        .department("Brain Surgery & Neurosurgery")
                        .departmentId(neuroDept != null ? neuroDept.getId() : null)
                        .qualification("MD, MCh (Neurosurgery), FAANS")
                        .experienceYears(18)
                        .consultationFee(50.00)
                        .availableDays("Tuesday, Thursday, Saturday")
                        .availableTimeSlots("09:00 - 12:00, 13:00 - 16:00")
                        .email("a.vance@cloudsouth.org")
                        .phone("+1 555-0103")
                        .isActive(true)
                        .build(),

                Doctor.builder()
                        .name("Dr. Elena Rostova")
                        .specialization("Medical Oncology & Chemotherapy")
                        .department("Cancer & Oncology Care")
                        .departmentId(oncoDept != null ? oncoDept.getId() : null)
                        .qualification("MD, PhD, DM (Oncology)")
                        .experienceYears(12)
                        .consultationFee(50.00)
                        .availableDays("Monday, Tuesday, Wednesday, Thursday")
                        .availableTimeSlots("09:00 - 13:00, 14:00 - 17:00")
                        .email("e.rostova@cloudsouth.org")
                        .phone("+1 555-0104")
                        .isActive(true)
                        .build(),

                Doctor.builder()
                        .name("Dr. David Chen")
                        .specialization("Advanced Hepatology & Endoscopy")
                        .department("Stomach & Gastroenterology")
                        .departmentId(gastroDept != null ? gastroDept.getId() : null)
                        .qualification("MD, FACG, AGAF")
                        .experienceYears(15)
                        .consultationFee(50.00)
                        .availableDays("Monday, Wednesday, Thursday, Friday")
                        .availableTimeSlots("10:00 - 14:00, 15:00 - 18:00")
                        .email("d.chen@cloudsouth.org")
                        .phone("+1 555-0105")
                        .isActive(true)
                        .build()
        );

        List<Doctor> savedDoctors = doctorRepository.saveAll(doctors);

        // Seed schedules for each doctor
        for (Doctor doc : savedDoctors) {
            seedSchedulesForDoctor(doc.getId());
        }
    }

    private void seedSchedulesForDoctor(Long doctorId) {
        List<String> weekdays = List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY");
        for (String day : weekdays) {
            doctorScheduleRepository.save(DoctorSchedule.builder()
                    .doctorId(doctorId)
                    .dayOfWeek(day)
                    .startTime("09:00")
                    .endTime("13:00")
                    .slotDurationMinutes(60)
                    .isActive(true)
                    .build());

            doctorScheduleRepository.save(DoctorSchedule.builder()
                    .doctorId(doctorId)
                    .dayOfWeek(day)
                    .startTime("14:00")
                    .endTime("17:00")
                    .slotDurationMinutes(60)
                    .isActive(true)
                    .build());
        }
    }
}
