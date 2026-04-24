-- ============================================================
-- V1: Initial Schema
-- ============================================================

-- School settings (single row)
CREATE TABLE school_settings (
    id          BIGSERIAL PRIMARY KEY,
    school_name VARCHAR(255) NOT NULL DEFAULT 'My School',
    address     TEXT,
    phone       VARCHAR(20),
    email       VARCHAR(255),
    logo_url    TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO school_settings (school_name) VALUES ('My School');

-- Academic years
CREATE TABLE academic_years (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(20) NOT NULL,   -- e.g. 2024-2025
    start_date DATE NOT NULL,
    end_date   DATE NOT NULL,
    is_current BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Grades (Grade 1, Grade 2, ...)
CREATE TABLE grades (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(50) NOT NULL UNIQUE,   -- e.g. Grade 1
    order_num  INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Sections (Grade 1-A, Grade 1-B, ...)
CREATE TABLE sections (
    id         BIGSERIAL PRIMARY KEY,
    grade_id   BIGINT NOT NULL REFERENCES grades(id),
    name       VARCHAR(10) NOT NULL,           -- e.g. A, B, C
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (grade_id, name)
);

-- User profiles (linked to Supabase auth.users)
CREATE TABLE profiles (
    id         UUID PRIMARY KEY,               -- same as auth.users.id
    full_name  VARCHAR(255) NOT NULL,
    role       VARCHAR(30) NOT NULL,           -- ADMIN, OFFICE_EMPLOYEE, TEACHER
    phone      VARCHAR(20),
    is_active  BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Teacher-Section mapping per academic year
CREATE TABLE teacher_sections (
    id               BIGSERIAL PRIMARY KEY,
    teacher_id       UUID NOT NULL REFERENCES profiles(id),
    section_id       BIGINT NOT NULL REFERENCES sections(id),
    academic_year_id BIGINT NOT NULL REFERENCES academic_years(id),
    is_class_teacher BOOLEAN NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (teacher_id, section_id, academic_year_id)
);

-- Students
CREATE TABLE students (
    id               BIGSERIAL PRIMARY KEY,
    admission_number VARCHAR(50) NOT NULL UNIQUE,
    full_name        VARCHAR(255) NOT NULL,
    date_of_birth    DATE,
    gender           VARCHAR(10),
    address          TEXT,
    photo_url        TEXT,
    -- Parent/guardian info
    parent_name      VARCHAR(255),
    parent_phone     VARCHAR(20),
    parent_email     VARCHAR(255),
    -- Academic placement
    section_id       BIGINT REFERENCES sections(id),
    academic_year_id BIGINT REFERENCES academic_years(id),
    enrollment_date  DATE NOT NULL DEFAULT CURRENT_DATE,
    is_active        BOOLEAN NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Student documents
CREATE TABLE student_documents (
    id          BIGSERIAL PRIMARY KEY,
    student_id  BIGINT NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    name        VARCHAR(255) NOT NULL,
    url         TEXT NOT NULL,
    uploaded_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Employees
CREATE TABLE employees (
    id           BIGSERIAL PRIMARY KEY,
    profile_id   UUID REFERENCES profiles(id),   -- null if no system login
    employee_code VARCHAR(50) NOT NULL UNIQUE,
    full_name    VARCHAR(255) NOT NULL,
    designation  VARCHAR(100),
    department   VARCHAR(100),
    phone        VARCHAR(20),
    email        VARCHAR(255),
    date_of_birth DATE,
    gender       VARCHAR(10),
    address      TEXT,
    join_date    DATE NOT NULL DEFAULT CURRENT_DATE,
    monthly_salary NUMERIC(12, 2) NOT NULL DEFAULT 0,
    is_active    BOOLEAN NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Student attendance
CREATE TABLE student_attendance (
    id               BIGSERIAL PRIMARY KEY,
    student_id       BIGINT NOT NULL REFERENCES students(id),
    academic_year_id BIGINT NOT NULL REFERENCES academic_years(id),
    date             DATE NOT NULL,
    status           VARCHAR(20) NOT NULL,    -- PRESENT, ABSENT, LATE, HALF_DAY
    remarks          TEXT,
    recorded_by      UUID REFERENCES profiles(id),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (student_id, date)
);

-- Employee attendance
CREATE TABLE employee_attendance (
    id          BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    date        DATE NOT NULL,
    status      VARCHAR(20) NOT NULL,
    check_in    TIME,
    check_out   TIME,
    remarks     TEXT,
    recorded_by UUID REFERENCES profiles(id),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (employee_id, date)
);

-- Leave requests
CREATE TABLE leave_requests (
    id          BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    leave_type  VARCHAR(20) NOT NULL,   -- SICK, CASUAL, EARNED
    start_date  DATE NOT NULL,
    end_date    DATE NOT NULL,
    reason      TEXT,
    status      VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    approved_by UUID REFERENCES profiles(id),
    approved_at TIMESTAMPTZ,
    remarks     TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Grade / marks records
CREATE TABLE grade_records (
    id               BIGSERIAL PRIMARY KEY,
    student_id       BIGINT NOT NULL REFERENCES students(id),
    academic_year_id BIGINT NOT NULL REFERENCES academic_years(id),
    subject          VARCHAR(100) NOT NULL,
    term             VARCHAR(50) NOT NULL,
    letter_grade     VARCHAR(5),    -- A+, A, B+, B, C, D, F
    marks            NUMERIC(5, 2),
    remarks          TEXT,
    recorded_by      UUID REFERENCES profiles(id),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (student_id, academic_year_id, subject, term)
);

-- Fee structures (per grade per fee type per academic year)
CREATE TABLE fee_structures (
    id               BIGSERIAL PRIMARY KEY,
    academic_year_id BIGINT NOT NULL REFERENCES academic_years(id),
    grade_id         BIGINT NOT NULL REFERENCES grades(id),
    fee_type         VARCHAR(20) NOT NULL,   -- TUITION, TRANSPORT, LIBRARY, EXAM
    total_amount     NUMERIC(12, 2) NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (academic_year_id, grade_id, fee_type)
);

-- Installment schedule for each fee structure
CREATE TABLE fee_installments (
    id                BIGSERIAL PRIMARY KEY,
    fee_structure_id  BIGINT NOT NULL REFERENCES fee_structures(id) ON DELETE CASCADE,
    installment_number INT NOT NULL,
    due_date          DATE NOT NULL,
    amount            NUMERIC(12, 2) NOT NULL,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (fee_structure_id, installment_number)
);

-- Late fee configuration (single row)
CREATE TABLE late_fee_config (
    id                BIGSERIAL PRIMARY KEY,
    amount_per_day    NUMERIC(10, 2) NOT NULL DEFAULT 0,
    grace_period_days INT NOT NULL DEFAULT 0,
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO late_fee_config (amount_per_day, grace_period_days) VALUES (0, 0);

-- Student fee records (one per student per installment)
CREATE TABLE student_fees (
    id                   BIGSERIAL PRIMARY KEY,
    student_id           BIGINT NOT NULL REFERENCES students(id),
    fee_installment_id   BIGINT NOT NULL REFERENCES fee_installments(id),
    amount_due           NUMERIC(12, 2) NOT NULL,
    amount_paid          NUMERIC(12, 2) NOT NULL DEFAULT 0,
    late_fee             NUMERIC(12, 2) NOT NULL DEFAULT 0,
    status               VARCHAR(30) NOT NULL DEFAULT 'PENDING',  -- PENDING, PARTIAL, PAID
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (student_id, fee_installment_id)
);

-- Fee payment records (office records, admin approves)
CREATE TABLE fee_payments (
    id              BIGSERIAL PRIMARY KEY,
    student_fee_id  BIGINT NOT NULL REFERENCES student_fees(id),
    amount          NUMERIC(12, 2) NOT NULL,
    payment_date    DATE NOT NULL DEFAULT CURRENT_DATE,
    receipt_number  VARCHAR(50) UNIQUE,
    collected_by    UUID NOT NULL REFERENCES profiles(id),
    status          VARCHAR(30) NOT NULL DEFAULT 'PENDING_APPROVAL',
    approved_by     UUID REFERENCES profiles(id),
    approved_at     TIMESTAMPTZ,
    rejection_reason TEXT,
    notes           TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Indexes for performance
CREATE INDEX idx_students_section ON students(section_id, academic_year_id);
CREATE INDEX idx_student_attendance_date ON student_attendance(student_id, date);
CREATE INDEX idx_employee_attendance_date ON employee_attendance(employee_id, date);
CREATE INDEX idx_fee_payments_status ON fee_payments(status);
CREATE INDEX idx_student_fees_student ON student_fees(student_id);
