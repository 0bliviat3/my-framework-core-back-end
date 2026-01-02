package com.wan.framework.apikey.service;

import com.wan.framework.apikey.dto.ApiKeyDTO;
import com.wan.framework.apikey.dto.ApiKeyPermissionDTO;
import com.wan.framework.apikey.exception.ApiKeyException;
import com.wan.framework.base.constant.AbleState;
import com.wan.framework.base.constant.DataStateCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static com.wan.framework.apikey.constant.ApiKeyExceptionMessage.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class ApiKeyServiceTest {

    @Autowired
    private ApiKeyService service;

    @Test
    void API_Key_생성_성공() {
        // given
        String description = "테스트 API Key";
        LocalDateTime expiredAt = LocalDateTime.now().plusDays(30);
        List<String> permissions = Arrays.asList("board:read", "board:write");

        // when
        ApiKeyDTO created = service.createApiKey(description, expiredAt, permissions, "user1");

        // then
        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getRawApiKey()).isNotNull(); // 최초 생성시에만 반환
        assertThat(created.getRawApiKey()).startsWith("sk_");
        assertThat(created.getApiKeyPrefix()).isNotNull();
        assertThat(created.getDescription()).isEqualTo(description);
        assertThat(created.getExpiredAt()).isEqualTo(expiredAt);
        assertThat(created.getUsageCount()).isZero();
        assertThat(created.getAbleState()).isEqualTo(AbleState.ABLE);
        assertThat(created.getPermissions()).hasSize(2);
        assertThat(created.getPermissions()).containsExactlyInAnyOrder("board:read", "board:write");
    }

    @Test
    void API_Key_생성_만료일_없이() {
        // given
        String description = "만료일 없는 API Key";

        // when
        ApiKeyDTO created = service.createApiKey(description, null, null, "user1");

        // then
        assertThat(created).isNotNull();
        assertThat(created.getExpiredAt()).isNull();
    }

    @Test
    void API_Key_검증_성공() {
        // given
        ApiKeyDTO created = service.createApiKey("테스트", null, Arrays.asList("test:read"), "user1");
        String rawApiKey = created.getRawApiKey();

        // when
        ApiKeyDTO validated = service.validateApiKey(rawApiKey);

        // then
        assertThat(validated).isNotNull();
        assertThat(validated.getId()).isEqualTo(created.getId());
        assertThat(validated.getPermissions()).containsExactly("test:read");
    }

    @Test
    void 잘못된_형식의_API_Key_검증시_예외발생() {
        // when & then
        assertThatThrownBy(() -> service.validateApiKey("invalid-key"))
                .isInstanceOf(ApiKeyException.class)
                .hasMessageContaining(INVALID_API_KEY.getMessage());
    }

    @Test
    void 존재하지_않는_API_Key_검증시_예외발생() {
        // given
        String fakeApiKey = "sk_" + "A".repeat(43); // 올바른 형식이지만 존재하지 않는 키

        // when & then
        assertThatThrownBy(() -> service.validateApiKey(fakeApiKey))
                .isInstanceOf(ApiKeyException.class)
                .hasMessageContaining(NOT_FOUND_API_KEY.getMessage());
    }

    @Test
    void 비활성화된_API_Key_검증시_예외발생() {
        // given
        ApiKeyDTO created = service.createApiKey("테스트", null, null, "user1");
        String rawApiKey = created.getRawApiKey();
        service.toggleApiKey(created.getId(), AbleState.DISABLE, "admin");

        // when & then
        assertThatThrownBy(() -> service.validateApiKey(rawApiKey))
                .isInstanceOf(ApiKeyException.class)
                .hasMessageContaining(DISABLED_API_KEY.getMessage());
    }

    @Test
    void 만료된_API_Key_검증시_예외발생() {
        // given
        LocalDateTime expiredAt = LocalDateTime.now().minusDays(1); // 이미 만료
        ApiKeyDTO created = service.createApiKey("만료 테스트", expiredAt, null, "user1");
        String rawApiKey = created.getRawApiKey();

        // when & then
        assertThatThrownBy(() -> service.validateApiKey(rawApiKey))
                .isInstanceOf(ApiKeyException.class)
                .hasMessageContaining(EXPIRED_API_KEY.getMessage());
    }

    @Test
    void API_Key_활성화() {
        // given
        ApiKeyDTO created = service.createApiKey("테스트", null, null, "user1");
        service.toggleApiKey(created.getId(), AbleState.DISABLE, "admin");

        // when
        service.toggleApiKey(created.getId(), AbleState.ABLE, "admin");

        // then
        ApiKeyDTO found = service.findById(created.getId());
        assertThat(found.getAbleState()).isEqualTo(AbleState.ABLE);
    }

    @Test
    void API_Key_비활성화() {
        // given
        ApiKeyDTO created = service.createApiKey("테스트", null, null, "user1");

        // when
        service.toggleApiKey(created.getId(), AbleState.DISABLE, "admin");

        // then
        ApiKeyDTO found = service.findById(created.getId());
        assertThat(found.getAbleState()).isEqualTo(AbleState.DISABLE);
    }

    @Test
    void API_Key_논리_삭제() {
        // given
        ApiKeyDTO created = service.createApiKey("삭제 테스트", null, null, "user1");

        // when
        service.deleteApiKey(created.getId(), "admin");

        // then
        assertThatThrownBy(() -> service.findById(created.getId()))
                .isInstanceOf(ApiKeyException.class)
                .hasMessageContaining(NOT_FOUND_API_KEY.getMessage());
    }

    @Test
    void API_Key_목록_조회() {
        // given
        service.createApiKey("Key 1", null, null, "user1");
        service.createApiKey("Key 2", null, null, "user1");
        service.createApiKey("Key 3", null, null, "user2");

        // when
        Page<ApiKeyDTO> page = service.findAllApiKeys(PageRequest.of(0, 10));

        // then
        assertThat(page.getContent()).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    void 내_API_Key_목록_조회() {
        // given
        service.createApiKey("My Key 1", null, null, "user1");
        service.createApiKey("My Key 2", null, null, "user1");
        service.createApiKey("Other Key", null, null, "user2");

        // when
        Page<ApiKeyDTO> page = service.findMyApiKeys("user1", PageRequest.of(0, 10));

        // then
        assertThat(page.getContent()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(page.getContent()).allMatch(dto -> dto.getCreatedBy().equals("user1"));
    }

    @Test
    void API_Key_단건_조회() {
        // given
        ApiKeyDTO created = service.createApiKey("단건 조회 테스트", null, Arrays.asList("perm1"), "user1");

        // when
        ApiKeyDTO found = service.findById(created.getId());

        // then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getDescription()).isEqualTo("단건 조회 테스트");
        assertThat(found.getPermissions()).containsExactly("perm1");
    }

    @Test
    void 권한_추가() {
        // given
        ApiKeyDTO created = service.createApiKey("권한 테스트", null, null, "user1");

        // when
        service.addPermission(created.getId(), "new:permission", "user1");

        // then
        List<ApiKeyPermissionDTO> permissions = service.findPermissions(created.getId());
        assertThat(permissions).hasSize(1);
        assertThat(permissions.get(0).getPermission()).isEqualTo("new:permission");
    }

    @Test
    void 중복_권한_추가시_예외발생() {
        // given
        ApiKeyDTO created = service.createApiKey("중복 테스트", null, Arrays.asList("perm1"), "user1");

        // when & then
        assertThatThrownBy(() -> service.addPermission(created.getId(), "perm1", "user1"))
                .isInstanceOf(ApiKeyException.class)
                .hasMessageContaining(DUPLICATE_PERMISSION.getMessage());
    }

    @Test
    void 권한_제거() {
        // given
        ApiKeyDTO created = service.createApiKey("권한 제거 테스트", null, Arrays.asList("perm1", "perm2"), "user1");

        // when
        service.removePermission(created.getId(), "perm1");

        // then
        List<ApiKeyPermissionDTO> permissions = service.findPermissions(created.getId());
        assertThat(permissions).hasSize(1);
        assertThat(permissions.get(0).getPermission()).isEqualTo("perm2");
    }

    @Test
    void 사용_횟수_증가() {
        // given
        ApiKeyDTO created = service.createApiKey("사용 횟수 테스트", null, null, "user1");

        // when
        service.incrementUsageCount(created.getId());
        service.incrementUsageCount(created.getId());
        service.incrementUsageCount(created.getId());

        // then
        ApiKeyDTO found = service.findById(created.getId());
        assertThat(found.getUsageCount()).isEqualTo(3L);
    }

    @Test
    void 여러_권한_동시_생성() {
        // given
        List<String> permissions = Arrays.asList("read:board", "write:board", "delete:board");

        // when
        ApiKeyDTO created = service.createApiKey("다중 권한", null, permissions, "user1");

        // then
        List<ApiKeyPermissionDTO> found = service.findPermissions(created.getId());
        assertThat(found).hasSize(3);
        assertThat(found).extracting(ApiKeyPermissionDTO::getPermission)
                .containsExactlyInAnyOrder("read:board", "write:board", "delete:board");
    }
}
