# CloudSouth Hospital Management — QA Functional & Business Rule Test Cases

- **Project**: CloudSouth Hospital Management System
- **Document Version**: 1.0 (QA Test Suite)
- **Traceability**: Aligned with BRD v1.0 and FRD v1.0
- **Base URL**: `http://localhost:8080/api/v1`

---

## 1. System Health Check Module

| Test Case ID | Test Scenario | Req Ref | Type | Pre-conditions | Test Steps | Test Data | Expected Result | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_SYS_001` | Verify system health check endpoint is operational | NFR-03 | Positive | Server is running on port 8080 | 1. Send `GET /api/v1/health`<br/>2. Inspect HTTP status code and response payload | None | HTTP `200 OK`<br/>`success: true`<br/>`data: "System is up and running!"` | **PASS** |

---

## 2. Department Management Module (BR-01 / FR-10 / FR-11)

| Test Case ID | Test Scenario | Req Ref | Type | Pre-conditions | Test Steps | Test Data | Expected Result | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_DEP_001` | Retrieve all 5 core hospital departments | BR-01<br/>FR-10 | Positive | System data seeded with 5 departments | 1. Send `GET /api/v1/departments`<br/>2. Verify array contains all 5 departments: GEN, ORTHO, NEURO, ONCO, GASTRO | None | HTTP `200 OK`<br/>Array length = 5<br/>Each department contains `name`, `code`, `totalDoctors` | **PASS** |
| `TC_DEP_002` | Retrieve a single department by valid ID | FR-11 | Positive | Department ID 1 exists | 1. Send `GET /api/v1/departments/1`<br/>2. Inspect JSON response | `id: 1` | HTTP `200 OK`<br/>`name: "General Physicians"`<br/>`code: "GEN"`<br/>`totalDoctors: 35` | **PASS** |
| `TC_DEP_003` | Retrieve department by non-existent ID | NFR-02 | Negative | ID 9999 does not exist | 1. Send `GET /api/v1/departments/9999`<br/>2. Inspect error message & status | `id: 9999` | HTTP `404 Not Found`<br/>`success: false`<br/>`message: "Department not found with id: 9999"` | **PASS** |
| `TC_DEP_004` | Retrieve all doctors assigned to a department | FR-11 | Positive | Doctors exist in Department 1 | 1. Send `GET /api/v1/departments/1/doctors`<br/>2. Inspect returned doctors list | `id: 1` | HTTP `200 OK`<br/>Returns list of doctors where `departmentId: 1` | **PASS** |
| `TC_DEP_005` | Create a new hospital department (Admin) | FR-19 | Positive | Admin access | 1. Send `POST /api/v1/departments` with valid JSON payload<br/>2. Check created department | `{ "name": "Cardiology Center", "code": "CARDIO", "totalDoctors": 15 }` | HTTP `201 Created`<br/>Department returned with generated `id` | **PASS** |
| `TC_DEP_006` | Soft delete / deactivate a department | FR-19 | Positive | Department ID exists | 1. Send `DELETE /api/v1/departments/{id}`<br/>2. Query `GET /api/v1/departments?activeOnly=true` | Target `id` | HTTP `200 OK`<br/>Department `isActive` set to `false`, excluded from active list | **PASS** |

---

## 3. Doctor Directory & Search Module (FR-04 / FR-05)

