# ResearchFlow

Academic Research Proposal and Review Management System (OOAD course project).

## Stack

- Java 17
- Spring Boot 3.4
- Maven 3.10+
- Thymeleaf
- Spring Data JPA / Hibernate
- Spring Security
- MariaDB 12.x

## Prerequisites

1. JDK 17
2. Apache Maven 3.10+
3. MariaDB server running locally
4. Database and user already created (example):

```sql
CREATE DATABASE researchflow CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'researchflow'@'localhost' IDENTIFIED BY 'your_password_here';
GRANT ALL PRIVILEGES ON researchflow.* TO 'researchflow'@'localhost';
FLUSH PRIVILEGES;
```

## Configuration (credentials)

Database credentials are **never** hardcoded.

1. Copy `.env.example` to `.env` in the project root.
2. Set `DB_URL`, `DB_USER`, and `DB_PASSWORD`.
3. Keep `.env` private — it is listed in `.gitignore`.

Example `.env`:

```env
DB_URL=jdbc:mariadb://localhost:3306/researchflow
DB_USER=researchflow
DB_PASSWORD=your_password_here
```

You may instead export the same variables in your OS environment. OS environment variables take precedence over `.env`.

A custom `DotEnvEnvironmentPostProcessor` loads `.env` explicitly. Spring Boot does not load `.env` by itself.

## Build

From the project root (PowerShell):

```powershell
# If mvn is not on PATH:
& "E:\Maven\apache-maven-3.10.1\bin\mvn.cmd" clean verify
```

Or, if Maven is on your PATH:

```powershell
mvn clean verify
```

## Run

```powershell
& "E:\Maven\apache-maven-3.10.1\bin\mvn.cmd" spring-boot:run
```

On successful startup you should see a log line confirming MariaDB connectivity (product and catalog only — no secrets).

Application URL: [http://localhost:8080](http://localhost:8080)

## Schema strategy (current phase)

`spring.jpa.hibernate.ddl-auto` is set to `none`.

- No automatic schema creation or recreation
- Domain entities and tables will be introduced in a later step

## Project layout (packages)

```
com.rit.researchflow
├── presentation
├── application
├── domain
├── infrastructure
└── config
```

## Current phase status

Initial Spring Boot + MariaDB connectivity setup only.

Not yet implemented: domain entities, repositories, authentication/registration, workflow, design-pattern wiring, or UI screens.
