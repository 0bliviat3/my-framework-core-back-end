package com.wan.framework.menu.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.wan.framework.menu.domain.MainPageSetting;
import com.wan.framework.menu.service.MainPageSettingService;

import java.util.Optional;

/**
 * 메인페이지 설정 컨트롤러
 */
@RestController
@RequestMapping("/main-setting")
public class MainPageSettingController {
    
    @Autowired
    private MainPageSettingService mainPageSettingService;
    
    /**
     * 메인페이지 설정 저장
     * @param setting 메인페이지 설정
     * @return 저장된 메인페이지 설정
     */
    @PostMapping
    public ResponseEntity<MainPageSetting> saveMainPageSetting(@RequestBody MainPageSetting setting) {
        MainPageSetting savedSetting = mainPageSettingService.saveMainPageSetting(setting);
        return ResponseEntity.ok(savedSetting);
    }
    
    /**
     * 사용자 ID로 메인페이지 설정 조회
     * @param userId 사용자 ID
     * @return 메인페이지 설정
     */
    @GetMapping("/{userId}")
    public ResponseEntity<MainPageSetting> getMainPageSetting(@PathVariable Long userId) {
        Optional<MainPageSetting> setting = mainPageSettingService.getMainPageSetting(userId);
        return setting.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}