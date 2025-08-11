package com.wan.framework.user.service;

import com.wan.framework.user.exception.UserException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static com.wan.framework.user.constant.UserExceptionMessage.FAIL_HASH_PASSWORD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@TestPropertySource(properties = {
        "password.salt.size=16",
        "password.iteration=10000"
})
public class PasswordServiceTest {

    @Autowired
    private PasswordService passwordService;

    @Test
    void generateSaltTest() {
        String salt = passwordService.generateSaltBase64();
        assertThat(salt).isNotNull();
        assertThat(salt.length()).isEqualTo(24);
    }

    @Test
    void compareHashWord() {
        String password = "qwer1234";
        String salt = passwordService.generateSaltBase64();

        String hash1 = passwordService.hashPassword(password, salt);
        String hash2 = passwordService.hashPassword(password, salt);

        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    void verifyPassWordTest() {
        String salt = passwordService.generateSaltBase64();
        String password = "qwer1234";
        String hashWord = passwordService.hashPassword(password, salt);

        boolean verified = passwordService.verifyPassword(password, salt, hashWord);

        assertThat(verified).isTrue();
    }

    @Test
    void 비밀번호_다른_경우() {
        String salt = passwordService.generateSaltBase64();
        String originPass = "qwer1234";
        String inputPass = "qwer1236";

        String hashed = passwordService.hashPassword(originPass, salt);

        boolean verified = passwordService.verifyPassword(inputPass, salt, hashed);

        assertThat(verified).isFalse();
    }

    @Test
    void 비밀번호_생성중_null값_포함시_에러발생() {
        assertThatThrownBy(() -> passwordService.hashPassword(null, "aaaaaa"))
                .isInstanceOf(UserException.class)
                .hasMessageContaining(FAIL_HASH_PASSWORD.getMessage());
    }

}
