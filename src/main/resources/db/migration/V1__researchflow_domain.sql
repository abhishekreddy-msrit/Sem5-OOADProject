-- ResearchFlow domain schema (MariaDB)
-- Owned by Flyway. Hibernate uses ddl-auto=validate.

CREATE TABLE users (
    id              BIGINT NOT NULL AUTO_INCREMENT,
    username        VARCHAR(100) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(200) NOT NULL,
    email           VARCHAR(255) NOT NULL,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_users_username UNIQUE (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE students (
    user_id             BIGINT NOT NULL,
    department          VARCHAR(200) NOT NULL,
    student_identifier  VARCHAR(100) NOT NULL,
    PRIMARY KEY (user_id),
    CONSTRAINT fk_students_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE faculty (
    user_id     BIGINT NOT NULL,
    department  VARCHAR(200) NOT NULL,
    designation VARCHAR(200) NOT NULL,
    PRIMARY KEY (user_id),
    CONSTRAINT fk_faculty_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE administrators (
    user_id      BIGINT NOT NULL,
    office_label VARCHAR(200) NULL,
    PRIMARY KEY (user_id),
    CONSTRAINT fk_administrators_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE research_proposals (
    id                  BIGINT NOT NULL AUTO_INCREMENT,
    author_id           BIGINT NOT NULL,
    title               VARCHAR(200) NOT NULL,
    abstract_text       VARCHAR(4000) NOT NULL,
    objectives          VARCHAR(4000) NOT NULL,
    methodology         VARCHAR(4000) NOT NULL,
    expected_outcomes   VARCHAR(4000) NOT NULL,
    status              VARCHAR(40) NOT NULL,
    revision_number     INT NOT NULL,
    created_at          TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    submitted_at        TIMESTAMP(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_proposals_author FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE reviewer_assignments (
    id               BIGINT NOT NULL AUTO_INCREMENT,
    proposal_id      BIGINT NOT NULL,
    faculty_id       BIGINT NOT NULL,
    assigned_by_id   BIGINT NOT NULL,
    revision_number  INT NOT NULL,
    assigned_at      TIMESTAMP(6) NOT NULL,
    active           BOOLEAN NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_assignment_proposal_faculty_revision
        UNIQUE (proposal_id, faculty_id, revision_number),
    CONSTRAINT fk_assignments_proposal FOREIGN KEY (proposal_id) REFERENCES research_proposals (id) ON DELETE RESTRICT,
    CONSTRAINT fk_assignments_faculty FOREIGN KEY (faculty_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_assignments_assigned_by FOREIGN KEY (assigned_by_id) REFERENCES users (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE reviews (
    id               BIGINT NOT NULL AUTO_INCREMENT,
    assignment_id    BIGINT NOT NULL,
    revision_number  INT NOT NULL,
    comments         VARCHAR(4000) NOT NULL,
    recommendation   VARCHAR(40) NOT NULL,
    submitted_at     TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_reviews_assignment UNIQUE (assignment_id),
    CONSTRAINT fk_reviews_assignment FOREIGN KEY (assignment_id) REFERENCES reviewer_assignments (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE decision_records (
    id                          BIGINT NOT NULL AUTO_INCREMENT,
    proposal_id                 BIGINT NOT NULL,
    decided_by_id               BIGINT NOT NULL,
    revision_number             INT NOT NULL,
    decision_type               VARCHAR(40) NOT NULL,
    note                        VARCHAR(2000) NULL,
    override_of_recommendations BOOLEAN NOT NULL,
    decided_at                  TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_decisions_proposal FOREIGN KEY (proposal_id) REFERENCES research_proposals (id) ON DELETE RESTRICT,
    CONSTRAINT fk_decisions_decided_by FOREIGN KEY (decided_by_id) REFERENCES users (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE notifications (
    id            BIGINT NOT NULL AUTO_INCREMENT,
    recipient_id  BIGINT NOT NULL,
    proposal_id   BIGINT NULL,
    type          VARCHAR(40) NOT NULL,
    message       VARCHAR(1000) NOT NULL,
    read_flag     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_notifications_recipient FOREIGN KEY (recipient_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_notifications_proposal FOREIGN KEY (proposal_id) REFERENCES research_proposals (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_proposals_author ON research_proposals (author_id);
CREATE INDEX idx_assignments_faculty ON reviewer_assignments (faculty_id);
CREATE INDEX idx_notifications_recipient ON notifications (recipient_id);
