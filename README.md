# 🚀 StudySync Backend

A production-ready real-time study collaboration backend built using Spring Boot 4, WebSockets, JWT Authentication, PostgreSQL, and cloud-native deployment architecture.

## 🌟 Features

- JWT Authentication + Refresh Tokens
- Google OAuth2 Login
- Real-time Room Chat (WebSocket + STOMP)
- Direct Messaging System
- Notifications System
- Study Analytics & Streak Tracking
- Khalti Payment Integration
- Cloudinary File Uploads
- Brevo Email Notifications
- Dockerized Deployment
- CI/CD with GitHub Actions

---

# 🛠️ Tech Stack

| Technology | Version |
|------------|----------|
| Java | 21 |
| Spring Boot | 4.0.6 |
| Spring Security | 7.x |
| PostgreSQL | 16 |
| WebSocket + STOMP | Latest |
| JJWT | 0.12.7 |
| MapStruct | 1.6.3 |
| Docker | Latest |

---

# 📁 Project Structure

```bash
src/main/java/com/sujan/studysync
├── config
├── controller
├── dto
├── enums
├── exception
├── mapper
├── model
├── repository
├── security
├── service
└── util
```

---

# ⚙️ Environment Variables

```env
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=your_database_url
DATABASE_USERNAME=your_db_username
DATABASE_PASSWORD=your_db_password
JWT_SECRET=your_secret
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_cloudinary_api_key
CLOUDINARY_API_SECRET=your_cloudinary_secret
BREVO_USERNAME=your_email
BREVO_PASSWORD=your_brevo_smtp_key
KHALTI_SECRET_KEY=your_khalti_key
```

---

# ▶️ Running Locally

## Prerequisites

- Java 21
- Maven 3+
- PostgreSQL
- Docker (optional)

## Run Application

```bash
./mvnw spring-boot:run
```

Application starts on:

```bash
http://localhost:8080
```

Swagger UI:

```bash
http://localhost:8080/swagger-ui.html
```

---

# 🐳 Docker

## Build Image

```bash
docker build -t studysync-backend .
```

## Run Container

```bash
docker run -p 8080:8080 studysync-backend
```

---

# 🤝 Contributing

Contributions are welcome.

## Contribution Flow

1. Fork the repository
2. Create a feature branch
3. Commit changes
4. Push your branch
5. Open a Pull Request

Please follow clean architecture and production-level coding standards.

---

# 📜 License

Licensed under the Apache License 2.0.
