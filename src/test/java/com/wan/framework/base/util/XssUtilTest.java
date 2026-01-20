package com.wan.framework.base.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * XssUtil 테스트
 *
 * XSS 공격 패턴을 검증하고 HTML Sanitization이 정상 동작하는지 확인합니다.
 */
class XssUtilTest {

    @Test
    @DisplayName("텍스트만 허용 - 모든 HTML 태그 제거")
    void sanitizeTextOnly_shouldRemoveAllHtmlTags() {
        // Given
        String maliciousInput = "<script>alert('XSS')</script>안녕하세요";
        String normalInput = "안녕하세요 <b>테스트</b>입니다";

        // When
        String result1 = XssUtil.sanitizeTextOnly(maliciousInput);
        String result2 = XssUtil.sanitizeTextOnly(normalInput);

        // Then
        assertEquals("안녕하세요", result1);
        assertEquals("안녕하세요 테스트입니다", result2);
    }

    @Test
    @DisplayName("기본 서식 허용 - 안전한 서식 태그만 허용")
    void sanitizeBasicFormatting_shouldAllowSafeFormattingTags() {
        // Given
        String input = "안녕하세요 <b>굵게</b> <i>이탤릭</i> <u>밑줄</u> <script>alert('XSS')</script>";

        // When
        String result = XssUtil.sanitizeBasicFormatting(input);

        // Then
        assertTrue(result.contains("<b>굵게</b>"));
        assertTrue(result.contains("<i>이탤릭</i>"));
        assertTrue(result.contains("<u>밑줄</u>"));
        assertFalse(result.contains("<script>"));
        assertFalse(result.contains("alert"));
    }

    @Test
    @DisplayName("리치 콘텐츠 허용 - 안전한 HTML 서식 허용")
    void sanitizeRichContent_shouldAllowSafeHtmlElements() {
        // Given
        String safeHtml = "<p>단락입니다</p><a href=\"https://example.com\">링크</a><img src=\"image.jpg\" alt=\"이미지\">";
        String maliciousHtml = "<p>안전한 단락</p><script>alert('XSS')</script><iframe src=\"evil.com\"></iframe>";

        // When
        String result1 = XssUtil.sanitizeRichContent(safeHtml);
        String result2 = XssUtil.sanitizeRichContent(maliciousHtml);

        // Then
        // 안전한 태그는 허용
        assertTrue(result1.contains("<p>"));
        assertTrue(result1.contains("<a"));
        assertTrue(result1.contains("href"));

        // 위험한 태그는 제거
        assertFalse(result2.contains("<script>"));
        assertFalse(result2.contains("<iframe>"));
        assertFalse(result2.contains("alert"));
    }

    @Test
    @DisplayName("XSS 공격 패턴 감지 - script 태그")
    void containsSuspiciousPattern_shouldDetectScriptTag() {
        // Given
        String maliciousInput1 = "<script>alert('XSS')</script>";
        String maliciousInput2 = "<SCRIPT>alert('XSS')</SCRIPT>";
        String safeInput = "안전한 텍스트입니다";

        // When & Then
        assertTrue(XssUtil.containsSuspiciousPattern(maliciousInput1));
        assertTrue(XssUtil.containsSuspiciousPattern(maliciousInput2));
        assertFalse(XssUtil.containsSuspiciousPattern(safeInput));
    }

    @Test
    @DisplayName("XSS 공격 패턴 감지 - javascript: 프로토콜")
    void containsSuspiciousPattern_shouldDetectJavascriptProtocol() {
        // Given
        String maliciousInput = "<a href=\"javascript:alert('XSS')\">클릭</a>";
        String safeInput = "<a href=\"https://example.com\">클릭</a>";

        // When & Then
        assertTrue(XssUtil.containsSuspiciousPattern(maliciousInput));
        assertFalse(XssUtil.containsSuspiciousPattern(safeInput));
    }

    @Test
    @DisplayName("XSS 공격 패턴 감지 - 이벤트 핸들러")
    void containsSuspiciousPattern_shouldDetectEventHandlers() {
        // Given
        String maliciousInput1 = "<img src=x onerror=\"alert('XSS')\">";
        String maliciousInput2 = "<body onload=\"alert('XSS')\">";
        String maliciousInput3 = "<div onclick=\"maliciousFunction()\">";

        // When & Then
        assertTrue(XssUtil.containsSuspiciousPattern(maliciousInput1));
        assertTrue(XssUtil.containsSuspiciousPattern(maliciousInput2));
        assertTrue(XssUtil.containsSuspiciousPattern(maliciousInput3));
    }

    @Test
    @DisplayName("XSS 공격 패턴 감지 - iframe, embed, object")
    void containsSuspiciousPattern_shouldDetectDangerousTags() {
        // Given
        String iframe = "<iframe src=\"evil.com\"></iframe>";
        String embed = "<embed src=\"evil.swf\">";
        String object = "<object data=\"evil.swf\">";

        // When & Then
        assertTrue(XssUtil.containsSuspiciousPattern(iframe));
        assertTrue(XssUtil.containsSuspiciousPattern(embed));
        assertTrue(XssUtil.containsSuspiciousPattern(object));
    }

