# ResearchFlow — Live Demonstration Guide

Prepared from the current local workspace:
`D:\RIT\Third Year\OOAD\ResearchFlow`

**Important:** The root `README.md` still says Thymeleaf screens are “not implemented yet”. That is outdated. Controllers and templates **are** implemented. Trust this guide and the source under `src/main`, not the stale README milestone note.

Never commit `.env`. Never project password values from `.env` on screen—open `.env` privately and type passwords yourself.

---

## 1. Project overview

ResearchFlow is an academic research proposal and peer-review system with three roles:

| Role | What they do |
|------|----------------|
| **Student** | Register/login, draft/edit/submit/resubmit proposals, view notifications |
| **Faculty** | Review assigned proposals; submit comments + recommendation |
| **Administrator** | Assign reviewers; record institutional APPROVE / REJECT / REQUEST_REVISION |

**Lifecycle**

`DRAFT → SUBMITTED → UNDER_REVIEW → APPROVED | REJECTED | REVISION_REQUIRED`

After `REVISION_REQUIRED`, the student edits and resubmits → `SUBMITTED` at a **new revision number**. Prior-cycle assignments are deactivated; history is kept.

**Patterns (application layer)**

| Pattern | Where |
|---------|--------|
| Factory Method | `UserCreator` + `StudentUserCreator` / `FacultyUserCreator` via `RegistrationService` |
| Facade | `ProposalWorkflowFacade` |
| Observer | `ProposalWorkflowEvent` → `NotificationEventListener` (`AFTER_COMMIT`) → `NotificationWriteService` (`REQUIRES_NEW`) |

**Layers**

Presentation (Thymeleaf controllers) → Application (Facade / registration / notifications) → Domain (entities + repository ports) ← Infrastructure (JPA adapters, Flyway, MariaDB). Security is cross-cutting (`SecurityConfig`).

---

## 2. Prerequisites

| Tool | Expected |
|------|----------|
| JDK | **17** (`java.version` in `pom.xml`) |
| Maven | 3.10+ (system Maven; **no** `mvnw` wrapper in this repo) |
| MariaDB | Local server with `researchflow` and (for tests) `researchflow_test` |
| App DB user | `researchflow`@`localhost` (matches `.env.example`) |
| Browser | Chrome/Edge |

Maven path used in this project’s docs:

`E:\Maven\apache-maven-3.10.1\bin\mvn.cmd`

---

## 3. Environment setup (PowerShell, exact order)

```powershell
cd "D:\RIT\Third Year\OOAD\ResearchFlow"
```

### 3.1 Check Java 17

```powershell
java -version
```

Expect a line containing `17`.

### 3.2 Check Maven (no wrapper)

```powershell
Test-Path .\\mvnw.cmd
& "E:\Maven\apache-maven-3.10.1\bin\mvn.cmd" -v
```

`Test-Path` should be `False`.

### 3.3 Check MariaDB service and start it

```powershell
Get-Service -Name "*maria*","*mysql*" | Format-Table Name, Status, StartType -AutoSize
```

If Status is **Stopped**:

```powershell
Start-Service -Name "<exact Name from the table>"
```

On this machine the expected service is typically `MariaDB`.

### 3.4 Verify databases exist

```sql
SHOW DATABASES LIKE 'researchflow%';
```

You should see at least `researchflow`. For tests also `researchflow_test`.

If missing (run once as admin):

```sql
CREATE DATABASE researchflow CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE researchflow_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'researchflow'@'localhost' IDENTIFIED BY '<choose-a-password>';
GRANT ALL PRIVILEGES ON researchflow.* TO 'researchflow'@'localhost';
GRANT ALL PRIVILEGES ON researchflow_test.* TO 'researchflow'@'localhost';
FLUSH PRIVILEGES;
```

Or for test DB only: `scripts/create-test-database.sql`.

### 3.5 Configure `.env`

```powershell
Test-Path .\\.env
# If False:
Copy-Item .\\.env.example .\\.env
```

Required keys:

| Variable | Purpose |
|----------|---------|
| `DB_URL` | Usually `jdbc:mariadb://localhost:3306/researchflow` |
| `DB_USER` | App DB username |
| `DB_PASSWORD` | App DB password |
| `ADMIN_USERNAME` / `ADMIN_PASSWORD` | Seed Administrator |
| `SEED_STUDENT_USERNAME` / `SEED_STUDENT_PASSWORD` | Seed Student |
| `SEED_FACULTY_USERNAME` / `SEED_FACULTY_PASSWORD` | Seed Faculty |

`DotEnvEnvironmentPostProcessor` loads `.env` at startup.

### 3.6 Flyway and connectivity

- Flyway: `classpath:db/migration` → `V1__researchflow_domain.sql`
- Hibernate `ddl-auto: validate`
- Startup log: `MariaDB connectivity verified (... catalog=researchflow)`

---

## 4. Database preparation

Do **not** recreate the database for a normal demo.

`DemoDataSeeder` is idempotent: creates seed users if missing; seeds five proposals only if the seed student has none; otherwise logs `Demo proposals already present`.

