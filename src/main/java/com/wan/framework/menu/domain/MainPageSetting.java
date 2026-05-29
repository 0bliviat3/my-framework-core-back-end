package com.wan.framework.menu.domain;

import jakarta.persistence.*;
import lombok.*;

import com.wan.framework.base.domain.BaseAuditEntity;

/**
 * 메인페이지 설정 도메인
 */
@Entity
@Table(name = "main_page_setting")
@EqualsAndHashCode(callSuper = true)
public class MainPageSetting extends BaseAuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "menu_id")
    private Long menuId;
    
    @Column(name = "title")
    private String title;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "theme")
    private String theme;
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getMenuId() {
        return menuId;
    }
    
    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getTheme() {
        return theme;
    }
    
    public void setTheme(String theme) {
        this.theme = theme;
    }
}