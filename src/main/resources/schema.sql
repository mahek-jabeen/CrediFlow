-- =====================================================
-- DATABASE
-- =====================================================
DROP DATABASE IF EXISTS loan_db;
CREATE DATABASE loan_db;
USE loan_db;
SHOW TABLES;
SELECT * FROM loan_applications;
SELECT * FROM loans;
SELECT * FROM users;

-- =====================================================
-- USERS TABLE
-- =====================================================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- LOAN APPLICATIONS TABLE (SOURCE OF TRUTH)
-- =====================================================
CREATE TABLE loan_applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_email VARCHAR(255) NOT NULL,
    amount DOUBLE NOT NULL,
    tenure INT NOT NULL,
    interest_rate DOUBLE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_loan_app_user
        FOREIGN KEY (user_email)
        REFERENCES users(email)
        ON DELETE CASCADE
);
ALTER TABLE emi_schedules
ADD COLUMN emi_number INT NOT NULL;
DESCRIBE emi_schedules;

-- =====================================================
-- LOANS TABLE (CREATED ONLY AFTER APPROVAL)
-- =====================================================
CREATE TABLE loans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    loan_application_id BIGINT NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    approved_amount DOUBLE NOT NULL,
    tenure INT NOT NULL,
    interest_rate DOUBLE NOT NULL,
    emi DOUBLE NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_loans_application
        FOREIGN KEY (loan_application_id)
        REFERENCES loan_applications(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_loans_user
        FOREIGN KEY (user_email)
        REFERENCES users(email)
        ON DELETE CASCADE
);

CREATE TABLE emi_schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    loan_id BIGINT NOT NULL,
    installment_number INT NOT NULL,
    emi_amount DOUBLE NOT NULL,
    due_date DATE NOT NULL,
    paid BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_emi_loan
        FOREIGN KEY (loan_id)
        REFERENCES loans(id)
        ON DELETE CASCADE
);

-- =====================================================
-- INDEXES (PERFORMANCE)
-- =====================================================
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_loan_app_user_email ON loan_applications(user_email);
CREATE INDEX idx_loans_user_email ON loans(user_email);

-- =====================================================
-- OPTIONAL: TEST DATA (SAFE TO REMOVE)
-- =====================================================
-- INSERT INTO users (email, password, role)
-- VALUES ('admin@gmail.com', '$2a$10$hashedpassword', 'ADMIN');

-- INSERT INTO users (email, password, role)
-- VALUES ('user@gmail.com', '$2a$10$hashedpassword', 'USER');
