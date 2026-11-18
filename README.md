# ResearchFlow

Academic Research Proposal and Review Management System (OOAD course project).

## Stack

- Java 17
- Spring Boot 3.4
- Maven 3.10+
- Thymeleaf (screens not implemented yet)
- Spring Data JPA / Hibernate
- Spring Security (session + BCrypt)
- Flyway (`flyway-core` + `flyway-mysql` for MariaDB support)
- MariaDB 12.x

## Prerequisites

1. JDK 17
2. Apache Maven 3.10+
3. MariaDB with databases:

```sql
CREATE DATABASE researchflow CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE researchflow_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'researchflow'@'localhost' IDENTIFIED BY 'your_password_here';
GRANT ALL PRIVILEGES ON researchflow.* TO 'researchflow'@'localhost';
GRANT ALL PRIVILEGES ON researchflow_test.* TO 'researchflow'@'localhost';
FLUSH PRIVILEGES;
```

If only the test database is missing, run `scripts/create-test-database.sql` as a MariaDB admin user.

## Configuration

1. Copy `.env.example` to `.env`.
2. Set `DB_URL`, `DB_USER`, `DB_PASSWORD`.
3. Set demo seed usernames/passwords (`ADMIN_*`, `SEED_STUDENT_*`, `SEED_FACULTY_*`) for local demo data.
4. Never commit `.env`.

`DotEnvEnvironmentPostProcessor` loads `.env` explicitly.

## Design patterns (application layer)

| Pattern | Where |
|---------|--------|
| Factory Method | `UserCreator` + `StudentUserCreator` / `FacultyUserCreator` |
| Facade | `ProposalWorkflowFacade` |
| Observer | `ProposalWorkflowEvent` + `NotificationEventListener` (`AFTER_COMMIT`) + `NotificationWriteService` (`REQUIRES_NEW`) |

Administrator accounts are seed-only (not public registration).

## Build and test

```powershell
& "E:\Maven\apache-maven-3.10.1\bin\mvn.cmd" clean verify
```

Persistence/application tests use profile `test` and database `researchflow_test` only.

## Run

```powershell
& "E:\Maven\apache-maven-3.10.1\bin\mvn.cmd" spring-boot:run
```

On startup, Flyway validates/migrates schema and (when seed env vars are set) loads demo users and proposals.

Application URL: [http://localhost:8080](http://localhost:8080)

Default Spring Security login is available. UI pages are not implemented yet.

## Current milestone status

Implemented:

- Domain model + persistence (Step 2)
- Factory Method registration
- Proposal workflow facade
- Observer notifications (after-commit)
- Session security + roles
- Idempotent demo seeding

Not yet implemented:

- Controllers / Thymeleaf screens
- End-to-end UI workflows
