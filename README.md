# ResearchFlow

Academic **Research Proposal and Review Management System** — OOAD course project (CSE556).

**Repository:** [https://github.com/abhishekreddy-msrit/Sem5-OOADProject](https://github.com/abhishekreddy-msrit/Sem5-OOADProject)

ResearchFlow replaces informal, email-scattered proposal handling with a single web application where **Students** draft and submit proposals, **Faculty** review assigned work, and **Administrators** assign reviewers and record the final institutional decision. Status, revision cycles, assignment history, reviews, decisions, and in-app notifications stay visible so every change is accountable.

---

## Features

Verified against the current codebase (`src/main`):

| Feature | Notes |
|---------|--------|
| Public registration | Student or Faculty only (`/register`) |
| Session login / logout | Spring Security form login |
| Role-based access control | `/student/**`, `/faculty/**`, `/admin/**` |
| Proposal create / edit | Draft fields: title, abstract, objectives, methodology, expected outcomes |
| Proposal submit / resubmit | Status transitions guarded on the domain aggregate |
| Admin reviewer assignment | Cycle-scoped `ReviewerAssignment` |
| Faculty review | Comments + recommendation `APPROVE` / `REJECT` / `REVISION` |
| Admin institutional decision | Approve, Reject, or Request revision (form value `REVISION` → domain `REQUEST_REVISION`) |
| Revision-cycle tracking | `revision_number`; prior-cycle assignments deactivated on resubmit |
| Historical assignments / reviews / decisions | Retained on the proposal detail pages |
| Notifications inbox | Mark-as-read supported |
| Access-denied page | Dedicated `/access-denied` for unauthorized roles |
| Idempotent demo seeding | Users + sample proposals when seed env vars are set |

Administrator accounts are **seed-only** (not created through public registration).

---

## Technology stack

| Technology | Purpose in ResearchFlow |
|------------|-------------------------|
| **Java 17** | Application language (`pom.xml` → `java.version`) |
| **Spring Boot 3.4** | Application framework, embedded Tomcat, auto-configuration |
| **Spring MVC** | HTTP controllers and request mapping |
| **Thymeleaf** | Server-rendered HTML templates |
| **Spring Security** | Authentication, session login, CSRF, role URL authorization |
| **Spring Data JPA / Hibernate** | Object–relational persistence (`open-in-view=false`) |
| **MariaDB** | Relational database |
| **Flyway** | Versioned schema migrations (`V1__researchflow_domain.sql`) |
| **Maven** | Dependencies, compile, test, and `spring-boot:run` |
| **BCrypt** | Password hashing (`BCryptPasswordEncoder`) |

### Why Maven?

Maven reads `pom.xml`, downloads the libraries ResearchFlow needs (Spring, Hibernate, MariaDB driver, Flyway, etc.) into a local cache, and runs build/test/run goals. You do **not** manually download JARs for each dependency.

This repository does **not** include a Maven Wrapper (`mvnw`). Install Maven 3.10+ (or use a full path to `mvn` / `mvn.cmd` on Windows).

---

## System requirements

| Requirement | How to check (PowerShell) |
|-------------|---------------------------|
| **JDK 17** | `java -version` → should show `17` |
| **Apache Maven 3.10+** | `mvn -v` (or your installed `mvn.cmd` path) |
| **MariaDB Server** | `Get-Service -Name "*maria*"` → Status **Running** |
| **Git** | `git --version` |
| **Web browser** | Chrome / Edge / Firefox |

Optional for tests: database `researchflow_test` (see Database setup).

---

## Clone the repository

Default branch: **`main`**.

```powershell
git clone https://github.com/abhishekreddy-msrit/Sem5-OOADProject.git
cd Sem5-OOADProject
```

If you already have a local copy:

```powershell
cd "D:\RIT\Third Year\OOAD\ResearchFlow"   # or your clone path
git checkout main
git pull
```

---

## Database setup

ResearchFlow expects MariaDB databases and an application user matching `.env` / `.env.example`.

### 1. Start MariaDB (Windows)

```powershell
Get-Service -Name "*maria*","*mysql*" | Format-Table Name, Status -AutoSize
Start-Service -Name "MariaDB"   # use the exact Name from Get-Service if different
```

### 2. Open a MariaDB client

Use HeidiSQL, DBeaver, MySQL Workbench, or the CLI as a **server admin** user (not necessarily the app user):

```powershell
mariadb -u root -p
```

### 3. Create databases and application user

Replace `your_local_password` with a password **you** choose. Use the **same** password later in `.env` as `DB_PASSWORD`.

```sql
CREATE DATABASE IF NOT EXISTS researchflow
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS researchflow_test
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'researchflow'@'localhost'
  IDENTIFIED BY 'your_local_password';

GRANT ALL PRIVILEGES ON researchflow.* TO 'researchflow'@'localhost';
GRANT ALL PRIVILEGES ON researchflow_test.* TO 'researchflow'@'localhost';
FLUSH PRIVILEGES;
```

- **`researchflow`** — runtime application database (`DB_URL` in `.env`).
- **`researchflow_test`** — automated tests only (`mvn test` / `mvn verify`).
- **`researchflow`@`localhost`** — dedicated app user (not the MariaDB `root` account).

If only the test database is missing, an admin can also run `scripts/create-test-database.sql`.

### 4. Verify connectivity

```sql
SHOW DATABASES LIKE 'researchflow%';
```

On application startup you should later see a log line similar to:

`MariaDB connectivity verified (product=MariaDB, version=..., catalog=researchflow)`

Flyway creates/validates tables from `src/main/resources/db/migration/V1__researchflow_domain.sql`. Hibernate uses `ddl-auto: validate` and does **not** invent schema.

---

## Environment configuration

Configuration is loaded from a local **`.env`** file by `DotEnvEnvironmentPostProcessor` (required `DB_URL`, `DB_USER`, `DB_PASSWORD` via `application.yml`).

### 1. Copy the template

```powershell
Copy-Item .env.example .env
```

`.env` is listed in `.gitignore` and must **never** be committed.

### 2. Edit `.env` — required variables

| Variable | Meaning |
|----------|---------|
| `DB_URL` | JDBC URL, normally `jdbc:mariadb://localhost:3306/researchflow` |
| `DB_USER` | Database username (e.g. `researchflow`) |
| `DB_PASSWORD` | Password for that user (must match what you set in MariaDB) |
| `ADMIN_USERNAME` / `ADMIN_PASSWORD` | Seed Administrator |
| `SEED_STUDENT_USERNAME` / `SEED_STUDENT_PASSWORD` | Seed Student |
| `SEED_FACULTY_USERNAME` / `SEED_FACULTY_PASSWORD` | Seed Faculty |

Optional test overrides (defaults already point at `researchflow_test`):

- `TEST_DB_URL`, `TEST_DB_USER`, `TEST_DB_PASSWORD`

Placeholder values in `.env.example` (e.g. `change_me`) are **examples only**. Set your own passwords locally.

If any seed variable is blank, `DemoDataSeeder` skips demo accounts and logs a warning.

---

## Run the application

### Port 8080

```powershell
Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue |
  Select-Object LocalPort, State, OwningProcess
```

If a leftover Java/ResearchFlow process holds the port, stop that PID, then start again. Do not start a second instance while one is already listening.

### Start

From the project root (folder that contains `pom.xml`):

```powershell
mvn spring-boot:run
```

If `mvn` is not on your PATH (common on Windows course machines):

```powershell
& "E:\Maven\apache-maven-3.10.1\bin\mvn.cmd" spring-boot:run
```

Adjust the path to wherever Maven is installed on **your** PC.

### What this command does

1. Compiles the project if needed.
2. Starts Spring Boot on **port 8080**.
3. Connects to MariaDB using `.env`.
4. Runs Flyway validate/migrate.
5. Starts Tomcat and registers security + controllers.
6. Runs `DemoDataSeeder` (when seed env vars are set).

### Successful startup — look for

In the terminal logs:

1. Flyway: migration validated / schema up to date  
2. `Tomcat started on port 8080`  
3. `Started ResearchFlowApplication`  
4. `MariaDB connectivity verified (... catalog=researchflow)`  
5. `Demo users ready (usernames only): admin='…', student='…', faculty='…'`

If the datasource fails, fix MariaDB service status and `DB_*` values in `.env`, then restart.

### Stop

Press **Ctrl+C** in the terminal that is running Maven.

---

## Access the application

**URL:** [http://localhost:8080](http://localhost:8080)

| Route | Access |
|-------|--------|
| `/login` | Public |
| `/register` | Public (Student / Faculty only) |
| `/home` | After login → role redirect |
| `/student/proposals` | Student dashboard |
| `/student/proposals/new` | New draft |
| `/faculty/assignments` | Faculty assigned proposals |
| `/admin/proposals` | Administrator dashboard |
| `/notifications` | Notifications inbox |
| `/access-denied` | Dedicated unauthorized page |
| `/logout` | POST logout (header **Logout** button) |

After successful login, `/home` redirects by role:

| Role | Redirect |
|------|----------|
| STUDENT | `/student/proposals` |
| FACULTY | `/faculty/assignments` |
| ADMINISTRATOR | `/admin/proposals` |

---

## Demo accounts

Seed credentials come **only** from your local `.env`. They are not universal and are not published here.

| Role | Username source | Password source |
|------|-----------------|-----------------|
| Administrator | `ADMIN_USERNAME` | `ADMIN_PASSWORD` |
| Student | `SEED_STUDENT_USERNAME` | `SEED_STUDENT_PASSWORD` |
| Faculty | `SEED_FACULTY_USERNAME` | `SEED_FACULTY_PASSWORD` |

`.env.example` uses placeholder usernames `admin`, `student1`, `faculty1` and password placeholders such as `change_me`. Replace them with values you control.

**Seeding behaviour (`DemoDataSeeder`):**

- Creates seed users if they do not already exist (passwords hashed with BCrypt).
- If the seed student has **no** proposals yet, creates several sample proposals in mixed statuses.
- If proposals already exist for that student, proposal seeding is skipped (`Demo proposals already present`).

**Public registration:** `/register` allows **Student** or **Faculty** only. Administrators remain seed-only.

Always click **Logout** before switching roles in a browser demo.

---

## Complete demo workflow

Use a **new** proposal (recommended title: `Live Demo: ResearchFlow Workflow Walkthrough`) so the story is clear even if older seed proposals already exist.

Field minimums from validation: abstract ≥ 50 characters; objectives / methodology / expected outcomes ≥ 20 characters; review comments ≥ 10 characters.

### 1. Student — create and submit

1. Open http://localhost:8080/login  
2. Log in as the seed **Student** (from `.env`).  
3. Click **New draft**.  
4. Fill title, abstract, objectives, methodology, expected outcomes → **Create draft**.  
5. Confirm status **DRAFT**.  
6. Click **Submit** → status **SUBMITTED**, revision **1**.

*Internally:* `StudentController` → `ProposalWorkflowFacade` → `ResearchProposal.submit()`.

### 2. Administrator — assign reviewer

1. **Logout** → log in as **Administrator**.  
2. Open `/admin/proposals` → open the Live Demo proposal.  
3. Under **Assign / reassign reviewer**, select the seed Faculty → **Assign reviewer**.  
4. Status becomes **UNDER_REVIEW**.

*Internally:* Facade `assignReviewer` creates a cycle-scoped assignment and publishes `REVIEWER_ASSIGNED` (notification after commit).

### 3. Faculty — submit review

1. **Logout** → log in as **Faculty**.  
2. Open **Assignments** → open the proposal.  
3. Enter comments; set recommendation to **REVISION** (or APPROVE / REJECT).  
4. **Submit review**.

*Internally:* A `Review` is stored 1:1 with the active `ReviewerAssignment`. Status stays **UNDER_REVIEW** until Admin decides.

### 4. Administrator — request revision

1. **Logout** → Admin → open the proposal.  
2. Confirm the Faculty review in progress/history.  
3. **Record decision** → Action **Request revision** (form value `REVISION`) → enter a **mandatory** note → **Submit decision**.  
4. Status **REVISION_REQUIRED** (stored as `REQUEST_REVISION`).

*Say in viva:* Faculty recommendations are advisory; the Administrator records the institutional decision.

### 5. Student — revise and resubmit

1. **Logout** → Student → open the proposal.  
2. **Edit** → update content → **Save changes**.  
3. Click **Resubmit**.  
4. Status **SUBMITTED**, revision **2**. Prior-cycle assignment shows **inactive**; earlier reviews/decisions remain in history.

### 6. Cycle 2 — reassign, approve review, final approval

1. **Admin:** Assign Faculty again → **UNDER_REVIEW**.  
2. **Faculty:** Submit a new review with recommendation **APPROVE**.  
3. **Admin:** Action **Approve** → **Submit decision** → status **APPROVED**.

### 7. Notifications and access control

1. Open **Notifications** (`/notifications`) and show workflow alerts; use **Mark as read** if unread.  
2. As **Student**, browse to http://localhost:8080/admin/proposals → dedicated **Access denied** page.

*Observer (accurate wording):* the Facade publishes a `ProposalWorkflowEvent`; after the workflow transaction commits, `NotificationEventListener` (`AFTER_COMMIT`) calls `NotificationWriteService` (`REQUIRES_NEW`). This is **not** `@Async` unless you add async configuration (the project does not).

---

## Build and test

Requires database `researchflow_test` and the same app user grants.

```powershell
mvn clean verify
```

Or with a full Maven path on Windows:

```powershell
& "E:\Maven\apache-maven-3.10.1\bin\mvn.cmd" clean verify
```

Tests use Spring profile `test` and **only** `researchflow_test` (they do not wipe your development `researchflow` data).

---

## Architecture (brief)

```text
Presentation (Spring MVC + Thymeleaf controllers/forms)
        ↓
Application (RegistrationService / UserCreator, ProposalWorkflowFacade,
             NotificationEventListener + NotificationWriteService)
        ↓
Domain (User hierarchy, ResearchProposal aggregate, assignments, reviews,
        decisions, notifications + repository ports)
        ↑
Infrastructure (JPA adapters, MariaDB, Flyway)
SecurityConfig — cross-cutting session auth + role matchers
```

**Design patterns**

| Pattern | Location |
|---------|----------|
| Factory Method | `UserCreator` + `StudentUserCreator` / `FacultyUserCreator` |
| Facade | `ProposalWorkflowFacade` |
| Observer | `ProposalWorkflowEvent` + `NotificationEventListener` + `NotificationWriteService` |

---

## Project layout (high level)

```text
src/main/java/com/rit/researchflow/
  presentation/     Controllers, forms, Thymeleaf advice
  application/      Facade, registration, events, notifications
  domain/           Entities, enums, repository interfaces
  infrastructure/   JPA adapters, SecurityConfig
  config/           .env loading, DemoDataSeeder, DB verifier
src/main/resources/
  templates/        Thymeleaf UI
  db/migration/     Flyway SQL
  application.yml
scripts/            Helper SQL (e.g. create-test-database.sql)
.env.example        Safe template for local secrets
```

---

## Troubleshooting

| Problem | What to check |
|---------|----------------|
| `Port 8080 was already in use` | Another ResearchFlow/Java process is running. Use the existing app at http://localhost:8080 **or** stop that PID, then start once. |
| Cannot connect to database | MariaDB service Running? `DB_URL` / `DB_USER` / `DB_PASSWORD` match the MariaDB user? |
| Demo seed skipped | All `ADMIN_*` and `SEED_*` variables set in `.env`? |
| Login fails | Password in `.env` vs what you type; Caps Lock; re-seed only creates users if username is missing (it does not reset an existing user’s password). |
| Access denied on a normal page | Wrong role still logged in → **Logout**, then login as the correct role. |
| Flyway / Hibernate validate errors | Confirm you are on database `researchflow` and migration `V1` has been applied; do not DROP the DB as a routine fix. |

Harmless startup notes you may see: Flyway warning that MariaDB 12.x is newer than the tested matrix; occasional `WSREP_ON` JDBC warning on some MariaDB builds.

---

## Extra local guides

If present in your clone:

- `DEMO_QUICK_REFERENCE.md` — one-page live-demo card  
- `DEMO_GUIDE.md` — longer demonstration / viva notes  

The **README** is the primary onboarding document for anyone cloning the repository.

---

## License / course context

Course project for Object Oriented Analysis and Design. Not a production deployment package.
