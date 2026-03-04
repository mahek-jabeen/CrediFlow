-- Drop existing tables safely
DROP TABLE IF EXISTS emi_schedules;
DROP TABLE IF EXISTS loans;
DROP TABLE IF EXISTS loan_applications;
DROP TABLE IF EXISTS users;

-- Create users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) UNIQUE,
    date_of_birth DATE,
    pan_number VARCHAR(10),
    aadhar_number VARCHAR(12),
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    pincode VARCHAR(10),
    employment_type ENUM('SALARIED', 'SELF_EMPLOYED', 'BUSINESS', 'PROFESSIONAL', 'UNEMPLOYED'),
    monthly_income DOUBLE,
    employer VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER'
);

-- Create loan_applications table
CREATE TABLE loan_applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_number VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    loan_type ENUM('HOME_LOAN', 'PERSONAL_LOAN', 'CAR_LOAN', 'EDUCATION_LOAN', 'BUSINESS_LOAN', 'GOLD_LOAN') NOT NULL,
    requested_amount DOUBLE NOT NULL,
    requested_tenure_months INT NOT NULL,
    proposed_interest_rate DOUBLE,
    status ENUM('PENDING', 'UNDER_REVIEW', 'APPROVED', 'REJECTED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    remarks TEXT,
    credit_score INT,
    existing_emi_amount DOUBLE,
    application_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_date TIMESTAMP NULL,
    reviewed_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create loans table
CREATE TABLE loans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    loan_number VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    loan_application_id BIGINT NOT NULL,
    principal_amount DOUBLE NOT NULL,
    interest_rate DOUBLE NOT NULL,
    tenure_months INT NOT NULL,
    emi_amount DOUBLE NOT NULL,
    total_payable_amount DOUBLE NOT NULL,
    total_interest DOUBLE NOT NULL,
    outstanding_amount DOUBLE NOT NULL,
    disbursement_date DATE NOT NULL,
    first_emi_date DATE NOT NULL,
    last_emi_date DATE NULL,
    status ENUM('ACTIVE', 'CLOSED', 'DEFAULTED', 'FORECLOSED') NOT NULL,
    paid_emis INT DEFAULT 0,
    pending_emis INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (loan_application_id) REFERENCES loan_applications(id) ON DELETE CASCADE
);

-- Create emi_schedules table
CREATE TABLE emi_schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    loan_id BIGINT NOT NULL,
    emi_number INT NOT NULL,
    installment_number INT NOT NULL,
    emi_amount DOUBLE NOT NULL,
    interest_component DOUBLE NOT NULL,
    principal_component DOUBLE NOT NULL,
    remaining_principal DOUBLE NOT NULL,
    payment_status ENUM('PENDING', 'PAID', 'OVERDUE', 'PARTIAL_PAID', 'WAIVED') NOT NULL DEFAULT 'PENDING',
    payment_date DATE NULL,
    amount_paid DOUBLE DEFAULT 0,
    late_fee DOUBLE DEFAULT 0,
    payment_reference VARCHAR(255),
    payment_method ENUM('CASH', 'CHEQUE', 'ONLINE_TRANSFER', 'UPI', 'AUTO_DEBIT', 'NET_BANKING'),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (loan_id) REFERENCES loans(id) ON DELETE CASCADE
);
