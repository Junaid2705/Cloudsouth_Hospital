# CloudSouth Hospitality - Backend API & Services

Comprehensive enterprise backend for **CloudSouth Hospital Management System** built with **Java 21**, **Spring Boot 3.x**, and **Spring Data JPA**.

Implemented strictly based on **Business Requirement Document (BRD v1.0)**, **Functional Requirement Document (FRD v1.0)**, and the **Doctor & Specialist Directory UI Designs**.

---

## 🚀 Key Highlights & Business Capabilities

- **Doctor Directory & Filtering (`FR-04`, `BR-02`)**: Single-pass multi-criteria filtering by department, available day, shift (Morning / Evening / Available Today), keyword query, minimum experience years, and pagination (`NFR-06`).
- **Standard & Returning Patient Fee Calculation (`BR-05`, `BR-06`, `FR-11`, `FR-16`, `VR-05`)**: Standard fee $50.00, automatic 10% discount ($5.00 savings) for returning patients ($45.00 final fee).
- **Hospital Departments (`FR-10`, `FR-11`)**: 5 core departments (*General Physician*, *Leg Fracture / Orthopedic Care*, *Brain Surgery / Neurosurgery*, *Cancer / Oncology*, *Stomach / Gastroenterology*) with dynamic doctor capacity and real-time availability.
- **Real-Time Doctor Availability Engine (`FR-07`, `FR-13`, `BR-03`, `VR-02`)**: Dynamic calculation of 45-minute slots taking into account recurring weekly schedules, doctor leaves, past-hour blocking, and booked slots.
- **Strict Double Booking Prevention (`BR-04`, `VR-02`)**: Guaranteed no overlapping appointments for the same doctor and slot.
- **Slot Hold & Pre-Booking Lifecycle (`FR-12`, `BR-07`, `BR-08`)**: Temporary 15-minute slot lock during payment with confirmation and release endpoints.
- **Schedule Overrides & Slot Blocking (`FR-09`, `BR-11`)**: Manual deactivation/blocking of specific time slots on specific dates.
- **Multi-day Availability Calendar (`FR-07`, `FR-12`)**: Day-by-day availability status, slot counts, and leave indicators for calendar date-pickers.

---

## 🛠️ Tech Stack & Setup

- **Language**: Java 21 (JDK 21)
- **Framework**: Spring Boot 3.x
- **Persistence**: Spring Data JPA / Hibernate
- **Database**: H2 In-Memory Database (Embedded, zero setup)
- **H2 Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (`jdbc:h2:mem:cloudsouthdb`, username: `sa`, password: `password`)
- **API Base URL**: `http://localhost:8080/api/v1`

---

## 📡 API Reference

All responses follow the unified `ApiResponse<T>` envelope:
```json
{
  "success": true,
  "message": "Operation description",
  "data": { ... },
  "timestamp": "2026-10-26T12:00:00"
}
```

---

### 👨‍⚕️ 1. Doctors API (`/api/v1/doctors`)

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/doctors` | Multi-criteria search (`department`, `day`, `shift`, `query`, `minExperience`, `maxFee`, `activeOnly`) |
| `GET` | `/api/v1/doctors/page` | Paginated and sorted doctor search (`page`, `size`, `sortBy`, `sortDirection`) |
| `GET` | `/api/v1/doctors/stats` | Header stats (`85+ Specialists`, `1,000+ Beds`, department counts) |
| `GET` | `/api/v1/doctors/specializations` | All distinct specializations across active doctors |
| `GET` | `/api/v1/doctors/departments` | All distinct doctor departments |
| `GET` | `/api/v1/doctors/experience?minYears=15` | Doctors with 15+ years experience |
| `GET` | `/api/v1/doctors/available?date=YYYY-MM-DD` | Doctors available on a specific date |
| `GET` | `/api/v1/doctors/{id}` | Get doctor details by ID |
| `GET` | `/api/v1/doctors/{id}/fee?isReturning=true` | Fee & discount calculation ($50 vs $45) |
| `POST` | `/api/v1/doctors` | Add a new doctor (`name`, `department`, `specialization` mandatory) |
| `PUT` | `/api/v1/doctors/{id}` | Update doctor profile |
| `PATCH`| `/api/v1/doctors/{id}/status?active=true` | Activate or deactivate a doctor |
| `DELETE`| `/api/v1/doctors/{id}` | Soft delete doctor (set inactive) |

---

### 🏥 2. Departments API (`/api/v1/departments`)

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/departments` | List all departments (`activeOnly=true/false`) |
| `GET` | `/api/v1/departments/summary` | Department summaries with active doctor counts and target capacity |
| `GET` | `/api/v1/departments/stats` | Hospital overview: 5 departments, 85 capacity, active counts |
| `GET` | `/api/v1/departments/code/{code}` | Lookup department by code (e.g. `ORTHO`, `GEN`, `NEURO`) |
| `GET` | `/api/v1/departments/{id}` | Get department by ID |
| `GET` | `/api/v1/departments/{id}/doctors` | All doctors belonging to a department |
| `GET` | `/api/v1/departments/{id}/doctors/search?query=...` | Search doctors inside a department |
| `GET` | `/api/v1/departments/{id}/specializations` | Unique specializations in this department |
| `GET` | `/api/v1/departments/{id}/availability?date=...` | Department-wide doctor availability and open slots on date |
| `POST` | `/api/v1/departments` | Create new department |
| `PUT` | `/api/v1/departments/{id}` | Update department details |
| `PATCH`| `/api/v1/departments/{id}/status?active=true` | Toggle department active status |
| `DELETE`| `/api/v1/departments/{id}` | Deactivate department |

