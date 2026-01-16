package com.wan.framework.base.config;

import com.wan.framework.session.constant.SessionConstants;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * JPA Auditing을 위한 현재 사용자 제공
 * - 세션에서 로그인한 사용자 ID를 가져옴
 * - createdBy, updatedBy 필드에 자동 설정됨
 */
@Slf4j
@Component
public class SessionAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        try {
            ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes == null) {
                log.debug("No RequestAttributes available, using system user");
                return Optional.of("SYSTEM");
            }

            HttpSession session = attributes.getRequest().getSession(false);

            if (session == null) {
                log.debug("No active session, using system user");
                return Optional.of("SYSTEM");
            }

            String userId = (String) session.getAttribute(SessionConstants.ATTR_USER_ID);

            if (userId == null || userId.isEmpty()) {
                log.debug("No user in session, using system user");
                return Optional.of("SYSTEM");
            }

            log.debug("Current auditor: {}", userId);
            return Optional.of(userId);

        } catch (Exception e) {
            log.warn("Failed to get current auditor from session: {}", e.getMessage());
            return Optional.of("SYSTEM");
        }
    }
}
