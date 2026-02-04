# 🏡 Home Staging AI - Back-end

[![Java Version](https://img.shields.io/badge/Java-25-blue.svg)](https://www.oracle.com/java/technologies/javase/jdk25-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Powering the future of real estate with AI-driven home staging. This repository contains the core API services, authentication logic, and image processing pipeline for the Home Staging AI platform.

---

## 🚀 Teck Stack

- **Core:** Java 25 & Spring Boot 4.0.2
- **Security:** Spring Security & JWT (JSON Web Token)
- **Database:** MySQL
- **ORM:** Spring Data JPA & Lombok
- **Storage:** Cloudinary (for image management)
- **Auth:** OAuth2 with Google Integration

---

## ✨ Key Features

- **🔐 Secure Authentication:** Hybrid JWT-based authentication supporting traditional login/register and Google OAuth2.
- **🖼️ Image Management:** Complete CRUD for room images with Cloudinary integration for scalable storage.
- **🤖 AI Pipeline:** Backend infrastructure to support AI-driven staging transformations.
- **📊 Monitoring:** Built-in health checks and metrics via Spring Boot Actuator.

---

## 📂 Project Structure

```text
src/main/java/io/home/staging/
├── config/         # ⚙️ Configuration (Cloudinary, etc.)
├── controller/     # 🕹️ REST API Endpoints
├── entity/         # 💾 Database Models
├── model/          # 📦 DTOs (Requests & Responses)
├── repository/     # 🗃️ Data Access Layers
├── security/       # 🛡️ Security Filters & JWT logic
└── service/        # 🧠 Business Logic
```

---

## 🛠️ Getting Started

### Prerequisites

- **JDK 25** installed
- **MySQL** instance running
- **Maven** 3.9+

### Environment Variables

Ensure you have the following environment variables configured:

```bash
DB_URL=jdbc:mysql://localhost:3306/staging
DB_USERNAME=your_username
DB_PASSWORD=your_password
CLOUDINARY_URL=your_cloudinary_url
GOOGLE_CLIENT_ID=your_google_id
JWT_SECRET=your_jwt_secret
```

### Run Locally

1. **Clone the repository**
2. **Install dependencies:**
   ```bash
   ./mvnw clean install
   ```
3. **Run the application:**
   ```bash
   ./mvnw spring-boot:run
   ```

---

## 📜 License

Distributed under the MIT License. See `LICENSE` for more information.

---

Made with ❤️ for modern real estate.
