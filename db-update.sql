-- 메뉴 테이블에 게스트 접근 가능 여부 컬럼 추가
ALTER TABLE menu ADD COLUMN IF NOT EXISTS guest_access_yn BOOLEAN DEFAULT FALSE;

-- 메뉴 테이블에 메인페이지 지정 여부 컬럼 추가
ALTER TABLE menu ADD COLUMN IF NOT EXISTS main_page_yn BOOLEAN DEFAULT FALSE;

-- 메인페이지 설정 테이블 생성
CREATE TABLE IF NOT EXISTS main_page_setting (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    menu_id BIGINT,
    title VARCHAR(255),
    description TEXT,
    theme VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);