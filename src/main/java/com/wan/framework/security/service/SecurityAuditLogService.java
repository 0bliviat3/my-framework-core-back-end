package com.wan.framework.security.service;

import com.wan.framework.security.entity.SecurityAuditLog;
import com.wan.framework.security.repository.SecurityAuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * SecurityAuditLog Service
 */
@Service
@Transactional
public class SecurityAuditLogService {
    
    @Autowired
    private SecurityAuditLogRepository securityAuditLogRepository;
    
    public SecurityAuditLog createAuditLog(SecurityAuditLog auditLog) {
        return securityAuditLogRepository.save(auditLog);
    }
    
    public List<SecurityAuditLog> getRecentAuditLogs(int limit) {
        // 실제 구현에서는 최신 로그를 가져오는 로직 추가
        return securityAuditLogRepository.findAll();
    }
    
    public void logPermissionChange(String userId, String action, String resourceType, 
                                   String resourceId, String ipAddress, String userAgent, 
                                   String details) {
        SecurityAuditLog auditLog = SecurityAuditLog.builder()
            .userId(userId)
            .action(action)
            .resourceType(resourceType)
            .resourceId(resourceId)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .details(details)
            .build();
        createAuditLog(auditLog);
    }
}