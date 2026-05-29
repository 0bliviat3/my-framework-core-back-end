-- V2 권한 관리 시스템을 위한 데이터베이스 스키마 변경

-- 1. RoleHierarchy 테이블 생성
CREATE TABLE IF NOT EXISTS t_role_hierarchy (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_role_id BIGINT NOT NULL,
    child_role_id BIGINT NOT NULL,
    inherited BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_role_id) REFERENCES t_role(role_id),
    FOREIGN KEY (child_role_id) REFERENCES t_role(role_id)
);

-- 2. DetailedPermission 테이블 생성
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
    FOREIGN KEY (role_id) REFERENCES t_role(role_id)
);

-- 3. SecurityAuditLog 테이블 생성
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

-- 4. 기존 테이블에 추가 컬럼 추가
ALTER TABLE t_role 
ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE;

-- 5. 인덱스 추가 (성능 최적화)
CREATE INDEX IF NOT EXISTS idx_role_hierarchy_parent ON t_role_hierarchy(parent_role_id);
CREATE INDEX IF NOT EXISTS idx_role_hierarchy_child ON t_role_hierarchy(child_role_id);
CREATE INDEX IF NOT EXISTS idx_detailed_permission_role ON t_detailed_permission(role_id);
CREATE INDEX IF NOT EXISTS idx_detailed_permission_method_pattern ON t_detailed_permission(http_method, uri_pattern);