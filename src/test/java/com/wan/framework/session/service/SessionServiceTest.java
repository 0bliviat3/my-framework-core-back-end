package com.wan.framework.session.service;

import com.wan.framework.session.config.SessionProperties;
import com.wan.framework.session.exception.SessionException;
import com.wan.framework.session.repository.SessionAuditRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * SessionService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private SessionAuditRepository sessionAuditRepository;

    @Mock
    private SessionProperties sessionProperties;

    @Mock
    private SessionSecurityService sessionSecurityService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @InjectMocks
    private SessionService sessionService;

    private SessionProperties.Cookie cookieConfig;
    private SessionProperties.Concurrent concurrentConfig;
    private SessionProperties.Refresh refreshConfig;
    private SessionProperties.Security securityConfig;

    @BeforeEach
    void setUp() {
        cookieConfig = new SessionProperties.Cookie();
        cookieConfig.setName("SESSION_ID");
        cookieConfig.setPath("/");
        cookieConfig.setHttpOnly(true);
        cookieConfig.setSecure(true);
        cookieConfig.setMaxAge(1800);

        concurrentConfig = new SessionProperties.Concurrent();
        concurrentConfig.setEnabled(false);  // 기본 테스트에서는 비활성화
        concurrentConfig.setMaxSessions(3);
        concurrentConfig.setPreventLogin(false);

        refreshConfig = new SessionProperties.Refresh();
        refreshConfig.setEnabled(false);  // 기본 테스트에서는 비활성화
        refreshConfig.setThreshold(0.5);

        securityConfig = new SessionProperties.Security();
        securityConfig.setValidateIp(true);
        securityConfig.setValidateUserAgent(false);

        lenient().when(sessionProperties.getCookie()).thenReturn(cookieConfig);
        lenient().when(sessionProperties.getConcurrent()).thenReturn(concurrentConfig);
        lenient().when(sessionProperties.getRefresh()).thenReturn(refreshConfig);
        lenient().when(sessionProperties.getSecurity()).thenReturn(securityConfig);
    }

    @Test
    @DisplayName("세션 생성 - 성공")
    void createSession_Success() {
        // given
        lenient().when(request.getSession(false)).thenReturn(null);
        lenient().when(request.getSession(true)).thenReturn(session);
        lenient().when(session.getId()).thenReturn("test-session-id");
        lenient().when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        lenient().when(request.getHeader(anyString())).thenReturn(null);  // 모든 헤더 요청에 null 반환
        lenient().when(request.getHeader("User-Agent")).thenReturn("TestAgent");

        // when
        var result = sessionService.createSession(
                request, response, "user123", "홍길동", Arrays.asList("ROLE_USER"));

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo("user123");
        assertThat(result.getSessionId()).isEqualTo("test-session-id");
        verify(session, atLeastOnce()).setAttribute(anyString(), any());
        verify(sessionAuditRepository).save(any());
    }

    @Test
    @DisplayName("현재 세션 조회 - 세션 없음")
    void getCurrentSession_NotFound() {
        // given
        given(request.getSession(false)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> sessionService.getCurrentSession(request))
                .isInstanceOf(SessionException.class);
    }

    @Test
    @DisplayName("세션 삭제 - 성공")
    void deleteSession_Success() {
        // given
        lenient().when(request.getSession(false)).thenReturn(session);
        lenient().when(session.getId()).thenReturn("test-session-id");
        lenient().when(session.getAttribute("userId")).thenReturn("user123");
        lenient().when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        lenient().when(request.getHeader(anyString())).thenReturn(null);  // 모든 헤더 요청에 null 반환
        lenient().when(request.getHeader("User-Agent")).thenReturn("TestAgent");

        // when
        sessionService.deleteSession(request, response);

        // then
        verify(session).invalidate();
        verify(sessionAuditRepository).save(any());
    }

    @Test
    @DisplayName("세션 유효성 검증 - 유효함")
    void validateSession_Valid() {
        // given
        given(request.getSession(false)).willReturn(session);
        doNothing().when(sessionSecurityService).validateSessionSecurity(any(), any());

        // when
        boolean result = sessionService.validateSession(request);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("세션 유효성 검증 - 세션 없음")
    void validateSession_NoSession() {
        // given
        given(request.getSession(false)).willReturn(null);

        // when
        boolean result = sessionService.validateSession(request);

        // then
        assertThat(result).isFalse();
    }
}
