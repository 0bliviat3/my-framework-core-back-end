package com.wan.framework.menu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wan.framework.menu.domain.Menu;
import com.wan.framework.menu.repository.MenuRepository;

import java.util.List;

/**
 * 메뉴 서비스
 */
@Service
public class MenuService {
    
    @Autowired
    private MenuRepository menuRepository;
    
    /**
     * 게스트 접근 가능한 메뉴 목록 조회
     * @return 게스트 접근 가능한 메뉴 리스트
     */
    public List<Menu> getGuestAccessibleMenus() {
        return menuRepository.findByGuestAccessYnTrue();
    }
}