| Test Case ID | Test Scenario | Req Ref | Type | Pre-conditions | Test Steps | Test Data | Expected Result | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_DOC_001` | Retrieve active doctor directory with $50 fee | FR-04<br/>BR-05 | Positive | Doctors are seeded and active | 1. Send `GET /api/v1/doctors`<br/>2. Inspect doctor records | None | HTTP `200 OK`<br/>List of doctors returned<br/>Each doctor has `consultationFee: 50.0` | **PASS** |
| `TC_DOC_002` | Search doctor by specialization keyword | FR-04 | Positive | Dr. Alan Vance exists in Neurosurgery | 1. Send `GET /api/v1/doctors/search?query=brain`<br/>2. Inspect matching doctors | `query: "brain"` | HTTP `200 OK`<br/>Returns Dr. Alan Vance (`specialization: "Complex Brain & Spine Surgery"`) | **PASS** |
| `TC_DOC_003` | Filter doctors by department name | FR-04 | Positive | Oncology department doctors exist | 1. Send `GET /api/v1/doctors?department=Cancer %26 Oncology Care`<br/>2. Check list | `department: "Cancer & Oncology Care"` | HTTP `200 OK`<br/>Returns only doctors under Oncology | **PASS** |
| `TC_DOC_004` | Retrieve doctor details by valid ID | FR-05 | Positive | Doctor ID 1 exists | 1. Send `GET /api/v1/doctors/1`<br/>2. Verify profile fields | `id: 1` | HTTP `200 OK`<br/>Returns `name`, `qualification`, `experienceYears`, `availableDays`, `availableTimeSlots` | **PASS** |
| `TC_DOC_005` | Retrieve doctor with invalid ID | NFR-02 | Negative | ID 9999 does not exist | 1. Send `GET /api/v1/doctors/9999` | `id: 9999` | HTTP `404 Not Found`<br/>`success: false`<br/>`message: "Doctor not found with id: 9999"` | **PASS** |

---

## 4. Doctor Availability & Scheduling Module (FR-07 / FR-09 / FR-13 / VR-02 / VR-03)

| Test Case ID | Test Scenario | Req Ref | Type | Pre-conditions | Test Steps | Test Data | Expected Result | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_AV_001` | Check availability for a valid future weekday | FR-07<br/>FR-13 | Positive | Doctor 1 has Thursday shift 09:00-17:00 | 1. Send `GET /api/v1/doctors/1/availability?date=2026-10-22`<br/>2. Verify generated time slots | `doctorId: 1`, `date: "2026-10-22"` (Thursday) | HTTP `200 OK`<br/>`isAvailable: true`<br/>`consultationFee: 50.0`<br/>Slots: 09:00-10:00, 10:00-11:00, etc. | **PASS** |
| `TC_AV_002` | Check availability with a past date | VR-02 | Negative / Boundary | Any doctor | 1. Send `GET /api/v1/doctors/1/availability?date=2020-01-01`<br/>2. Inspect error handling | `date: "2020-01-01"` | HTTP `200 OK`<br/>`isAvailable: false`<br/>`message: "Cannot check availability for past dates"`<br/>`availableSlots: []` | **PASS** |
| `TC_AV_003` | Real-time expiration of past slots on same-day check | VR-02 | Boundary | Doctor is active today | 1. Send `GET /api/v1/doctors/1/availability` with today's date<br/>2. Check slots before current local time | `date: [Today]` | Slots whose start time is before current time are marked `status: "UNAVAILABLE"`, `available: false` | **PASS** |
| `TC_AV_004` | Check availability on a doctor's off-day (Sunday) | FR-07 | Negative | Doctor has no Sunday schedule | 1. Send `GET /api/v1/doctors/1/availability?date=2026-10-18` (Sunday)<br/>2. Verify slot list | `date: "2026-10-18"` (Sunday) | HTTP `200 OK`<br/>`isAvailable: false`<br/>`message: "No available slots on this date"` | **PASS** |
| `TC_AV_005` | Fetch doctor's recurring weekly schedules | FR-09 | Positive | Doctor 1 has weekly schedules | 1. Send `GET /api/v1/doctors/1/schedules`<br/>2. Inspect schedule records | `doctorId: 1` | HTTP `200 OK`<br/>Returns list of recurring shifts with `dayOfWeek`, `startTime`, `endTime`, `slotDurationMinutes: 60` | **PASS** |
| `TC_AV_006` | Add a new schedule slot for a doctor | FR-09 | Positive | Authorized user | 1. Send `POST /api/v1/doctors/1/schedules` with new shift<br/>2. Check created schedule | `{ "dayOfWeek": "SUNDAY", "startTime": "10:00", "endTime": "14:00" }` | HTTP `201 Created`<br/>New schedule saved and returned | **PASS** |
| `TC_AV_007` | Delete a schedule slot | FR-09 | Positive | Schedule ID exists | 1. Send `DELETE /api/v1/doctors/1/schedules/{scheduleId}` | Target `scheduleId` | HTTP `200 OK`<br/>Schedule removed from database | **PASS** |

---

## 5. Patient Registration & Returning Discount Module (BR-02 / BR-05 / BR-06 / FR-11)

| Test Case ID | Test Scenario | Req Ref | Type | Pre-conditions | Test Steps | Test Data | Expected Result | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_PAT_001` | Register a new first-time patient | BR-05<br/>FR-01 | Positive | Email does not exist in DB | 1. Send `POST /api/v1/patients` with new email<br/>2. Inspect `isReturningPatient` flag | `email: "new.patient@test.com"`, `firstName: "Alice"` | HTTP `201 Created`<br/>`isReturningPatient: false`<br/>Standard fee = $50.00 | **PASS** |
| `TC_PAT_002` | Automatic detection of Returning Patient with 10% discount | BR-06<br/>FR-11 | Business Rule | Patient with email already registered | 1. Send `POST /api/v1/patients` using the same email address<br/>2. Check `isReturningPatient` | `email: "new.patient@test.com"` | HTTP `201 Created`<br/>`isReturningPatient: true`<br/>Qualifies for 10% discount ($45 final fee) | **PASS** |
| `TC_PAT_003` | Retrieve all registered patients | FR-01 | Positive | At least 1 patient registered | 1. Send `GET /api/v1/patients`<br/>2. Check returned list | None | HTTP `200 OK`<br/>List of patients returned with contact details | **PASS** |
| `TC_PAT_004` | Update patient profile details | FR-03 | Positive | Patient ID 1 exists | 1. Send `PUT /api/v1/patients/1` with updated phone & address<br/>2. Check updated fields | `{ "phone": "+1 555-9999", "address": "789 Health Ave" }` | HTTP `200 OK`<br/>Updated profile returned | **PASS** |
| `TC_PAT_005` | Fetch patient by non-existent ID | NFR-02 | Negative | ID 9999 does not exist | 1. Send `GET /api/v1/patients/9999` | `id: 9999` | HTTP `404 Not Found`<br/>`message: "Patient not found with id: 9999"` | **PASS** |

---

## 6. Security, Validation & Exception Handling (NFR-02 / VR-01)

| Test Case ID | Test Scenario | Req Ref | Type | Pre-conditions | Test Steps | Test Data | Expected Result | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_SEC_001` | Verify Cross-Origin Resource Sharing (CORS) header | NFR-02 | Security | Server running | 1. Send `OPTIONS /api/v1/doctors` with `Origin: http://localhost:3000`<br/>2. Inspect response headers | `Origin: http://localhost:3000` | Response contains header:<br/>`Access-Control-Allow-Origin: http://localhost:3000` | **PASS** |
| `TC_SEC_002` | Verify unified error response on 404 | NFR-02 | Exception | Server running | 1. Request an invalid entity ID | Path `/api/v1/doctors/8888` | Returns `{ "success": false, "message": "...", "data": null }` with HTTP 404 | **PASS** |
| `TC_SEC_003` | Verify timestamp is present on all API responses | NFR-01 | Compliance | Server running | 1. Execute any endpoint<br/>2. Check `timestamp` field | Any endpoint | JSON includes ISO-8601 `timestamp` string | **PASS** |
