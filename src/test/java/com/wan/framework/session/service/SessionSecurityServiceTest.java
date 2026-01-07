package com.wan.framework.session.service;

import com.wan.framework.session.config.SessionProperties;
import com.wan.framework.session.exception.SessionException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.wan.framework.session.constant.SessionConstants.ATTR_IP_ADDRESS;
import static com.wan.framework.session.constant.SessionConstants.ATTR_USER_AGENT;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

/**
 * SessionSecurityService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class SessionSecurityServiceTest {

    @Mock
    private SessionProperties sessionProperties;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @InjectMocks
    private SessionSecurityService sessionSecurityService;

    private SessionProperties.Security securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SessionProperties.Security();
        securityConfig.setValidateIp(true);
        securityConfig.setValidateUserAgent(false);

        given(sessionProperties.getSecurity()).willReturn(securityConfig);
    }

    @Test
    @DisplayName("IP 검증 - 일치")
    void validateIp_Match() {
        // given
        given(session.getAttribute(ATTR_IP_ADDRESS)).willReturn("127.0.0.1");
        given(request.getRemoteAddr()).willReturn("127.0.0.1");

        // when & then
        assertThatCode(() -> sessionSecurityService.validateSessionSecurity(session, request))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("IP 검증 - 불일치")
    void validateIp_Mismatch() {
        // given
        given(session.getAttribute(ATTR_IP_ADDRESS)).willReturn("127.0.0.1");
        given(request.getRemoteAddr()).willReturn("192.168.1.100");

        // when & then
        assertThatThrownBy(() -> sessionSecurityService.validateSessionSecurity(session, request))
                .isInstanceOf(SessionException.class);
    }

    @Test
    @DisplayName("User-Agent 검증 - 일치")
    void validateUserAgent_Match() {
        // given
        securityConfig.setValidateUserAgent(true);
        given(session.getAttribute(ATTR_USER_AGENT)).willReturn("TestAgent");
        given(request.getHeader("User-Agent")).willReturn("TestAgent");

        // when & then
        assertThatCode(() -> sessionSecurityService.validateSessionSecurity(session, request))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("User-Agent 검증 - 불일치")
    void validateUserAgent_Mismatch() {
        // given
        securityConfig.setValidateUserAgent(true);
        given(session.getAttribute(ATTR_USER_AGENT)).willReturn("TestAgent");
        given(request.getHeader("User-Agent")).willReturn("DifferentAgent");

        // when & then
        assertThatThrownBy(() -> sessionSecurityService.validateSessionSecurity(session, request))
                .isInstanceOf(SessionException.class);
    }

    @Test
    @DisplayName("검증 비활성화 - 예외 없음")
    void validateDisabled_NoException() {
        // given
        securityConfig.setValidateIp(false);
        securityConfig.setValidateUserAgent(false);

        // when & then
        assertThatCode(() -> sessionSecurityService.validateSessionSecurity(session, request))
                .doesNotThrowAnyException();
    }
}