For the live show, create a **new** proposal titled `Live Demo: …`.

---

## 5. Application startup

### 5.1 Port 8080

```powershell
Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue |
  Select-Object LocalPort, State, OwningProcess
```

If occupied by a leftover ResearchFlow/Java process:

```powershell
Get-Process -Id <OwningProcess>
Stop-Process -Id <OwningProcess> -Force
```

### 5.2 Launch

```powershell
cd "D:\RIT\Third Year\OOAD\ResearchFlow"
& "E:\Maven\apache-maven-3.10.1\bin\mvn.cmd" spring-boot:run
```

### 5.3 Success signals

1. Flyway OK  
2. `Started ResearchFlowApplication`  
3. `Tomcat started on port 8080`  
4. `MariaDB connectivity verified (... catalog=researchflow)`  
5. `Demo users ready (usernames only): admin='…', student='…', faculty='…'`

### 5.4 URL

http://localhost:8080/login

### 5.5 Stop

**Ctrl+C** in the Maven PowerShell window.

---

## 6. Demo credentials and account preparation

| Role | Username env var | Password env var |
|------|------------------|------------------|
| Administrator | `ADMIN_USERNAME` | `ADMIN_PASSWORD` |
| Student | `SEED_STUDENT_USERNAME` | `SEED_STUDENT_PASSWORD` |
| Faculty | `SEED_FACULTY_USERNAME` | `SEED_FACULTY_PASSWORD` |

`.env.example` usernames: `admin`, `student1`, `faculty1`. Read passwords from your local `.env`.

Admin is **seed-only**. `/register` allows only Student/Faculty.

**Seeded proposals** (if first seed): Draft IoT; Submitted Feedback Portal; Under Review Adaptive Quiz; Revision Required Lab Booking; Approved Alumni Mentorship.

Always **Logout** before switching roles.

---

## 7. Complete end-to-end demo

Suggested title: `Live Demo: ResearchFlow Workflow Walkthrough`  
(Abstract ≥50 chars; other fields ≥20 chars.)

### Stage 1 — Registration (optional) + Student login

1. http://localhost:8080/register → Role Student → fill fields → **Register**  
   OR go straight to login with seed student.  
2. **Log in** → `/student/proposals`, header `username (STUDENT)`.

**Say:** Public registration is Student/Faculty only; Admin is seed-only. Creation uses Factory Method via `UserCreator` subclasses.

### Stage 2 — Create and submit

1. **New draft** → fill form → **Create draft** → status **DRAFT**  
2. **Submit** → **SUBMITTED**, revision **1**

**Flow:** `StudentController` → `ProposalWorkflowFacade` → `ResearchProposal.submit()`.

### Stage 3 — Admin assigns reviewer

1. Logout → Admin → `/admin/proposals` → Open Live Demo  
2. Select Faculty → **Assign reviewer** → **UNDER_REVIEW**

**Say:** Aggregate creates cycle-scoped `ReviewerAssignment`; event notifies Faculty after commit.

### Stage 4 — Faculty review (REVISION)

1. Logout → Faculty → Assignments → Open proposal  
2. Comments + recommendation **REVISION** → **Submit review**

**Say:** Review is 1:1 with `ReviewerAssignment`; status stays UNDER_REVIEW until Admin decides.

### Stage 5 — Admin requests revision

1. Logout → Admin → Open proposal  
2. Action **Request revision** (form value `REVISION`) + mandatory note → **Submit decision**  
3. Status **REVISION_REQUIRED** (domain type `REQUEST_REVISION`)

**Say:** Faculty recommendations are advisory; Admin records the institutional decision.

### Stage 6 — Student revises and resubmits

1. Logout → Student → Edit → **Save changes** → **Resubmit**  
2. **SUBMITTED**, revision **2**; prior assignment **inactive**; history retained

**Say:** Revision-cycle isolation—old cycle deactivated, history kept.

### Stage 7 — Reassign and final approve

1. Admin assigns Faculty again → UNDER_REVIEW (cycle 2)  
2. Faculty review **APPROVE**  
3. Admin **Approve** → **APPROVED**

### Stage 8 — Notifications + access control

1. `/notifications` → show events → **Mark as read** if unread  
2. As Student open `/admin/proposals` → dedicated **Access denied** page  

**Say:** Observer uses AFTER_COMMIT + REQUIRES_NEW; not `@Async`. Security uses role URL matchers + `accessDeniedPage`.

---

## 8. Stage cheat sheet

| Stage | Route/action | Result | DB | OOAD |
|-------|--------------|--------|----|------|
| Register | POST `/register` | Login flash | users + students/faculty | Factory Method |
| Draft | POST `/student/proposals` | DRAFT | research_proposals | Aggregate |
| Submit | POST `.../submit` | SUBMITTED rev1 | status + notifications | Encapsulated transition |
| Assign | POST `/admin/.../assign` | UNDER_REVIEW | reviewer_assignments | Cycle association |
| Review | POST `/faculty/.../reviews` | Review saved | reviews | Assignment owns Review |
| Decision | POST `/admin/.../decision` | REVISION_REQUIRED/APPROVED | decision_records | Admin authority |
| Resubmit | submit again | SUBMITTED rev2 | deactivate assignments | Cycle isolation |
| Notify | automatic | Inbox | notifications | Observer |
| Deny | GET `/admin/**` as Student | Access denied | none | RBAC |

