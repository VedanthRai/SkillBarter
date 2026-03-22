-- ================================================================
-- SkillBarter Database Schema
-- UE23CS352B OOAD Project
-- Run this ONLY for a fresh setup; spring.jpa.ddl-auto=update
-- handles schema evolution automatically.
-- ================================================================

CREATE DATABASE IF NOT EXISTS skillbarter_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE skillbarter_db;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    username                VARCHAR(50)  NOT NULL UNIQUE,
    email                   VARCHAR(255) NOT NULL UNIQUE,
    password_hash           VARCHAR(255) NOT NULL,
    bio                     VARCHAR(500),
    profile_picture_url     VARCHAR(500),
    credit_balance          DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    escrow_balance          DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    streak_count            INT NOT NULL DEFAULT 0,
    total_sessions_completed INT NOT NULL DEFAULT 0,
    reputation_score        INT NOT NULL DEFAULT 0,
    last_session_date       DATETIME,
    email_verified          TINYINT(1) NOT NULL DEFAULT 0,
    email_verification_token VARCHAR(255),
    status                  ENUM('PENDING_VERIFICATION','ACTIVE','SUSPENDED','BANNED') NOT NULL DEFAULT 'ACTIVE',
    role                    ENUM('ROLE_USER','ROLE_VERIFIER','ROLE_ADMIN') NOT NULL DEFAULT 'ROLE_USER',
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email  (email),
    INDEX idx_status (status),
    INDEX idx_role   (role)
);

-- Skills table
CREATE TABLE IF NOT EXISTS skills (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    name              VARCHAR(100) NOT NULL,
    description       TEXT,
    category          ENUM('TECHNOLOGY','LANGUAGES','MUSIC','ARTS_AND_CRAFTS',
                           'ACADEMICS','FITNESS','COOKING','BUSINESS',
                           'PERSONAL_DEVELOPMENT','OTHER') NOT NULL,
    hourly_rate       DECIMAL(5,2) NOT NULL DEFAULT 1.00,
    proficiency_level VARCHAR(50),
    certificate_path  VARCHAR(500),
    verified          TINYINT(1) NOT NULL DEFAULT 0,
    is_offering       TINYINT(1) NOT NULL DEFAULT 1,
    average_rating    DOUBLE NOT NULL DEFAULT 0.0,
    total_ratings     INT NOT NULL DEFAULT 0,
    total_sessions    INT NOT NULL DEFAULT 0,
    user_id           BIGINT NOT NULL,
    created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_category   (category),
    INDEX idx_is_offering(is_offering),
    INDEX idx_verified   (verified),
    FULLTEXT idx_search  (name, description)
);

-- Sessions table
CREATE TABLE IF NOT EXISTS sessions (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    learner_id        BIGINT NOT NULL,
    teacher_id        BIGINT NOT NULL,
    skill_id          BIGINT NOT NULL,
    scheduled_at      DATETIME NOT NULL,
    duration_minutes  INT NOT NULL DEFAULT 60,
    status            ENUM('REQUESTED','ACCEPTED','IN_PROGRESS','COMPLETED',
                           'CANCELLED','DISPUTED') NOT NULL DEFAULT 'REQUESTED',
    credit_amount     DECIMAL(10,2) NOT NULL,
    learner_notes     VARCHAR(500),
    teacher_notes     VARCHAR(500),
    learner_confirmed TINYINT(1) NOT NULL DEFAULT 0,
    teacher_confirmed TINYINT(1) NOT NULL DEFAULT 0,
    receipt_path      VARCHAR(500),
    started_at        DATETIME,
    completed_at      DATETIME,
    created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (learner_id) REFERENCES users(id),
    FOREIGN KEY (teacher_id) REFERENCES users(id),
    FOREIGN KEY (skill_id)   REFERENCES skills(id),
    INDEX idx_learner_id (learner_id),
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_status     (status)
);

-- Transactions table
CREATE TABLE IF NOT EXISTS transactions (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    payer_id    BIGINT NOT NULL,
    payee_id    BIGINT NOT NULL,
    session_id  BIGINT NOT NULL UNIQUE,
    amount      DECIMAL(10,2) NOT NULL,
    status      ENUM('PENDING','ESCROWED','RELEASED','REFUNDED','DISPUTED')
                NOT NULL DEFAULT 'PENDING',
    note        VARCHAR(500),
    escrowed_at DATETIME,
    resolved_at DATETIME,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (payer_id)   REFERENCES users(id),
    FOREIGN KEY (payee_id)   REFERENCES users(id),
    FOREIGN KEY (session_id) REFERENCES sessions(id),
    INDEX idx_payer  (payer_id),
    INDEX idx_payee  (payee_id),
    INDEX idx_status (status)
);

