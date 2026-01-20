package com.wan.framework.base.util;

import lombok.extern.slf4j.Slf4j;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

/**
 * XSS(Cross-Site Scripting) 공격 방어 유틸리티
 *
 * OWASP Java HTML Sanitizer를 사용하여 사용자 입력 HTML을 안전하게 필터링합니다.
 *
 * @author Framework Team
 * @since 1.0.0
 */
@Slf4j
public class XssUtil {

    /**
     * 기본 정책: 모든 HTML 태그 제거, 텍스트만 허용
     * 일반 텍스트 입력에 사용 (제목, 댓글 등)
     */
    private static final PolicyFactory TEXT_ONLY_POLICY = new HtmlPolicyBuilder()
            .toFactory();

    /**
     * 기본 서식 정책: 안전한 서식 태그만 허용
     * 간단한 서식이 필요한 경우 사용 (굵게, 이탤릭, 밑줄 등)
     */
    private static final PolicyFactory BASIC_FORMATTING_POLICY = new HtmlPolicyBuilder()
            .allowElements("b", "i", "u", "em", "strong", "br")
            .toFactory();

    /**
     * 리치 콘텐츠 정책: 안전한 HTML 서식 허용
     * 게시글 본문 등에 사용 (단락, 목록, 링크, 이미지 등)
     */
    private static final PolicyFactory RICH_CONTENT_POLICY = Sanitizers.FORMATTING
            .and(Sanitizers.BLOCKS)
            .and(Sanitizers.LINKS)
            .and(Sanitizers.IMAGES)
            .and(Sanitizers.TABLES)
            .and(new HtmlPolicyBuilder()
                    // 추가 안전한 태그
                    .allowElements("hr", "pre", "code", "blockquote")

                    // 안전한 속성만 허용
                    .allowAttributes("class").onElements("p", "div", "span", "code", "pre")
                    .allowAttributes("alt", "title").onElements("img")
                    .allowAttributes("href", "title", "target", "rel").onElements("a")

                    // 링크는 http, https, mailto만 허용
                    .allowStandardUrlProtocols()
                    .requireRelNofollowOnLinks()

                    .toFactory());

    /**
     * 커스텀 정책: 에디터 전용 (최대한 허용)
     * 관리자 전용 에디터 등에 사용 (더 많은 태그 허용)
     */
    private static final PolicyFactory EDITOR_POLICY = new HtmlPolicyBuilder()
            // 기본 서식
            .allowElements("b", "i", "u", "em", "strong", "s", "del", "ins", "mark", "small", "sub", "sup", "br")

            // 블록 요소
            .allowElements("p", "div", "span", "h1", "h2", "h3", "h4", "h5", "h6", "hr", "pre", "code", "blockquote")

            // 리스트
            .allowElements("ul", "ol", "li")

            // 테이블
            .allowElements("table", "thead", "tbody", "tfoot", "tr", "th", "td", "caption")

            // 링크 및 미디어
            .allowElements("a", "img")

            // 속성
            .allowAttributes("class", "id", "style").globally()
            .allowAttributes("href", "title", "target", "rel").onElements("a")
            .allowAttributes("src", "alt", "title", "width", "height").onElements("img")
            .allowAttributes("border", "cellpadding", "cellspacing").onElements("table")
            .allowAttributes("align", "valign", "colspan", "rowspan").onElements("th", "td")

            // URL 프로토콜
            .allowStandardUrlProtocols()
            .requireRelNofollowOnLinks()

            .toFactory();

    /**
     * 텍스트만 허용 (모든 HTML 태그 제거)
     *
     * @param input 사용자 입력 문자열
     * @return 안전하게 필터링된 텍스트
     */
    public static String sanitizeTextOnly(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String sanitized = TEXT_ONLY_POLICY.sanitize(input);
        logIfChanged(input, sanitized, "TEXT_ONLY");
        return sanitized;
    }

