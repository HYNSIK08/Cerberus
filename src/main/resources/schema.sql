CREATE DATABASE IF NOT EXISTS secauth CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE secauth;

-- CHANGE_THIS_PASSWORD 바꾸고 실행. db.properties / SECAUTH_DB_PASS 도 같은 값.
CREATE USER IF NOT EXISTS 'secauth'@'localhost' IDENTIFIED BY 'CHANGE_THIS_PASSWORD';
GRANT ALL PRIVILEGES ON secauth.* TO 'secauth'@'localhost';
FLUSH PRIVILEGES;

CREATE TABLE IF NOT EXISTS users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL UNIQUE,
    password_hash   VARCHAR(60)  NOT NULL,
    role            ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER',
    enabled         BOOLEAN NOT NULL DEFAULT TRUE,
    failed_attempts INT NOT NULL DEFAULT 0,
    locked_until    DATETIME NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login      DATETIME NULL
);

CREATE TABLE IF NOT EXISTS access_logs (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50) NULL,
    ip_address  VARCHAR(45) NOT NULL,
    action      VARCHAR(50) NOT NULL,
    result      ENUM('SUCCESS','FAILURE') NOT NULL,
    detail      VARCHAR(500) NULL,
    logged_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username  (username),
    INDEX idx_logged_at (logged_at),
    INDEX idx_result    (result)
);

CREATE TABLE IF NOT EXISTS security_policy (
    policy_key   VARCHAR(50)  NOT NULL PRIMARY KEY,
    policy_value VARCHAR(200) NOT NULL,
    description  VARCHAR(300) NULL
);

INSERT INTO security_policy (policy_key, policy_value, description) VALUES
('max_failed_attempts', '5',  '계정 잠금까지 최대 로그인 실패 횟수'),
('lockout_minutes',     '30', '계정 잠금 시간(분)'),
('session_timeout',     '30', '세션 만료 시간(분)'),
('password_min_length', '8',  '최소 비밀번호 길이')
ON DUPLICATE KEY UPDATE policy_value = VALUES(policy_value);
