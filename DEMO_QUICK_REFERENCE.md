# ResearchFlow — Demo Quick Reference

Keep this open during the live demo.

## Startup

```powershell
cd "D:\RIT\Third Year\OOAD\ResearchFlow"
Get-Service -Name "MariaDB*"
# If Stopped: Start-Service -Name "<exact MariaDB service name>"
& "E:\Maven\apache-maven-3.10.1\bin\mvn.cmd" spring-boot:run
```

Stop: `Ctrl+C` in that PowerShell window.

## URL

http://localhost:8080

After login, `/home` redirects by role:
- Student → `/student/proposals`
- Faculty → `/faculty/assignments`
- Administrator → `/admin/proposals`

## Accounts (passwords are in your local `.env` only)

| Role | Username env var | Password env var |
|------|------------------|------------------|
| Administrator | `ADMIN_USERNAME` (example default `admin`) | `ADMIN_PASSWORD` |
| Student | `SEED_STUDENT_USERNAME` (example default `student1`) | `SEED_STUDENT_PASSWORD` |
| Faculty | `SEED_FACULTY_USERNAME` (example default `faculty1`) | `SEED_FACULTY_PASSWORD` |

Read the values from `.env` on your machine. Do not invent passwords.  
Admin is seed-only (not public registration).

Logout: header **Logout** button (POST `/logout`).

## Recommended live-demo login sequence

1. **Student** (`student1`) — create + submit a new proposal titled `Live Demo: …`
2. **Admin** (`admin`) — Assign reviewer → status `UNDER_REVIEW`
3. **Faculty** (`faculty1`) — Submit review → recommendation `REVISION`
4. **Admin** — Action **Request revision** (`REVISION`) + mandatory note → `REVISION_REQUIRED`
5. **Student** — Edit → **Resubmit** → `SUBMITTED` revision **2**
6. **Admin** — Assign reviewer again for cycle 2
7. **Faculty** — New review → `APPROVE`
8. **Admin** — **Approve** → `APPROVED`
9. Any role → `/notifications` ; as Student open `/admin/proposals` → Access denied

## Status transitions you must show

`DRAFT → SUBMITTED → UNDER_REVIEW → REVISION_REQUIRED → SUBMITTED (rev 2) → UNDER_REVIEW → APPROVED`

## Important routes

| Route | Who |
|-------|-----|
| `/register` | Public (Student/Faculty only) |
| `/login` | Public |
| `/student/proposals` | Student dashboard |
| `/student/proposals/new` | New draft |
| `/admin/proposals` | Admin dashboard |
| `/faculty/assignments` | Faculty dashboard |
| `/notifications` | Inbox |
| `/access-denied` | Dedicated denial page |

## Final 60-second checklist

- [ ] MariaDB service Running  
- [ ] `.env` present with `DB_*` and seed vars  
- [ ] `mvn spring-boot:run` shows Flyway + “Started ResearchFlowApplication” + MariaDB verified  
- [ ] http://localhost:8080/login opens  
- [ ] Can log in as `admin` / `student1` / `faculty1`  
- [ ] Know which proposal title is your **Live Demo** proposal  
- [ ] Logout between every role switch  
