-- V2 테스트를 위한 초기 데이터베이스 설정

-- 사용자 테이블 생성
CREATE TABLE IF NOT EXISTS t_user (
    user_id VARCHAR(50) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_code VARCHAR(20) DEFAULT 'I'
);

-- 역할 테이블 생성
CREATE TABLE IF NOT EXISTS t_role (
    role_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(50) UNIQUE NOT NULL,
    role_name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- 역할 계층 테이블 생성
CREATE TABLE IF NOT EXISTS t_role_hierarchy (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_role_id BIGINT,
    child_role_id BIGINT,
    inherited BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_role_id) REFERENCES t_role(role_id),
    FOREIGN KEY (child_role_id) REFERENCES t_role(role_id)
);

-- 세부 권한 테이블 생성
CREATE TABLE IF NOT EXISTS t_detailed_permission (
    permission_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    http_method VARCHAR(10),
    uri_pattern VARCHAR(500),
    allowed BOOLEAN DEFAULT TRUE,
    valid_from TIMESTAMP,
    valid_to TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES t't_role(role_id)
);

-- 보안 로그 테이블 생성
CREATE TABLE IF NOT EXISTS t_security_audit_log (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(50),
    action VARCHAR(50) NOT NULL,
    resource_type VARCHAR(50),
    resource_id VARCHAR(100),
    ip_address VARCHAR(45),
    user_agent TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    details JSON
);

-- 기본 데이터 삽입
INSERT IGNORE INTO t_role (role_code, role_name, description) VALUES 
('ROLE_ADMIN', '관리자', '시스템 관리자'),
('ROLE_USER', '일반 사용자', '일반 사용자'),
('ROLE_MANAGER', '매니저', '매니저 사용자');

-- 관리자 계정 생성
INSERT IGNORE INTO t_user (user_id, password, name, email) VALUES 
('admin', '$2a$10$8K1BmLpVqGJdDkYHtEjZuOyQzXnRvPfMgNqHbWxUcTmKlQpSfYyA.', '시스템 관리자', 'admin@example.com'),
('user1', '$2a$10$8K1BmLpVqGJdDkYHtEjZuOyQzXnRvPfMgNqHbWxUcTmKlQpSfYyA.', '일반 사용자', 'user1@example.com');

-- 기본 권한 설정
INSERT IGNORE INTO t_detailed_permission (role_id, http_method, uri_pattern, allowed) VALUES
((SELECT role_id FROM t_role WHERE role_code = 'ROLE_ADMIN'), 'GET', '/api/admin/*', true),
((SELECT role_id FROM t_role WHERE role_code = 'ROLE_ADMIN'), 'POST', '/api/admin/*', true),
((SELECT role_id FROM t_role WHERE role_code = 'ROLE_USER'), 'GET', '/api/users/*', true),
((SELECT role_id FROM t_role WHERE role_code = 'ROLE_USER'), 'GET', '/api/public/*', true);