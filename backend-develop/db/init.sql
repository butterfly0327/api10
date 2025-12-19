CREATE DATABASE IF NOT EXISTS yumyumcoach
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

USE yumyumcoach;

CREATE TABLE accounts (
  email VARCHAR(255) NOT NULL,
  username VARCHAR(255) NOT NULL,
  password VARCHAR(255) NOT NULL,
  PRIMARY KEY (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE refresh_tokens (
    email VARCHAR(255) NOT NULL,
    token_hash CHAR(64) NOT NULL,
    expires_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (email),
    UNIQUE KEY uk_refresh_email_hash (email, token_hash),
    KEY idx_refresh_expires (expires_at),
    CONSTRAINT fk_refresh_email
        FOREIGN KEY (email) REFERENCES accounts(email) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE titles (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE profiles (
  email VARCHAR(255) NOT NULL,
  name VARCHAR(255) NOT NULL,
  introduction VARCHAR(255) DEFAULT NULL,
  height DOUBLE DEFAULT NULL,
  current_weight DOUBLE DEFAULT NULL,
  target_weight DOUBLE DEFAULT NULL,
  has_diabetes TINYINT DEFAULT NULL,
  has_hypertension TINYINT DEFAULT NULL,
  has_hyperlipidemia TINYINT DEFAULT NULL,
  other_disease TEXT DEFAULT NULL,
  goal VARCHAR(255) DEFAULT NULL,
  activity_level VARCHAR(255) DEFAULT NULL,
  display_title_id BIGINT UNSIGNED DEFAULT NULL,
  PRIMARY KEY (email),
  CONSTRAINT fk_profiles_account
    FOREIGN KEY (email) REFERENCES accounts(email),
  CONSTRAINT fk_profiles_display_title
    FOREIGN KEY (display_title_id) REFERENCES titles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE posts (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  author_email VARCHAR(255) NOT NULL,
  title VARCHAR(255) NOT NULL,
  category VARCHAR(255) DEFAULT NULL,
  content TEXT DEFAULT NULL,
  created_at DATETIME NOT NULL,
  likes INT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  CONSTRAINT fk_posts_author
    FOREIGN KEY (author_email) REFERENCES accounts(email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE post_images (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  post_id BIGINT UNSIGNED NOT NULL,
  image_url VARCHAR(255) NOT NULL,
  order_index INT NOT NULL DEFAULT 1,
  PRIMARY KEY (id),
  UNIQUE KEY uq_post_images_post_order (post_id, order_index),
  CONSTRAINT fk_post_images_post
    FOREIGN KEY (post_id) REFERENCES posts(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE post_comments (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  post_id BIGINT UNSIGNED NOT NULL,
  author_email VARCHAR(255) NOT NULL,
  content TEXT NOT NULL,
  created_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_post_comments_post
    FOREIGN KEY (post_id) REFERENCES posts(id),
  CONSTRAINT fk_post_comments_author
    FOREIGN KEY (author_email) REFERENCES accounts(email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE post_likes (
  post_id BIGINT UNSIGNED NOT NULL,
  email VARCHAR(255) NOT NULL,
  created_at DATETIME NOT NULL,
  PRIMARY KEY (post_id, email),
  KEY idx_post_likes_email_created_at (email, created_at),
  CONSTRAINT fk_post_likes_post
    FOREIGN KEY (post_id) REFERENCES posts(id),
  CONSTRAINT fk_post_likes_account
    FOREIGN KEY (email) REFERENCES accounts(email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE diet_records (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  email VARCHAR(255) NOT NULL,
  record_date DATE NOT NULL,
  meal_type VARCHAR(255) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_diet_records_account
    FOREIGN KEY (email) REFERENCES accounts(email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE foods (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  carbohydrate DOUBLE DEFAULT NULL,
  protein DOUBLE DEFAULT NULL,
  fat DOUBLE DEFAULT NULL,
  calories DOUBLE DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE diet_foods (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  diet_id BIGINT UNSIGNED NOT NULL,
  order_index INT NOT NULL,
  food_id BIGINT UNSIGNED NOT NULL,
  weight VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_diet_foods_diet_order (diet_id, order_index),
  CONSTRAINT fk_diet_foods_record
    FOREIGN KEY (diet_id) REFERENCES diet_records(id),
  CONSTRAINT fk_diet_foods_food
    FOREIGN KEY (food_id) REFERENCES foods(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE exercises (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  met DOUBLE NOT NULL,
  intensity_level VARCHAR(255) NOT NULL,
  type VARCHAR(255) NOT NULL,
  description TEXT DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE exercise_records (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  email VARCHAR(255) NOT NULL,
  exercise_id BIGINT UNSIGNED NOT NULL,
  record_date DATE NOT NULL,
  duration_minutes DOUBLE NOT NULL,
  calories DOUBLE NOT NULL,
  PRIMARY KEY (id),
  KEY idx_exercise_records_email_date (email, record_date),
  CONSTRAINT fk_exercise_records_account
    FOREIGN KEY (email) REFERENCES accounts(email),
  CONSTRAINT fk_exercise_records_exercise
    FOREIGN KEY (exercise_id) REFERENCES exercises(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE challenge_difficulties (
  code VARCHAR(255) NOT NULL,
  name VARCHAR(255) NOT NULL,
  description TEXT DEFAULT NULL,
  PRIMARY KEY (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE challenge_types (
  code VARCHAR(255) NOT NULL,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE challenges (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  reward_title_id BIGINT UNSIGNED DEFAULT NULL,
  name VARCHAR(255) NOT NULL,
  short_description VARCHAR(255) NOT NULL,
  difficulty_code VARCHAR(255) NOT NULL,
  goal_summary VARCHAR(255) NOT NULL,
  rule_description TEXT DEFAULT NULL,
  type_code VARCHAR(255) NOT NULL,
  image_url VARCHAR(255) DEFAULT NULL,
  start_date DATE NOT NULL,
  duration_days INT NOT NULL DEFAULT 30,
  is_active TINYINT NOT NULL DEFAULT 1,
  PRIMARY KEY (id),
  CONSTRAINT fk_challenges_difficulty
    FOREIGN KEY (difficulty_code) REFERENCES challenge_difficulties(code),
  CONSTRAINT fk_challenges_type
    FOREIGN KEY (type_code) REFERENCES challenge_types(code),
  CONSTRAINT fk_challenges_reward_title
    FOREIGN KEY (reward_title_id) REFERENCES titles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE challenge_participants (
  challenge_id BIGINT UNSIGNED NOT NULL,
  email VARCHAR(255) NOT NULL,
  joined_at DATETIME NOT NULL,
  status VARCHAR(255) NOT NULL DEFAULT 'joined',
  progress_percentage DOUBLE NOT NULL DEFAULT 0,
  last_evaluated_at DATETIME DEFAULT NULL,
  completed_at DATETIME DEFAULT NULL,
  PRIMARY KEY (challenge_id, email),
  CONSTRAINT fk_challenge_participants_challenge
    FOREIGN KEY (challenge_id) REFERENCES challenges(id),
  CONSTRAINT fk_challenge_participants_account
    FOREIGN KEY (email) REFERENCES accounts(email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE account_titles (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  title_id BIGINT UNSIGNED NOT NULL,
  email VARCHAR(255) NOT NULL,
  obtained_at DATETIME NOT NULL,
  source_challenge_id BIGINT UNSIGNED DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_account_titles_email_title (email, title_id),
  CONSTRAINT fk_account_titles_account
    FOREIGN KEY (email) REFERENCES accounts(email),
  CONSTRAINT fk_account_titles_title
    FOREIGN KEY (title_id) REFERENCES titles(id),
  CONSTRAINT fk_account_titles_challenge
    FOREIGN KEY (source_challenge_id) REFERENCES challenges(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE follows (
  follower_email VARCHAR(255) NOT NULL,
  followee_email VARCHAR(255) NOT NULL,
  followed_at DATETIME NOT NULL,
  PRIMARY KEY (follower_email, followee_email),
  CONSTRAINT fk_follows_follower
    FOREIGN KEY (follower_email) REFERENCES accounts(email),
  CONSTRAINT fk_follows_followee
    FOREIGN KEY (followee_email) REFERENCES accounts(email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
