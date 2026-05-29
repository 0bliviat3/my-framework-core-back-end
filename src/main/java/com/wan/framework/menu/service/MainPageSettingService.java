package com.wan.framework.menu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wan.framework.menu.domain.MainPageSetting;
import com.wan.framework.menu.repository.MainPageSettingRepository;

import java.util.Optional;

/**
 * 메인페이지 설정 서비스
 */
@Service
public class MainPageSettingService {
    
    @Autowired
    private MainPageSettingRepository mainPageSettingRepository;
    
    /**
     * 메인페이지 설정 저장
     * @param setting 메인페이지 설정
     * @return 저장된 메인페이지 설정
     */
    public MainPageSetting saveMainPageSetting(MainPageSetting setting) {
        return mainPageSettingRepository.save(setting);
    }
    
    /**
     * 사용자 ID로 메인페이지 설정 조회
     * @param userId 사용자 ID
     * @return 메인페이지 설정
     */
    public Optional<MainPageSetting> getMainPageSetting(Long userId) {
        return mainPageSettingRepository.findByUserId(userId);
    }
}