---

### ⏱️ 3. Availability & Scheduling API (`/api/v1/availability` & `/api/v1/doctors/{id}/...`)

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/doctors/{id}/availability?date=YYYY-MM-DD` | Real-time slots for doctor on date (45-min intervals) |
| `GET` | `/api/v1/doctors/{id}/calendar?startDate=...&endDate=...` | 30-day availability calendar for date picker UI |
| `GET` | `/api/v1/doctors/{id}/next-available` | Earliest open slot in the next 30 days |
| `POST` | `/api/v1/doctors/{id}/check-slot` | Check if specific slot is available (`BR-04` double-booking check) |
| `POST` | `/api/v1/doctors/{id}/pre-book-slot?holdMinutes=15` | Temporarily hold slot during pre-booking/payment flow (`FR-12`) |
| `POST` | `/api/v1/doctors/{id}/book-slot` | Directly reserve/book a confirmed slot |
| `POST` | `/api/v1/doctors/slots/{slotId}/confirm` | Confirm a held slot upon successful payment |
| `POST` | `/api/v1/doctors/slots/{slotId}/release` | Release/cancel a held or booked slot |
| `POST` | `/api/v1/doctors/{id}/block-slot` | Manually block doctor slot for meeting/surgery (`FR-09`) |
| `POST` | `/api/v1/doctors/{id}/unblock-slot` | Unblock a previously blocked slot |
| `GET` | `/api/v1/doctors/{id}/blocked-slots?date=...` | List blocked slots on a date |
| `GET` | `/api/v1/availability/doctors?date=...` | Unified endpoint: find all available doctors on date |
| `GET` | `/api/v1/availability/departments/{id}?date=...` | Unified endpoint: check department availability |

---

### 📅 4. Doctor Weekly Schedules & Leaves (`FR-09`)

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/doctors/{id}/schedules` | Weekly recurring schedule rules |
| `POST` | `/api/v1/doctors/{id}/schedules` | Add a schedule rule (`dayOfWeek`, `startTime`, `endTime`) |
| `POST` | `/api/v1/doctors/{id}/schedules/bulk` | Bulk save entire weekly schedule (Mon-Sun) |
| `PUT` | `/api/v1/doctors/{id}/schedules/{scheduleId}` | Update schedule rule |
| `DELETE`| `/api/v1/doctors/{id}/schedules/{scheduleId}` | Remove schedule rule |
| `GET` | `/api/v1/doctors/{id}/leaves` | All leaves for a doctor |
| `POST` | `/api/v1/doctors/{id}/leaves` | Schedule single-date leave |
| `POST` | `/api/v1/doctors/{id}/leaves/range` | Schedule date range leave (`startDate` to `endDate`) |
| `DELETE`| `/api/v1/doctors/{id}/leaves/{leaveId}` | Cancel doctor leave |
| `GET` | `/api/v1/doctors/leaves?date=...` | Hospital-wide view of all doctors on leave |

---

## 🧪 Postman Collection

An updated Postman test collection with sample payloads for all endpoints is available in the root folder:
- **File**: [CloudSouth_Postman_Collection.json](file:///d:/Cloudsouth_Hospital/Cloudsouth_Hospital/CloudSouth_Postman_Collection.json)
- Import it into Postman to instantly test all endpoints.
