-- AI 및 통계 신규 기능용 테이블 모음

CREATE TABLE IF NOT EXISTS ai_meal_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    plan_date DATE NOT NULL,
    breakfast_menu TEXT,
    lunch_menu TEXT,
    dinner_menu TEXT,
    breakfast_calories DOUBLE,
    lunch_calories DOUBLE,
    dinner_calories DOUBLE,
    breakfast_note TEXT,
    lunch_note TEXT,
    dinner_note TEXT,
    total_calories DOUBLE,
    model_used VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_ai_meal_plan_user_date (email, plan_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ai_chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    conversation_date DATE NOT NULL,
    role VARCHAR(20) NOT NULL,
    message TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_ai_chat_user_date (email, conversation_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ai_nutrition_evaluations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    reference_date DATE NOT NULL,
    week_start_date DATE NOT NULL,
    week_end_date DATE NOT NULL,
    carbohydrate_status VARCHAR(50),
    protein_status VARCHAR(50),
    fat_status VARCHAR(50),
    calorie_status VARCHAR(50),
    analysis_summary TEXT,
    model_used VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_ai_nutrition_user_date (email, reference_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ai_exercise_evaluations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    reference_date DATE NOT NULL,
    week_start_date DATE NOT NULL,
    week_end_date DATE NOT NULL,
    volume_status VARCHAR(50),
    recommendation TEXT,
    model_used VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_ai_exercise_user_date (email, reference_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
