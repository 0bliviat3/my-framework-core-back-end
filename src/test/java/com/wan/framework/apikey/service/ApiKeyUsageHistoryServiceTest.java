package com.wan.framework.apikey.service;

import com.wan.framework.apikey.dto.ApiKeyDTO;
import com.wan.framework.apikey.dto.ApiKeyUsageHistoryDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class ApiKeyUsageHistoryServiceTest {

    @Autowired
    private ApiKeyUsageHistoryService usageHistoryService;

    @Autowired
    private ApiKeyService apiKeyService;

    private Long testApiKeyId;

    @BeforeEach
    void setUp() {
        ApiKeyDTO apiKey = apiKeyService.createApiKey("테스트 API Key", null, null, "user1");
        testApiKeyId = apiKey.getId();
    }

    @Test
    void 사용_이력_기록_성공() {
        // when
        usageHistoryService.recordUsage(
                testApiKeyId,
                "/api/test",
                "GET",
                "192.168.0.1",
                "Mozilla/5.0",
                200,
                true,
                null
        );

        // then
        Page<ApiKeyUsageHistoryDTO> history = usageHistoryService.findByApiKeyId(
                testApiKeyId, PageRequest.of(0, 10));

        assertThat(history.getContent()).hasSize(1);
        ApiKeyUsageHistoryDTO record = history.getContent().get(0);
        assertThat(record.getRequestUri()).isEqualTo("/api/test");
        assertThat(record.getRequestMethod()).isEqualTo("GET");
        assertThat(record.getIpAddress()).isEqualTo("192.168.0.1");
        assertThat(record.getResponseStatus()).isEqualTo(200);
        assertThat(record.getIsSuccess()).isTrue();
    }

    @Test
    void 실패_이력_기록() {
        // when
        usageHistoryService.recordUsage(
                testApiKeyId,
                "/api/test",
                "POST",
                "192.168.0.1",
                "Mozilla/5.0",
                401,
                false,
                "Unauthorized"
        );

        // then
        Page<ApiKeyUsageHistoryDTO> history = usageHistoryService.findByApiKeyId(
                testApiKeyId, PageRequest.of(0, 10));

        assertThat(history.getContent()).hasSize(1);
        ApiKeyUsageHistoryDTO record = history.getContent().get(0);
        assertThat(record.getIsSuccess()).isFalse();
        assertThat(record.getErrorMessage()).isEqualTo("Unauthorized");
        assertThat(record.getResponseStatus()).isEqualTo(401);
    }

    @Test
    void 여러_이력_기록() {
        // when
        for (int i = 0; i < 5; i++) {
            usageHistoryService.recordUsage(
                    testApiKeyId,
                    "/api/test/" + i,
                    "GET",
                    "192.168.0.1",
                    "Mozilla/5.0",
                    200,
                    true,
                    null
            );
        }

        // then
        Page<ApiKeyUsageHistoryDTO> history = usageHistoryService.findByApiKeyId(
                testApiKeyId, PageRequest.of(0, 10));

        assertThat(history.getContent()).hasSize(5);
    }

    @Test
    void 기간별_사용_이력_조회() {
        // given
        LocalDateTime now = LocalDateTime.now();
        usageHistoryService.recordUsage(testApiKeyId, "/api/1", "GET", "192.168.0.1", "UA", 200, true, null);

        // when
        List<ApiKeyUsageHistoryDTO> history = usageHistoryService.findByApiKeyIdAndDateRange(
                testApiKeyId,
                now.minusHours(1),
                now.plusHours(1)
        );

        // then
        assertThat(history).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    void 성공_이력만_조회() {
        // given
        usageHistoryService.recordUsage(testApiKeyId, "/api/1", "GET", "192.168.0.1", "UA", 200, true, null);
        usageHistoryService.recordUsage(testApiKeyId, "/api/2", "GET", "192.168.0.1", "UA", 401, false, "Fail");
        usageHistoryService.recordUsage(testApiKeyId, "/api/3", "GET", "192.168.0.1", "UA", 200, true, null);

        // when
        Page<ApiKeyUsageHistoryDTO> successHistory = usageHistoryService.findByApiKeyIdAndSuccess(
                testApiKeyId, true, PageRequest.of(0, 10));

        // then
        assertThat(successHistory.getContent()).hasSize(2);
        assertThat(successHistory.getContent()).allMatch(ApiKeyUsageHistoryDTO::getIsSuccess);
    }

    @Test
    void 실패_이력만_조회() {
        // given
        usageHistoryService.recordUsage(testApiKeyId, "/api/1", "GET", "192.168.0.1", "UA", 200, true, null);
        usageHistoryService.recordUsage(testApiKeyId, "/api/2", "GET", "192.168.0.1", "UA", 401, false, "Fail");

        // when
        Page<ApiKeyUsageHistoryDTO> failHistory = usageHistoryService.findByApiKeyIdAndSuccess(
                testApiKeyId, false, PageRequest.of(0, 10));

        // then
        assertThat(failHistory.getContent()).hasSize(1);
        assertThat(failHistory.getContent()).allMatch(dto -> !dto.getIsSuccess());
    }

    @Test
    void 전체_사용_이력_조회() {
        // given
        ApiKeyDTO anotherKey = apiKeyService.createApiKey("Another Key", null, null, "user2");
        usageHistoryService.recordUsage(testApiKeyId, "/api/1", "GET", "192.168.0.1", "UA", 200, true, null);
        usageHistoryService.recordUsage(anotherKey.getId(), "/api/2", "GET", "192.168.0.2", "UA", 200, true, null);

        // when
        Page<ApiKeyUsageHistoryDTO> allHistory = usageHistoryService.findAll(PageRequest.of(0, 10));

        // then
        assertThat(allHistory.getContent()).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void 총_사용_횟수_조회() {
        // given
        for (int i = 0; i < 7; i++) {
            usageHistoryService.recordUsage(testApiKeyId, "/api/" + i, "GET", "192.168.0.1", "UA", 200, true, null);
        }

        // when
        Long count = usageHistoryService.countByApiKeyId(testApiKeyId);

        // then
        assertThat(count).isEqualTo(7L);
    }

    @Test
    void 성공_실패_횟수_조회() {
        // given
        usageHistoryService.recordUsage(testApiKeyId, "/api/1", "GET", "192.168.0.1", "UA", 200, true, null);
        usageHistoryService.recordUsage(testApiKeyId, "/api/2", "GET", "192.168.0.1", "UA", 200, true, null);
        usageHistoryService.recordUsage(testApiKeyId, "/api/3", "GET", "192.168.0.1", "UA", 401, false, "Fail");

        // when
        Long successCount = usageHistoryService.countByApiKeyIdAndSuccess(testApiKeyId, true);
        Long failCount = usageHistoryService.countByApiKeyIdAndSuccess(testApiKeyId, false);

        // then
        assertThat(successCount).isEqualTo(2L);
        assertThat(failCount).isEqualTo(1L);
    }

    @Test
    void 다양한_HTTP_메서드_기록() {
        // when
        usageHistoryService.recordUsage(testApiKeyId, "/api/resource", "GET", "192.168.0.1", "UA", 200, true, null);
        usageHistoryService.recordUsage(testApiKeyId, "/api/resource", "POST", "192.168.0.1", "UA", 201, true, null);
        usageHistoryService.recordUsage(testApiKeyId, "/api/resource", "PUT", "192.168.0.1", "UA", 200, true, null);
        usageHistoryService.recordUsage(testApiKeyId, "/api/resource", "DELETE", "192.168.0.1", "UA", 204, true, null);

        // then
        Page<ApiKeyUsageHistoryDTO> history = usageHistoryService.findByApiKeyId(
                testApiKeyId, PageRequest.of(0, 10));

        assertThat(history.getContent()).hasSize(4);
        assertThat(history.getContent()).extracting(ApiKeyUsageHistoryDTO::getRequestMethod)
                .containsExactlyInAnyOrder("GET", "POST", "PUT", "DELETE");
    }

    @Test
    void 다양한_IP_주소_기록() {
        // when
        usageHistoryService.recordUsage(testApiKeyId, "/api/test", "GET", "192.168.0.1", "UA", 200, true, null);
        usageHistoryService.recordUsage(testApiKeyId, "/api/test", "GET", "192.168.0.2", "UA", 200, true, null);
        usageHistoryService.recordUsage(testApiKeyId, "/api/test", "GET", "10.0.0.1", "UA", 200, true, null);

        // then
        Page<ApiKeyUsageHistoryDTO> history = usageHistoryService.findByApiKeyId(
                testApiKeyId, PageRequest.of(0, 10));

        assertThat(history.getContent()).hasSize(3);
        assertThat(history.getContent()).extracting(ApiKeyUsageHistoryDTO::getIpAddress)
                .containsExactlyInAnyOrder("192.168.0.1", "192.168.0.2", "10.0.0.1");
    }

    @Test
    void 페이징_처리() {
        // given
        for (int i = 0; i < 25; i++) {
            usageHistoryService.recordUsage(testApiKeyId, "/api/" + i, "GET", "192.168.0.1", "UA", 200, true, null);
        }

        // when
        Page<ApiKeyUsageHistoryDTO> page1 = usageHistoryService.findByApiKeyId(
                testApiKeyId, PageRequest.of(0, 10));
        Page<ApiKeyUsageHistoryDTO> page2 = usageHistoryService.findByApiKeyId(
                testApiKeyId, PageRequest.of(1, 10));

        // then
        assertThat(page1.getContent()).hasSize(10);
        assertThat(page2.getContent()).hasSize(10);
        assertThat(page1.getTotalElements()).isEqualTo(25L);
    }
}
