-- Run once as a MariaDB administrative account (not the application runtime user).
-- The application user 'researchflow'@'localhost' cannot create databases by default.

CREATE DATABASE IF NOT EXISTS researchflow_test
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

GRANT ALL PRIVILEGES ON researchflow_test.* TO 'researchflow'@'localhost';
FLUSH PRIVILEGES;
