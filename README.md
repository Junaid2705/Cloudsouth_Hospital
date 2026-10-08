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

## 🧪 Testing the APIs (Postman)
A Postman collection is included in the project repository so you can instantly test the working APIs.

1. Open **Postman**.
2. Click **Import** (top left).
3. Select the `CloudSouth_Postman_Collection.json` file located in the root of this repository.
4. You will see a collection named **CloudSouth API** with ready-to-run requests for:
   - Server Health Check
   - Creating/Viewing Doctors
   - Registering/Viewing Patients

---

## 📁 Features Implemented So Far (Sprint 1)
- **Standardized API Responses**: Every API returns a consistent JSON wrapper (`ApiResponse`).
- **Global Error Handling**: Custom `ResourceNotFoundException` and centralized error trapping.
- **Doctor Module**: Full CRUD APIs (Create, Read, Update, Delete) to manage hospital doctors.
- **Patient Module**: Full CRUD APIs with initial logic to identify returning patients for discount handling.
- **CORS Configuration**: Fully configured to allow requests from frontend (React/Next.js) running on `localhost:3000`.

---

## 📦 Pushing to GitHub
To push this code to a new GitHub repository, run the following commands in the terminal:

```bash
git branch -M main
git remote add origin <YOUR_GITHUB_REPO_URL>
git push -u origin main
```
