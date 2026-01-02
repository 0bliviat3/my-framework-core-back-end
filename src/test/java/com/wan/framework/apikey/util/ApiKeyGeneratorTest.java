package com.wan.framework.apikey.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class ApiKeyGeneratorTest {

    @Autowired
    private ApiKeyGenerator generator;

    @Test
    void API_Key_생성_성공() {
        // when
        String apiKey = generator.generateApiKey();

        // then
        assertThat(apiKey).isNotNull();
        assertThat(apiKey).startsWith("sk_");
        assertThat(apiKey.length()).isGreaterThan(10);
    }

    @Test
    void API_Key_고유성_검증() {
        // given
        Set<String> keys = new HashSet<>();
        int count = 1000;

        // when
        for (int i = 0; i < count; i++) {
            keys.add(generator.generateApiKey());
        }

        // then
        assertThat(keys).hasSize(count); // 모두 고유해야 함
    }

    @Test
    void API_Key_해시_생성() {
        // given
        String apiKey = generator.generateApiKey();

        // when
        String hash = generator.hashApiKey(apiKey);

        // then
        assertThat(hash).isNotNull();
        assertThat(hash.length()).isEqualTo(64); // SHA-256 해시는 64자 16진수
    }

    @Test
    void 동일한_API_Key는_동일한_해시_생성() {
        // given
        String apiKey = generator.generateApiKey();

        // when
        String hash1 = generator.hashApiKey(apiKey);
        String hash2 = generator.hashApiKey(apiKey);

        // then
        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    void 다른_API_Key는_다른_해시_생성() {
        // given
        String apiKey1 = generator.generateApiKey();
        String apiKey2 = generator.generateApiKey();

        // when
        String hash1 = generator.hashApiKey(apiKey1);
        String hash2 = generator.hashApiKey(apiKey2);

        // then
        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    void API_Key_접두사_추출() {
        // given
        String apiKey = generator.generateApiKey();

        // when
        String prefix = generator.extractPrefix(apiKey);

        // then
        assertThat(prefix).isNotNull();
        assertThat(prefix.length()).isLessThanOrEqualTo(16);
        assertThat(apiKey).startsWith(prefix);
    }

    @Test
    void 짧은_문자열_접두사_추출() {
        // when
        String prefix = generator.extractPrefix("sk_abc");

        // then
        assertThat(prefix).isEqualTo("sk_abc");
    }

    @Test
    void null_접두사_추출() {
        // when
        String prefix = generator.extractPrefix(null);

        // then
        assertThat(prefix).isNull();
    }

    @Test
    void 유효한_형식_검증_성공() {
        // given
        String apiKey = generator.generateApiKey();

        // when
        boolean isValid = generator.isValidFormat(apiKey);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    void 접두사_없는_키_형식_검증_실패() {
        // when
        boolean isValid = generator.isValidFormat("invalid_key_without_prefix");

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    void 짧은_키_형식_검증_실패() {
        // when
        boolean isValid = generator.isValidFormat("sk_");

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    void null_키_형식_검증_실패() {
        // when
        boolean isValid = generator.isValidFormat(null);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    void API_Key_길이_일관성() {
        // given
        Set<Integer> lengths = new HashSet<>();

        // when
        for (int i = 0; i < 100; i++) {
            String apiKey = generator.generateApiKey();
            lengths.add(apiKey.length());
        }

        // then
        assertThat(lengths).hasSize(1); // 모든 키의 길이가 동일해야 함
    }

    @Test
    void 해시_길이_일관성() {
        // given
        Set<Integer> lengths = new HashSet<>();

        // when
        for (int i = 0; i < 100; i++) {
            String apiKey = generator.generateApiKey();
            String hash = generator.hashApiKey(apiKey);
            lengths.add(hash.length());
        }

        // then
        assertThat(lengths).hasSize(1); // 모든 해시의 길이가 동일해야 함
        assertThat(lengths).contains(64); // SHA-256은 64자
    }

    @Test
    void 해시_16진수_형식() {
        // given
        String apiKey = generator.generateApiKey();

        // when
        String hash = generator.hashApiKey(apiKey);

        // then
        assertThat(hash).matches("[0-9a-f]{64}"); // 소문자 16진수
    }
}
