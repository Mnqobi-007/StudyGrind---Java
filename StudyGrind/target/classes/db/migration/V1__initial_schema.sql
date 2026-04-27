-- V1__initial_schema.sql
-- Initial database schema

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    failed_attempts INTEGER DEFAULT 0,
    account_locked_until TIMESTAMP,
    last_login_ip VARCHAR(255),
    last_login_at TIMESTAMP,
    password_changed_at TIMESTAMP,
    student_number VARCHAR(255) UNIQUE,
    phone_number VARCHAR(255),
    address VARCHAR(255),
    date_of_birth TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP,
    profile_completed BOOLEAN DEFAULT FALSE
);

-- Courses table
CREATE TABLE IF NOT EXISTS courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    teacher_id BIGINT NOT NULL,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    created_at TIMESTAMP,
    price DOUBLE DEFAULT 29.99,
    FOREIGN KEY (teacher_id) REFERENCES users(id)
);

-- Course enrollments table (FIXED - removed UNIQUE KEY syntax)
CREATE TABLE IF NOT EXISTS course_enrollments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    enrollment_date TIMESTAMP,
    trial_start_date TIMESTAMP,
    trial_end_date TIMESTAMP,
    trial_used BOOLEAN DEFAULT FALSE,
    subscription_start_date TIMESTAMP,
    subscription_end_date TIMESTAMP,
    subscription_active BOOLEAN DEFAULT FALSE,
    outstanding_balance DOUBLE DEFAULT 0.0,
    last_payment_date TIMESTAMP,
    payment_status VARCHAR(255) DEFAULT 'pending',
    is_enrolled BOOLEAN DEFAULT TRUE,
    unenrolled_date TIMESTAMP,
    unenrollment_reason VARCHAR(255),
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (course_id) REFERENCES courses(id)
);

-- Add unique constraint separately
ALTER TABLE course_enrollments ADD CONSTRAINT uk_student_course UNIQUE (student_id, course_id);

-- Assignments table
CREATE TABLE IF NOT EXISTS assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    subject VARCHAR(255),
    due_date TIMESTAMP NOT NULL,
    max_score INTEGER DEFAULT 100,
    teacher_id BIGINT NOT NULL,
    course_id BIGINT,
    created_at TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES users(id),
    FOREIGN KEY (course_id) REFERENCES courses(id)
);

-- Submissions table (FIXED - removed UNIQUE KEY syntax)
CREATE TABLE IF NOT EXISTS submissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    assignment_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    file_path VARCHAR(255),
    content TEXT,
    score DOUBLE,
    feedback TEXT,
    submitted_at TIMESTAMP,
    graded_at TIMESTAMP,
    FOREIGN KEY (assignment_id) REFERENCES assignments(id),
    FOREIGN KEY (student_id) REFERENCES users(id)
);

-- Add unique constraint separately
ALTER TABLE submissions ADD CONSTRAINT uk_assignment_student UNIQUE (assignment_id, student_id);

-- Quizzes table
CREATE TABLE IF NOT EXISTS quizzes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    subject VARCHAR(255),
    time_limit INTEGER,
    teacher_id BIGINT NOT NULL,
    created_at TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES users(id)
);

-- Questions table
CREATE TABLE IF NOT EXISTS questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quiz_id BIGINT NOT NULL,
    text TEXT NOT NULL,
    option_a VARCHAR(255) NOT NULL,
    option_b VARCHAR(255) NOT NULL,
    option_c VARCHAR(255),
    option_d VARCHAR(255),
    correct_answer VARCHAR(255) NOT NULL,
    points INTEGER DEFAULT 1,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id)
);

-- Quiz attempts table
CREATE TABLE IF NOT EXISTS quiz_attempts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quiz_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    score DOUBLE,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id),
    FOREIGN KEY (student_id) REFERENCES users(id)
);

-- Quiz answers table
CREATE TABLE IF NOT EXISTS quiz_answers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    attempt_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    selected_answer VARCHAR(255) NOT NULL,
    is_correct BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (attempt_id) REFERENCES quiz_attempts(id),
    FOREIGN KEY (question_id) REFERENCES questions(id)
);

-- Notes table
CREATE TABLE IF NOT EXISTS notes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    subject VARCHAR(255),
    file_path VARCHAR(255),
    teacher_id BIGINT NOT NULL,
    course_id BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES users(id),
    FOREIGN KEY (course_id) REFERENCES courses(id)
);

-- Textbooks table
CREATE TABLE IF NOT EXISTS textbooks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    file_path VARCHAR(255) NOT NULL,
    file_name VARCHAR(255),
    file_size BIGINT,
    uploaded_by BIGINT NOT NULL,
    course_id BIGINT,
    upload_date TIMESTAMP,
    FOREIGN KEY (uploaded_by) REFERENCES users(id),
    FOREIGN KEY (course_id) REFERENCES courses(id)
);

-- Timetables table
CREATE TABLE IF NOT EXISTS timetables (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    day_of_week VARCHAR(255) NOT NULL,
    start_time VARCHAR(255) NOT NULL,
    end_time VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    notes TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES users(id)
);

-- Subscriptions table
CREATE TABLE IF NOT EXISTS subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    status VARCHAR(255) NOT NULL,
    amount DOUBLE,
    payment_date TIMESTAMP,
    payment_method VARCHAR(255),
    stripe_payment_id VARCHAR(255),
    FOREIGN KEY (student_id) REFERENCES users(id)
);

-- Trial periods table
CREATE TABLE IF NOT EXISTS trial_periods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    is_active BOOLEAN,
    trial_days INTEGER DEFAULT 14,
    FOREIGN KEY (student_id) REFERENCES users(id)
);

-- Course payments table
CREATE TABLE IF NOT EXISTS course_payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    enrollment_id BIGINT NOT NULL,
    amount DOUBLE NOT NULL,
    payment_date TIMESTAMP,
    payment_method VARCHAR(255),
    payment_id VARCHAR(255),
    billing_cycle_start TIMESTAMP,
    billing_cycle_end TIMESTAMP,
    status VARCHAR(255) DEFAULT 'completed',
    FOREIGN KEY (enrollment_id) REFERENCES course_enrollments(id)
);