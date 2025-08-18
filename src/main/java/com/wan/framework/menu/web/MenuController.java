package com.wan.framework.menu.web;

import com.wan.framework.menu.dto.MenuDTO;
import com.wan.framework.menu.dto.MenuTreeNodeDTO;
import com.wan.framework.menu.service.MenuService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    /**
     * 메뉴 생성
     */
    @PostMapping
    public ResponseEntity<MenuDTO> createMenu(@RequestBody MenuDTO request) {
        MenuDTO created = menuService.createMenu(request);
        return ResponseEntity.ok(created);
    }

    /**
     * 메뉴 단건 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<MenuDTO> getMenu(@PathVariable Long id) {
        MenuDTO menu = menuService.getMenu(id);
        return ResponseEntity.ok(menu);
    }

    @GetMapping
    public ResponseEntity<List<MenuDTO>> getMenu() {
        return ResponseEntity.ok(menuService.findAll());
    }

    /**
     * 모든 메뉴 트리 조회
     */
    @GetMapping("/tree")
    public ResponseEntity<MenuTreeNodeDTO> getMenuTree(HttpSession session) {
        String roles = (String) Optional.ofNullable(session.getAttribute("roles")).orElse("guest");
        if (roles.equals("admin")) {
            MenuTreeNodeDTO tree = menuService.findAllTree();
            return ResponseEntity.ok(tree);
        }
        MenuTreeNodeDTO tree = menuService.findAllTreeByRoles(roles);
        return ResponseEntity.ok(tree);
    }

    /**
     * 메뉴 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<MenuDTO> updateMenu(@PathVariable Long id, @RequestBody MenuDTO request) {
        MenuDTO updated = menuService.updateMenu(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * 메뉴 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMenu(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return ResponseEntity.noContent().build();
    }
}