    @Test
    @DisplayName("SQL Injection 패턴 감지")
    void containsSuspiciousSqlPattern_shouldDetectSqlInjection() {
        // Given
        String sqlInjection1 = "admin' OR '1'='1";
        String sqlInjection2 = "'; DROP TABLE users--";
        String sqlInjection3 = "1 UNION SELECT * FROM users";
        String safeInput = "안전한 검색어";

        // When & Then
        assertTrue(XssUtil.containsSuspiciousSqlPattern(sqlInjection1));
        assertTrue(XssUtil.containsSuspiciousSqlPattern(sqlInjection2));
        assertTrue(XssUtil.containsSuspiciousSqlPattern(sqlInjection3));
        assertFalse(XssUtil.containsSuspiciousSqlPattern(safeInput));
    }

    @Test
    @DisplayName("Null 및 빈 문자열 처리")
    void sanitize_shouldHandleNullAndEmptyString() {
        // When & Then
        assertNull(XssUtil.sanitizeTextOnly(null));
        assertEquals("", XssUtil.sanitizeTextOnly(""));

        assertNull(XssUtil.sanitizeBasicFormatting(null));
        assertEquals("", XssUtil.sanitizeBasicFormatting(""));

        assertNull(XssUtil.sanitizeRichContent(null));
        assertEquals("", XssUtil.sanitizeRichContent(""));
    }

    @Test
    @DisplayName("리치 콘텐츠 - 링크에 rel=nofollow 자동 추가")
    void sanitizeRichContent_shouldAddRelNofollow() {
        // Given
        String html = "<a href=\"https://example.com\">링크</a>";

        // When
        String result = XssUtil.sanitizeRichContent(html);

        // Then
        assertTrue(result.contains("rel=\"nofollow\""));
    }

    @Test
    @DisplayName("리치 콘텐츠 - data: URL 차단")
    void sanitizeRichContent_shouldBlockDataUrl() {
        // Given
        String maliciousHtml = "<a href=\"data:text/html,<script>alert('XSS')</script>\">클릭</a>";

        // When
        String result = XssUtil.sanitizeRichContent(maliciousHtml);

        // Then
        assertFalse(result.contains("data:text/html"));
        assertFalse(result.contains("<script>"));
    }

    @Test
    @DisplayName("에디터 콘텐츠 - 더 많은 태그 허용")
    void sanitizeEditorContent_shouldAllowMoreTags() {
        // Given
        String html = "<h1>제목</h1><h2>부제목</h2><table><tr><th>헤더</th></tr></table>";

        // When
        String result = XssUtil.sanitizeEditorContent(html);

        // Then
        assertTrue(result.contains("<h1>"));
        assertTrue(result.contains("<h2>"));
        assertTrue(result.contains("<table>"));
        assertTrue(result.contains("<th>"));
    }

    @Test
    @DisplayName("특수문자 이스케이핑 확인")
    void sanitize_shouldPreserveSpecialCharacters() {
        // Given
        String input = "가격: 10,000원 (50% 할인!) & 무료배송";

        // When
        String result = XssUtil.sanitizeTextOnly(input);

        // Then
        assertEquals(input, result);
    }

    @Test
    @DisplayName("실제 게시글 시나리오 - 제목 Sanitization")
    void realWorldScenario_titleSanitization() {
        // Given
        String title = "공지사항 <script>alert('XSS')</script> - 필독";

        // When
        String sanitized = XssUtil.sanitizeTextOnly(title);

        // Then
        assertEquals("공지사항  - 필독", sanitized);
        assertFalse(sanitized.contains("<script>"));
    }

    @Test
    @DisplayName("실제 게시글 시나리오 - 본문 Sanitization")
    void realWorldScenario_contentSanitization() {
        // Given
        String content = "<p>안녕하세요.</p>" +
                "<p><b>공지사항</b>입니다.</p>" +
                "<script>alert('XSS')</script>" +
                "<a href=\"https://example.com\">링크</a>" +
                "<img src=x onerror=\"alert('XSS')\">";

        // When
        String sanitized = XssUtil.sanitizeRichContent(content);

        // Then
        assertTrue(sanitized.contains("<p>"));
        assertTrue(sanitized.contains("<b>"));
        assertTrue(sanitized.contains("<a"));
        assertFalse(sanitized.contains("<script>"));
        assertFalse(sanitized.contains("onerror"));
        assertFalse(sanitized.contains("alert"));
    }

    @Test
    @DisplayName("실제 댓글 시나리오 - 댓글 Sanitization")
    void realWorldScenario_commentSanitization() {
        // Given
        String comment = "좋은 글 감사합니다! <b>정말 유용하네요</b> <script>alert('XSS')</script>";

        // When
        String sanitized = XssUtil.sanitizeBasicFormatting(comment);

        // Then
        assertTrue(sanitized.contains("<b>"));
        assertFalse(sanitized.contains("<script>"));
        assertFalse(sanitized.contains("alert"));
    }
}