    /**
     * 기본 서식만 허용 (굵게, 이탤릭, 밑줄 등)
     *
     * @param input 사용자 입력 문자열
     * @return 안전하게 필터링된 HTML
     */
    public static String sanitizeBasicFormatting(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String sanitized = BASIC_FORMATTING_POLICY.sanitize(input);
        logIfChanged(input, sanitized, "BASIC_FORMATTING");
        return sanitized;
    }

    /**
     * 리치 콘텐츠 허용 (게시글 본문용)
     * 안전한 HTML 서식, 링크, 이미지, 테이블 등을 허용합니다.
     *
     * @param input 사용자 입력 HTML
     * @return 안전하게 필터링된 HTML
     */
    public static String sanitizeRichContent(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String sanitized = RICH_CONTENT_POLICY.sanitize(input);
        logIfChanged(input, sanitized, "RICH_CONTENT");
        return sanitized;
    }

    /**
     * 에디터 콘텐츠 허용 (관리자용)
     * 더 많은 HTML 태그와 속성을 허용합니다.
     *
     * @param input 사용자 입력 HTML
     * @return 안전하게 필터링된 HTML
     */
    public static String sanitizeEditorContent(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String sanitized = EDITOR_POLICY.sanitize(input);
        logIfChanged(input, sanitized, "EDITOR");
        return sanitized;
    }

    /**
     * 커스텀 정책으로 Sanitize
     *
     * @param input 사용자 입력 문자열
     * @param policy 커스텀 PolicyFactory
     * @return 안전하게 필터링된 문자열
     */
    public static String sanitizeWithPolicy(String input, PolicyFactory policy) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        return policy.sanitize(input);
    }

    /**
     * 입력값이 변경되었는지 로깅
     *
     * @param original 원본 문자열
     * @param sanitized 필터링된 문자열
     * @param policyName 정책 이름
     */
    private static void logIfChanged(String original, String sanitized, String policyName) {
        if (!original.equals(sanitized)) {
            log.warn("XSS 필터링 적용 [{}]: {} 문자 → {} 문자",
                    policyName, original.length(), sanitized.length());
            log.debug("XSS 필터링 원본: {}", original);
            log.debug("XSS 필터링 결과: {}", sanitized);
        }
    }

    /**
     * XSS 공격 패턴 감지 (추가 검증용)
     *
     * @param input 입력 문자열
     * @return 의심스러운 패턴이 있으면 true
     */
    public static boolean containsSuspiciousPattern(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }

        String lowerInput = input.toLowerCase();

        // 일반적인 XSS 공격 패턴
        String[] suspiciousPatterns = {
            "<script",
            "javascript:",
            "onerror=",
            "onload=",
            "onclick=",
            "onmouseover=",
            "onfocus=",
            "<iframe",
            "<embed",
            "<object",
            "eval(",
            "expression(",
            "vbscript:",
            "data:text/html"
        };

        for (String pattern : suspiciousPatterns) {
            if (lowerInput.contains(pattern)) {
                log.warn("의심스러운 XSS 패턴 감지: {}", pattern);
                return true;
            }
        }

        return false;
    }

    /**
     * SQL Injection 패턴 감지 (추가 보안)
     *
     * @param input 입력 문자열
     * @return 의심스러운 SQL 패턴이 있으면 true
     */
    public static boolean containsSuspiciousSqlPattern(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }

        String lowerInput = input.toLowerCase();

        // 일반적인 SQL Injection 패턴
        String[] sqlPatterns = {
            "' or '1'='1",
            "' or 1=1",
            "'; drop table",
            "'; delete from",
            "union select",
            "exec(",
            "execute(",
            "xp_cmdshell"
        };

        for (String pattern : sqlPatterns) {
            if (lowerInput.contains(pattern)) {
                log.warn("의심스러운 SQL Injection 패턴 감지: {}", pattern);
                return true;
            }
        }

        return false;
    }
}
