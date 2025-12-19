-- AI 기능 및 주간 통계 의존 테이블 생성 스크립트
-- 실행 순서: init.sql -> 기존 리팩토링 SQL 이후 본 파일 실행

CREATE TABLE IF NOT EXISTS ai_daily_meal_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    plan_date DATE NOT NULL,
    model VARCHAR(100) DEFAULT NULL,
    breakfast_menu TEXT NULL,
    breakfast_calories DOUBLE NULL,
    breakfast_summary VARCHAR(255) NULL,
    lunch_menu TEXT NULL,
    lunch_calories DOUBLE NULL,
    lunch_summary VARCHAR(255) NULL,
    dinner_menu TEXT NULL,
    dinner_calories DOUBLE NULL,
    dinner_summary VARCHAR(255) NULL,
    raw_response LONGTEXT NULL,
    request_context JSON NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_ai_daily_meal_plan_email_date (email, plan_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ai_chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    conversation_date DATE NOT NULL,
    role VARCHAR(20) NOT NULL,
    message TEXT NOT NULL,
    request_context JSON NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_ai_chat_messages_email_date (email, conversation_date, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ai_weekly_nutrition_reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    target_date DATE NOT NULL,
    week_start_date DATE NOT NULL,
    week_end_date DATE NOT NULL,
    model VARCHAR(100) DEFAULT NULL,
    nutrition_assessment JSON NULL,
    guidance TEXT NULL,
    request_context JSON NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_ai_weekly_nutrition_email_date (email, target_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ai_weekly_exercise_reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    target_date DATE NOT NULL,
    week_start_date DATE NOT NULL,
    week_end_date DATE NOT NULL,
    model VARCHAR(100) DEFAULT NULL,
    exercise_assessment JSON NULL,
    recommendation TEXT NULL,
    request_context JSON NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_ai_weekly_exercise_email_date (email, target_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
