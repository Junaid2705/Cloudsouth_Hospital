# QA Testing Masterclass: Step-by-Step Learning Guide
### Project: CloudSouth Hospital Management System

Welcome to the **QA Testing & Test Case Creation Guide**! This tutorial will teach you step-by-step how professional Quality Assurance (QA) engineers study requirements, write industry-standard test cases, and create automated API assertions in Postman.

---

## Step 1: How QA Engineers Analyze Requirements

Before writing a single test case, a QA engineer reads the **BRD (Business Requirement Document)** and **FRD (Functional Requirements Document)** to identify:
1. **Core Business Rules (BR)**: Explicit rules that the software must obey (e.g., standard fee is $50; returning patient receives 10% discount).
2. **Validation Rules (VR)**: What user input is permitted or blocked (e.g., past dates cannot be booked; duplicate emails are blocked).
3. **Acceptance Criteria (AC)**: The conditions under which a feature is accepted as working.

### Example from CloudSouth FRD:
- **Requirement (`FR-11` & `BR-06`)**:
  > *"If the patient has a previous visit/registration, they are treated as a returning patient and a 10% ($5) discount is automatically applied to the $50 appointment fee. Final payable amount is $45."*
- **QA Thought Process**:
  - **Positive Test**: Register a brand new patient -> Verify `isReturningPatient = false`, fee = $50.
  - **Returning Test**: Register/Query with an existing email -> Verify `isReturningPatient = true`, fee = $45.
  - **Edge Case**: Attempt registration with empty email or invalid email format -> Verify system rejects with 400 Bad Request.

---

## Step 2: Anatomy of a Professional QA Test Case

Every formal test case contains 9 standard fields:

| Field Name | Description | Example from CloudSouth |
| :--- | :--- | :--- |
| **Test Case ID** | Unique code | `TC_DOC_AVAIL_001` |
| **Test Scenario** | High-level objective | Verify doctor availability for a valid future weekday |
| **Requirement Ref** | Traced FRD / BRD ID | `FR-07`, `FR-13`, `VR-03` |
| **Test Type** | Category | Functional / Positive / API |
| **Pre-conditions** | State before test runs | Doctor with ID 1 is active with schedule configured |
| **Test Steps** | Step-by-step actions | 1. Send `GET /api/v1/doctors/1/availability?date=2026-10-22`<br/>2. Inspect HTTP status & JSON payload |
| **Test Data** | Inputs used | `doctorId: 1`, `date: 2026-10-22` |
| **Expected Result** | What should happen | Status 200, `isAvailable: true`, slots list populated, fee is $50.00 |
| **Actual Result & Status**| Result after execution | Status 200, matching expected slots -> **PASS** |

---

## Step 3: Positive vs. Negative vs. Boundary Testing

When testing an API or application, always test 3 categories:

```
                  ┌────────────────────────────────────────┐
                  │          QA Test Coverage              │
                  └────────────────────────────────────────┘
                                      │
            ┌─────────────────────────┼─────────────────────────┐
            ▼                         ▼                         ▼
   [ Positive Testing ]      [ Negative Testing ]      [ Boundary / Edge ]
  Valid inputs & flows      Invalid inputs & errors   Edges, past dates, empty
  e.g. Valid date, active   e.g. Doctor ID 999999     e.g. Date is today, 
  doctor -> 200 OK slots    -> 404 Not Found error    slot time already passed
```

1. **Positive Tests**: Proves the feature works under normal, valid conditions.
   - Example: Request availability for Dr. Sarah Jenkins on a Thursday -> Returns 200 OK with open 60-minute slots.
2. **Negative Tests**: Proves the application handles invalid input gracefully without crashing.
   - Example: Request doctor availability for a non-existent ID `9999` -> Returns 404 Not Found with clean JSON error message.
3. **Boundary / Edge Tests**: Tests extreme conditions.
   - Example: Request availability for a date in the past (`2020-01-01`) -> System must return `isAvailable: false` and message `"Cannot check availability for past dates"` (`VR-02`).

---

## Step 4: How Postman Automated Assertions Work

In Postman, you don't just send a request and look with your eyes—you write **JavaScript assertions** in the **Scripts / Tests** tab. When you run the collection, Postman checks every test automatically!

### Common Postman Assertions Syntax:

#### 1. Check HTTP Status Code:
```javascript
pm.test("Status code is 200 OK", function () {
    pm.response.to.have.status(200);
});
```

#### 2. Check JSON Response Wrapper (`ApiResponse`):
```javascript
pm.test("Response wrapper has success: true", function () {
    var response = pm.response.json();
    pm.expect(response.success).to.eql(true);
    pm.expect(response.message).to.be.a('string');
});
```

#### 3. Verify Specific Data Fields:
```javascript
pm.test("Consultation fee is standard $50.00", function () {
    var response = pm.response.json();
    pm.expect(response.data.consultationFee).to.eql(50.0);
});
```

#### 4. Verify Array Length & Non-Empty Slots:
```javascript
pm.test("Time slots list is populated", function () {
    var response = pm.response.json();
    pm.expect(response.data.availableSlots).to.be.an('array');
    pm.expect(response.data.availableSlots.length).to.be.above(0);
});
```

---

## Step 5: How to Run the Postman Test Suite

1. Open **Postman**.
2. Click **Import** (top left).
3. Select `qa-testing/CloudSouth_API_TestSuite.postman_collection.json`.
4. Click on the collection name **"CloudSouth Hospital API Test Suite"**.
5. Click **Run Collection** (Runner button).
6. Click **Run CloudSouth Hospital API Test Suite**.
7. Watch Postman execute every API call and display green **PASS** checkmarks for all business rules!
