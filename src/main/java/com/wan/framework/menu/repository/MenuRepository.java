package com.wan.framework.menu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wan.framework.menu.domain.Menu;

import java.util.List;

/**
 * 메뉴 리포지토리
 */
@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    
    /**
     * 게스트 접근 가능한 메뉴 목록 조회
     * @return 게스트 접근 가능한 메뉴 리스트
     */
    List<Menu> findByGuestAccessYnTrue();
}