package com.wan.framework.menu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wan.framework.menu.domain.MainPageSetting;

import java.util.Optional;

/**
 * 메인페이지 설정 리포지토리
 */
@Repository
public interface MainPageSettingRepository extends JpaRepository<MainPageSetting, Long> {
    
    /**
     * 사용자 ID로 메인페이지 설정 조회
     * @param userId 사용자 ID
     * @return 메인페이지 설정
     */
    Optional<MainPageSetting> findByUserId(Long userId);
}