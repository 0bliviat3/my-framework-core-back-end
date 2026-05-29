package com.wan.framework.menu.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.wan.framework.menu.domain.Menu;
import com.wan.framework.menu.service.MenuService;

import java.util.List;

/**
 * 메뉴 컨트롤러
 */
@RestController
@RequestMapping("/menus")
public class MenuController {
    
    @Autowired
    private MenuService menuService;
    
    /**
     * 게스트 접근 가능한 메뉴 목록 조회
     * @return 게스트 접근 가능한 메뉴 리스트
     */
    @GetMapping("/guest-accessible")
    public ResponseEntity<List<Menu>> getGuestAccessibleMenus() {
        List<Menu> menus = menuService.getGuestAccessibleMenus();
        return ResponseEntity.ok(menus);
    }
}