-- Reviews table
CREATE TABLE IF NOT EXISTS reviews (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id  BIGINT NOT NULL UNIQUE,
    reviewer_id BIGINT NOT NULL,
    reviewed_id BIGINT NOT NULL,
    rating      INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment     TEXT,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id)  REFERENCES sessions(id),
    FOREIGN KEY (reviewer_id) REFERENCES users(id),
    FOREIGN KEY (reviewed_id) REFERENCES users(id)
);

-- Disputes table
CREATE TABLE IF NOT EXISTS disputes (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id            BIGINT NOT NULL UNIQUE,
    raised_by_id          BIGINT NOT NULL,
    assigned_verifier_id  BIGINT,
    description           TEXT NOT NULL,
    evidence              TEXT,
    status                ENUM('OPEN','UNDER_REVIEW','RESOLVED_TEACHER',
                               'RESOLVED_LEARNER','CLOSED') NOT NULL DEFAULT 'OPEN',
    resolution            TEXT,
    assigned_at           DATETIME,
    resolved_at           DATETIME,
    created_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            DATETIME ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id)           REFERENCES sessions(id),
    FOREIGN KEY (raised_by_id)         REFERENCES users(id),
    FOREIGN KEY (assigned_verifier_id) REFERENCES users(id)
);

-- Badges table
CREATE TABLE IF NOT EXISTS badges (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    skill_id    BIGINT,
    badge_name  VARCHAR(200) NOT NULL,
    badge_type  VARCHAR(100) NOT NULL,
    icon_url    VARCHAR(500),
    description VARCHAR(500),
    awarded_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id)  REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE SET NULL
);

-- Notifications table
CREATE TABLE IF NOT EXISTS notifications (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient_id BIGINT NOT NULL,
    type         VARCHAR(50) NOT NULL,
    message      VARCHAR(500) NOT NULL,
    action_url   VARCHAR(500),
    is_read      TINYINT(1) NOT NULL DEFAULT 0,
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_recipient_unread (recipient_id, is_read)
);

-- ── Seed Data ────────────────────────────────────────────────────────
-- Admin user (password: Admin@1234)
INSERT INTO users (username, email, password_hash, credit_balance, email_verified, status, role)
VALUES ('admin', 'admin@skillbarter.app',
        '$2a$12$2eS3gMNhRsLRZdSJ2EBG4.5VQ3qXlT6t2zSwMLf3..fK8U9aCKn4i',
        100.00, 1, 'ACTIVE', 'ROLE_ADMIN');

-- Demo verifier (password: Verify@1234)
INSERT INTO users (username, email, password_hash, credit_balance, email_verified, status, role)
VALUES ('verifier1', 'verifier@skillbarter.app',
        '$2a$12$2eS3gMNhRsLRZdSJ2EBG4.5VQ3qXlT6t2zSwMLf3..fK8U9aCKn4i',
        50.00, 1, 'ACTIVE', 'ROLE_VERIFIER');

-- Demo users (password: Test@1234)
INSERT INTO users (username, email, password_hash, credit_balance, streak_count,
                   total_sessions_completed, reputation_score, email_verified, status, role)
VALUES
  ('alice_dev',   'alice@example.com',   '$2a$12$xyz', 15.00, 7,  12, 45, 1, 'ACTIVE', 'ROLE_USER'),
  ('bob_music',   'bob@example.com',     '$2a$12$xyz', 8.50,  3,   5, 20, 1, 'ACTIVE', 'ROLE_USER'),
  ('charlie_lang','charlie@example.com', '$2a$12$xyz', 20.00, 14, 22, 80, 1, 'ACTIVE', 'ROLE_USER');

-- Demo skills
INSERT INTO skills (name, description, category, hourly_rate, proficiency_level, verified, is_offering, user_id)
VALUES
  ('Python Programming', 'Teach Python from basics to OOP and data structures.', 'TECHNOLOGY', 2.00, 'ADVANCED',  1, 1, 3),
  ('Classical Guitar',   'Learn acoustic guitar — scales, chords, classical pieces.', 'MUSIC', 1.50, 'INTERMEDIATE', 0, 1, 4),
  ('Spanish Conversation','Conversational Spanish for travel and business.', 'LANGUAGES', 1.00, 'ADVANCED', 1, 1, 5),
  ('Java Spring Boot',   'Build REST APIs with Spring Boot 3.', 'TECHNOLOGY', 2.50, 'EXPERT', 1, 1, 3);