---

## 9. Database inspection (safe SELECTs)

| Table | Meaning |
|-------|---------|
| `users` | JOINED root |
| `students` / `faculty` / `administrators` | Role tables |
| `research_proposals` | Aggregate |
| `reviewer_assignments` | Cycle assignments |
| `reviews` | One per assignment |
| `decision_records` | Admin decisions |
| `notifications` | Inbox (`read_flag`) |

```sql
SELECT id, title, status, revision_number FROM research_proposals ORDER BY id DESC LIMIT 10;
```

```sql
SELECT id, proposal_id, faculty_id, revision_number, active
FROM reviewer_assignments WHERE proposal_id = <ID> ORDER BY revision_number, id;
```

```sql
SELECT r.id, r.revision_number, r.recommendation
FROM reviews r JOIN reviewer_assignments a ON a.id = r.assignment_id
WHERE a.proposal_id = <ID> ORDER BY r.revision_number, r.id;
```

```sql
SELECT id, revision_number, decision_type, note FROM decision_records
WHERE proposal_id = <ID> ORDER BY id;
```

```sql
SELECT id, type, read_flag, message FROM notifications ORDER BY id DESC LIMIT 15;
```

Do not DROP/truncate during the demo.

---

## 10. Architecture explanation

```text
Browser + CSRF
  → Controller
  → Facade / RegistrationService (@Transactional)
  → Domain aggregate / UserCreator
  → Repository port → JPA adapter → MariaDB
  → ProposalWorkflowEvent
  → AFTER_COMMIT listener → REQUIRES_NEW notification write
```

```text
Presentation → Application → Domain ← Infrastructure
SecurityConfig cross-cutting (session, BCrypt, role matchers)
Flyway owns schema; Hibernate validates
```

---

## 11. Troubleshooting

| Symptom | Cause | Fix | Restart? |
|---------|-------|-----|----------|
| DB connection error | MariaDB down / bad `DB_*` | Start MariaDB; fix `.env` | Yes |
| Flyway/validate fail | Wrong/corrupt schema | Confirm DB name; avoid DROP | Yes after fix |
| Port 8080 busy | Old Java process | Stop your leftover PID | Then start |
| Login fail | Wrong password / seed skipped | Check env + seed log | Maybe |
| Seed skipped | Missing seed env vars | Fill `.env` | Yes |
| Messy old proposals | Prior seed data | Create new Live Demo draft | No |
| Unexpected Access denied | Wrong role session | Logout / login | No |

---

## 12. Viva Q&A (15)

1. **Aggregate root?** — Centralizes proposal invariants.  
2. **No `setStatus`?** — Only guarded domain methods change status.  
3. **JOINED inheritance?** — Shared user row + role-specific tables.  
4. **Factory Method?** — Shared register pipeline; polymorphic `createUser`.  
5. **Facade?** — One transactional workflow API for controllers.  
6. **Observer?** — Events after workflow actions drive notifications.  
7. **AFTER_COMMIT vs async?** — Transaction phase, not `@Async`.  
8. **REQUIRES_NEW?** — Separate TX for notification persistence.  
9. **Assignment cycle?** — `revision_number` + uniqueness constraint.  
10. **Review on assignment?** — One review per assignment FK.  
11. **Overrides?** — Admin decision can diverge; notes/rules apply.  
12. **Security?** — Session + role URL matchers + access-denied page.  
13. **Flyway?** — Versioned SQL owns schema.  
14. **Relationships?** — Author, assignments, reviews, decisions, notifications.  
15. **Limits?** — In-app notify only; README stale; future email/dashboards.

---

## 13. Final pre-demo checklist

- [ ] JDK 17 + Maven OK  
- [ ] MariaDB **Running**  
- [ ] `.env` with `DB_*` and seed vars  
- [ ] App logs show Flyway + Started + MariaDB verified + Demo users ready  
- [ ] `/login` works for admin/student/faculty  
- [ ] New **Live Demo** proposal planned  
- [ ] Know Admin “Request revision” form value is `REVISION`  
- [ ] Logout between roles  
- [ ] `DEMO_QUICK_REFERENCE.md` open  

---

## Start immediately

1. **First command**

```powershell
cd "D:\RIT\Third Year\OOAD\ResearchFlow"
& "E:\Maven\apache-maven-3.10.1\bin\mvn.cmd" spring-boot:run
```

2. **URL:** http://localhost:8080/login  

3. **First account:** `SEED_STUDENT_USERNAME` (typically `student1`; password from `SEED_STUDENT_PASSWORD`)  

4. **First action:** **New draft** → `Live Demo: ResearchFlow Workflow Walkthrough` → **Submit**.
