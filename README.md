# CloudSouth Hospitality - Backend API

This is the backend API for the **CloudSouth Hospitality Management System**. It is built using Spring Boot and provides RESTful APIs for managing doctors, patients, appointments, and other hospital operations.

## 🚀 Tech Stack
- **Framework**: Spring Boot 3.x
- **Language**: Java 21
- **Build Tool**: Gradle
- **Database**: H2 Database (In-Memory for development & testing)
- **Architecture**: MVC (Model-View-Controller)

---

## 🛠️ Prerequisites
- **Java Development Kit (JDK) 21** must be installed on your system.
- No database installation is required (the project uses an embedded H2 database that spins up automatically).

---

## ⚙️ How to Run the Application

### 1. Open Terminal/Command Prompt
Navigate to the root directory of the project (`cloudsouth-backend`).

### 2. Run the Application
Use the provided Gradle Wrapper to start the Spring Boot server.

**For Windows:**
```cmd
.\gradlew.bat bootRun
```

**For Mac/Linux:**
```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`.

---

## 💾 Accessing the Database (H2 Console)
The project is configured to use a temporary in-memory database for immediate testing without any setup.
- **URL**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
- **JDBC URL**: `jdbc:h2:mem:cloudsouthdb`
- **Username**: `sa`
- **Password**: `password`

Every time you restart the server, the database will be fresh.

---

## 📡 API Endpoints & Test Data

The APIs have been completely structured to return a consistent JSON payload (`ApiResponse`). All API base URLs start with `http://localhost:8080/api/v1`.

### 👨‍⚕️ Doctors API

#### 1. Create a Doctor
- **URL**: `POST /api/v1/doctors`
- **Description**: Adds a new doctor to the hospital directory.
- **JSON Payload (Test Data)**:
```json
{
    "name": "Dr. Sarah Jenkins",
    "specialization": "Cardiology",
    "department": "Cardiology Dept",
    "qualification": "MD, FACC",
    "experienceYears": 12,
    "isActive": true
}
```

#### 2. Get All Doctors
- **URL**: `GET /api/v1/doctors`
- **Description**: Retrieves a list of all active doctors. You can append `?activeOnly=false` to see inactive ones.

#### 3. Get Doctor by ID
- **URL**: `GET /api/v1/doctors/1`
- **Description**: Retrieves a single doctor's details by their ID.

#### 4. Update a Doctor
- **URL**: `PUT /api/v1/doctors/1`
- **Description**: Updates an existing doctor's details.
- **JSON Payload (Test Data)**:
```json
{
    "name": "Dr. Sarah Jenkins-Smith",
    "specialization": "Advanced Cardiology",
    "department": "Cardiology Dept",
    "qualification": "MD, FACC, PhD",
    "experienceYears": 15,
    "isActive": true
}
```

#### 5. Delete a Doctor
- **URL**: `DELETE /api/v1/doctors/1`
- **Description**: Deletes a doctor from the system based on their ID.

---

### 🏥 Patients API

#### 1. Register a Patient
- **URL**: `POST /api/v1/patients`
- **Description**: Registers a new patient. The system will automatically check if the email exists to identify returning patients for discount handling.
- **JSON Payload (Test Data)**:
```json
{
    "firstName": "John",
    "lastName": "Doe",
    "email": "johndoe@example.com",
    "phone": "+1234567890",
    "address": "123 Main St",
    "dateOfBirth": "1990-01-01",
    "bloodGroup": "O+"
}
```

#### 2. Get All Patients
- **URL**: `GET /api/v1/patients`
- **Description**: Retrieves a list of all registered patients.

#### 3. Get Patient by ID
- **URL**: `GET /api/v1/patients/1`
- **Description**: Retrieves a single patient's details by their ID.

#### 4. Update a Patient
- **URL**: `PUT /api/v1/patients/1`
- **Description**: Updates an existing patient's details.
- **JSON Payload (Test Data)**:
```json
{
    "firstName": "Johnathon",
    "lastName": "Doe",
    "email": "johndoe@example.com",
    "phone": "+1987654321",
    "address": "456 New Ave",
    "dateOfBirth": "1990-01-01",
    "bloodGroup": "O+"
}
```

#### 5. Delete a Patient
- **URL**: `DELETE /api/v1/patients/1`
- **Description**: Deletes a patient from the system based on their ID.

---

## 🧪 Testing the APIs (Postman)
A Postman collection is included in the project repository so you can instantly test the working APIs.

1. Open **Postman**.
2. Click **Import** (top left).
3. Select the `CloudSouth_Postman_Collection.json` file located in the root of this repository.
4. You will see a collection named **CloudSouth API** with ready-to-run folders for all the **Doctors** and **Patients** CRUD operations exactly as listed above.

---

## 📦 Pushing to GitHub
To push this code to a new GitHub repository, run the following commands in the terminal:

```bash
git branch -M main
git remote add origin <YOUR_GITHUB_REPO_URL>
git push -u origin main
```
