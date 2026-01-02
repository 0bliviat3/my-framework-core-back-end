package com.wan.framework.apikey.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Component
public class ApiKeyGenerator {

    private static final String PREFIX = "sk_"; // Secret Key prefix
    private static final int RAW_KEY_LENGTH = 32; // 32 bytes = 256 bits
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * API Key 생성
     * 형식: sk_base64encodedRandomBytes
     */
    public String generateApiKey() {
        byte[] randomBytes = new byte[RAW_KEY_LENGTH];
        secureRandom.nextBytes(randomBytes);

        String encoded = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(randomBytes);

        return PREFIX + encoded;
    }

    /**
     * API Key 해시 (SHA-256)
     * DB에 저장할 때 사용
     */
    public String hashApiKey(String apiKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(apiKey.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 알고리즘을 찾을 수 없습니다.", e);
            throw new RuntimeException("API Key 해시 생성 실패", e);
        }
    }

    /**
     * API Key 접두사 추출 (첫 8자)
     */
    public String extractPrefix(String apiKey) {
        if (apiKey == null || apiKey.length() < 8) {
            return apiKey;
        }
        return apiKey.substring(0, Math.min(16, apiKey.length()));
    }

    /**
     * Byte 배열을 Hex 문자열로 변환
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    /**
     * API Key 형식 검증
     */
    public boolean isValidFormat(String apiKey) {
        return apiKey != null && apiKey.startsWith(PREFIX) && apiKey.length() > PREFIX.length();
    }
}
