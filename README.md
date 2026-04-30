# StudyGrind - Learning Management System

![StudyGrind Logo](src/main/resources/static/images/StudyGrind%20logo%20with%20owl%20mascot-clean.png)

**Engineered for Academic Growth**

StudyGrind is a comprehensive Learning Management System (LMS) designed for educational institutions. It provides a complete platform for student registration, course management, assignments, quizzes, payments, and administrative oversight.

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![License](https://img.shields.io/badge/License-Proprietary-red.svg)](LICENSE)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://www.docker.com/)

---

## 📋 Table of Contents

- [Features](#-features)
- [Technology Stack](#-technology-stack)
- [Prerequisites](#-prerequisites)
- [Installation & Setup](#-installation--setup)
- [Running the Application](#-running-the-application)
- [Docker Deployment](#-docker-deployment)
- [Configuration](#-configuration)
- [API Documentation](#-api-documentation)
- [Monitoring & Metrics](#-monitoring--metrics)
- [Default Users](#-default-users)
- [Project Structure](#-project-structure)
- [Testing](#-testing)
- [Contributing](#-contributing)
- [License](#-license)
- [Support](#-support)

---

## ✨ Features

### For Students
- 🔐 **Secure Registration** with Student ID Card verification
- 📚 **Course Enrollment** with 14-day free trial per course
- 📝 **Assignment Submission** with file upload support
- ✅ **Quiz Taking** with automatic grading
- 📖 **Study Notes** access (text resources)
- 📕 **Textbook Downloads** (PDF, DOC, etc.)
- 💳 **Payment Processing** (PayFast integration + mock mode)
- 📅 **Personal Timetable** management
- 🔔 **Real-time Notifications**
- 💬 **WhatsApp Group** access requests

### For Teachers
- 📘 **Course Creation** and management
- 📢 **Announcement Posting** to enrolled students
- 📋 **Assignment Creation** with due dates
- 📊 **Submission Grading** with feedback
- 🧪 **Quiz Creation** with multiple choice questions
- 📝 **Resource Upload** (notes and textbooks)
- 👥 **Student Enrollment** viewing

### For Administrators
- ✅ **Student Verification** (ID card approval/rejection)
- 👨‍🏫 **Teacher Account Management**
- 👩‍🎓 **Student Directory** management
- 📚 **Course Oversight** (create, delete)
- 💰 **Payment Monitoring** (revenue, subscriptions)
- 📱 **WhatsApp Access Management**
- ⚙️ **System Settings** configuration

### Technical Features
- 🔑 **JWT Authentication** with refresh tokens
- 🛡️ **Rate Limiting** for security
- 📊 **Prometheus Metrics** for monitoring
- 📖 **OpenAPI/Swagger Documentation**
- 🐳 **Docker Support** for easy deployment
- 🔄 **CI/CD Ready** (GitHub Actions)
- 🏥 **Health Checks** via Actuator
- 📧 **Email Notifications** (SMTP configurable)
- 💳 **PayFast Payment Integration**

---

## 🛠 Technology Stack

| Category | Technologies |
|----------|--------------|
| **Backend** | Java 17, Spring Boot 3.1.5 |
| **Security** | Spring Security, JWT |
| **Database** | H2 (dev), PostgreSQL (prod) |
| **ORM** | Spring Data JPA, Hibernate |
| **Migration** | Flyway |
| **Frontend** | HTML5, CSS3, JavaScript (Vanilla) |
| **API Docs** | OpenAPI 3.0, Swagger UI |
| **Monitoring** | Micrometer, Prometheus, Grafana |
| **Payment** | PayFast Integration |
| **Email** | JavaMail (SMTP) |
| **Build Tool** | Maven |
| **Container** | Docker, Docker Compose |
| **CI/CD** | GitHub Actions |

---

## 📋 Prerequisites

- **Java 17** or higher
- **Maven 3.8+** (or use included Maven wrapper)
- **Git** (for cloning)
- **Docker** (optional, for containerized deployment)
- **PostgreSQL** (optional, for production)

---

## 🚀 Installation & Setup

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/StudyGrind.git
cd StudyGrind
```

### 2. Configure Application

Edit `src/main/resources/application.properties` or create `application-dev.properties` for development:

```properties
# Database (H2 for development)
spring.datasource.url=jdbc:h2:file:./data/studygrind
spring.datasource.username=sa
spring.datasource.password=

# JWT Secret (generate your own)
app.jwt.secret=your-super-secret-jwt-key-at-least-32-characters

# Email (optional - set to false for development)
app.email.enabled=false

# PayFast (set to false for mock payments)
payfast.enabled=false
```

### 3. Build the Application

```bash
mvn clean package
```

### 4. Run the Application

```bash
mvn spring-boot:run
```

Or run the JAR directly:

```bash
java -jar target/studygrind-1.0.0.jar
```

### 5. Access the Application

Open your browser and navigate to:

```
http://localhost:8080
```

---

## 🐳 Docker Deployment

### Quick Start with Docker Compose

```bash
# Start the application with PostgreSQL
docker-compose up -d

# Start with monitoring (Prometheus + Grafana)
docker-compose -f docker-compose.yml -f docker-compose.monitoring.yml up -d
```

### Build Custom Image

```bash
docker build -t studygrind:latest .
docker run -p 8080:8080 studygrind:latest
```

### Environment Variables

Create a `.env` file:

```env
DB_PASSWORD=your_secure_password
JWT_SECRET=your-jwt-secret-min-32-chars
MAIL_USERNAME=support@studygrind.com
MAIL_PASSWORD=your_email_password
PAYFAST_MERCHANT_ID=your_merchant_id
PAYFAST_MERCHANT_KEY=your_merchant_key
```

---

## ⚙️ Configuration

### Application Profiles

| Profile | Description | Database |
|---------|-------------|----------|
| `default` | Development | H2 |
| `dev` | Development with debug logging | H2 |
| `prod` | Production | PostgreSQL |
| `docker` | Docker container | PostgreSQL |
| `https` | HTTPS enabled | Configurable |

### Running with Profiles

```bash
# Development profile
java -jar target/studygrind.jar --spring.profiles.active=dev

# Production profile
java -jar target/studygrind.jar --spring.profiles.active=prod

# HTTPS profile
java -jar target/studygrind.jar --spring.profiles.active=https
```

### Key Configuration Properties

```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/studygrind
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# JWT
app.jwt.secret=${JWT_SECRET}
app.jwt.expiration-ms=86400000  # 24 hours
app.jwt.refresh-expiration-ms=604800000  # 7 days

# File Upload
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB

# Trial Period
app.trial.days=14

# PayFast
payfast.enabled=true
payfast.merchant.id=${PAYFAST_MERCHANT_ID}
payfast.merchant.key=${PAYFAST_MERCHANT_KEY}
```

---

## 📚 API Documentation

### Swagger UI (Interactive)

Once the application is running, access:

```
http://localhost:8080/swagger-ui.html
```

**Note:** Swagger UI is secured and requires ADMIN authentication.

### OpenAPI JSON

```
http://localhost:8080/api-docs
http://localhost:8080/v3/api-docs
```

### Authentication

All API endpoints (except public ones) require a JWT token:

```bash
# Login to get token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@studygrind.com","password":"admin123"}'

# Use token in subsequent requests
curl -H "Authorization: Bearer YOUR_TOKEN" http://localhost:8080/api/students
```

### Key API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | User login |
| POST | `/api/auth/register` | Student registration |
| GET | `/api/courses` | List all courses |
| POST | `/api/courses/{id}/enroll` | Enroll in course |
| GET | `/api/assignments` | Get assignments |
| POST | `/api/assignments/{id}/submit` | Submit assignment |
| GET | `/api/quizzes` | List quizzes |
| POST | `/api/quizzes/{id}/submit` | Submit quiz |
| GET | `/api/billing/summary` | Get billing summary |
| POST | `/api/billing/pay-all` | Pay all outstanding |
| GET | `/api/verification/pending` | Pending verifications (admin) |
| POST | `/api/verification/verify/{id}` | Verify student (admin) |

---

## 📊 Monitoring & Metrics

### Actuator Endpoints

| Endpoint | Description | Access |
|----------|-------------|--------|
| `/actuator/health` | Application health | Public |
| `/actuator/info` | Application info | Public |
| `/actuator/metrics` | All metrics | Admin only |
| `/actuator/prometheus` | Prometheus metrics | Admin only |

### Available Metrics

```bash
# HTTP requests
http.server.requests

# Authentication
auth.login.success
auth.login.failure

# API calls
api.calls.total

# Database
hikaricp.connections.active
jdbc.connections.active

# JVM
jvm.memory.used
jvm.gc.pause
jvm.threads.live

# System
system.cpu.usage
process.uptime
```

### Prometheus Configuration

```yaml
scrape_configs:
  - job_name: 'studygrind'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
```

### Grafana Dashboard

Import the provided dashboard from `monitoring/grafana/dashboards/` or create your own using the metrics above.

---

## 👥 Default Users

The application comes with pre-configured users for testing:

| Role | Email | Password | Access |
|------|-------|----------|--------|
| **Admin** | `admin@studygrind.com` | `admin123` | Full system access |
| **Teacher** | `teacher@studygrind.com` | `teacher123` | Course management |
| **Student** | `student@studygrind.com` | `student123` | Learning portal |

**⚠️ Important:** Change these passwords in production!

---

## 📁 Project Structure

```
StudyGrind/
├── src/
│   ├── main/
│   │   ├── java/com/studygrind/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── dto/             # Data transfer objects
│   │   │   ├── exception/       # Custom exceptions
│   │   │   ├── filter/          # Request filters
│   │   │   ├── model/           # JPA entities
│   │   │   ├── repository/      # Data repositories
│   │   │   ├── security/        # Security configuration
│   │   │   ├── service/         # Business logic
│   │   │   └── util/            # Utility classes
│   │   └── resources/
│   │       ├── db/migration/    # Flyway migrations
│   │       ├── static/          # Static resources
│   │       ├── templates/       # HTML templates
│   │       └── application*.properties
│   └── test/                    # Unit and integration tests
├── monitoring/                  # Prometheus & Grafana configs
├── uploads/                     # File upload directory
├── docker-compose.yml           # Docker composition
├── Dockerfile                   # Docker build file
├── nginx.conf                   # Nginx configuration
├── .github/workflows/           # CI/CD pipelines
└── pom.xml                      # Maven configuration
```

---

## 🧪 Testing

### Run Unit Tests

```bash
mvn test
```

### Run Integration Tests

```bash
mvn verify
```

### Test Coverage

Generate coverage report:

```bash
mvn jacoco:report
```

Open `target/site/jacoco/index.html`

### Manual Testing Flow

1. **Register as Student** (upload student ID card)
2. **Login as Admin** → Approve student verification
3. **Login as Teacher** → Create course
4. **Login as Student** → Enroll in course
5. **Submit assignment** → Teacher grades it
6. **Take quiz** → Auto-graded
7. **View billing** → Pay for course (mock mode)
8. **Access textbooks** → Download resources

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Coding Standards

- Follow Spring Boot conventions
- Write unit tests for new features
- Document public APIs with Swagger annotations
- Use DTOs for request/response (never expose entities)

---

## 📄 License

This project is proprietary software. Unauthorized copying, distribution, or use is strictly prohibited.

© 2024 StudyGrind. All rights reserved.

---

## 📞 Support

### Documentation
- [Swagger UI](http://localhost:8080/swagger-ui.html)
- [API Docs](http://localhost:8080/api-docs)
- [Actuator Health](http://localhost:8080/actuator/health)

### Contact
- **Email:** support@studygrind.com
- **Website:** https://studygrind.com
- **Issues:** GitHub Issues page

### Troubleshooting

**Q: Application won't start - port 8080 already in use**
```bash
# Find process using port 8080 (Windows)
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :8080
kill -9 <PID>
```

**Q: Database connection errors**
- Ensure H2 database directory is writable
- For PostgreSQL, verify credentials and that service is running

**Q: File upload failing**
- Check `uploads/` directory permissions
- Verify file size is under 50MB
- Check file type is allowed

**Q: Email not sending**
- Set `app.email.enabled=true`
- Configure SMTP settings in `application.properties`
- Check email credentials

**Q: PayFast not working**
- Set `payfast.enabled=true`
- Add valid merchant credentials
- For testing, use sandbox mode

---

## 🎯 Roadmap

- [ ] Mobile app (React Native)
- [ ] Real-time chat between students and teachers
- [ ] Video conferencing integration
- [ ] Advanced analytics dashboard
- [ ] Certificate generation
- [ ] Bulk course enrollment
- [ ] Multi-language support
- [ ] Gamification features

---

## 🙏 Acknowledgments

- Spring Boot team for excellent framework
- PayFast for payment gateway integration
- All contributors and testers

---

**Built with ❤️ for education**
