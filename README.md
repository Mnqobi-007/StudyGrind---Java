# StudyGrind - Learning Management System

**A complete Learning Management System for educational institutions**

---

## 📖 About

StudyGrind is a comprehensive Learning Management System (LMS) built with Spring Boot. It provides a complete platform for student registration, course management, assignments, quizzes, payments, and administrative oversight.

---

## ✨ Features

- **Student Portal** - Register with ID verification, enroll in courses, submit assignments, take quizzes, download resources
- **Teacher Portal** - Create courses, post announcements, manage assignments, grade submissions, create quizzes
- **Admin Dashboard** - Verify students, manage users, monitor payments, configure settings
- **Payment Integration** - PayFast gateway with mock mode for testing
- **Security** - JWT authentication, role-based access, rate limiting
- **Monitoring** - Prometheus metrics, Grafana dashboards, health checks
- **API Documentation** - OpenAPI/Swagger UI

---

## 🛠 Tech Stack

- Java 17, Spring Boot 3.1.5, Spring Security, JWT
- Spring Data JPA, Hibernate, Flyway
- H2 (dev) / PostgreSQL (prod)
- HTML5, CSS3, JavaScript
- Docker, Prometheus, Grafana
- Maven

---

## 🚀 Quick Start

```bash
git clone https://github.com/Mnqobi-007/StudyGrind---Java
cd StudyGrind
mvn spring-boot:run
```

Access at `http://localhost:8080`

**Default credentials:**
- Admin: `admin@studygrind.com` / `admin123`
- Teacher: `teacher@studygrind.com` / `teacher123`
- Student: `student@studygrind.com` / `student123`

---

## 📋 Prerequisites

- Java 17+
- Maven 3.8+
- Git

---

## 🔧 Configuration

Edit `src/main/resources/application.properties` for database, JWT secret, email, and payment settings.

Run with different profiles:
```bash
java -jar target/studygrind.jar --spring.profiles.active=prod
```

---

## 🐳 Docker Deployment

```bash
docker-compose up -d
docker-compose -f docker-compose.yml -f docker-compose.monitoring.yml up -d
```

---

## 📚 API Documentation

Once running:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- API Docs: `http://localhost:8080/api-docs`
- Health Check: `http://localhost:8080/actuator/health`

---

## 📁 Project Structure

```
src/
├── main/java/com/studygrind/
│   ├── config/          # Configuration classes
│   ├── controller/      # REST controllers
│   ├── dto/             # Data transfer objects
│   ├── model/           # JPA entities
│   ├── repository/      # Data repositories
│   ├── security/        # Security configuration
│   ├── service/         # Business logic
│   └── util/            # Utilities
├── main/resources/
│   ├── db/migration/    # Flyway migrations
│   ├── static/          # CSS, JS, images
│   ├── templates/       # HTML files
│   └── application.properties
└── test/                # Unit tests
```

---

## 🧪 Testing

```bash
mvn test
mvn verify
```

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Open a Pull Request

---

## 📄 License

This project is certified by MIT License

© 2026 StudyGrind. All rights reserved.

---

**Built with ❤️ for education**
