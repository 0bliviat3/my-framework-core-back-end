package com.wan.framework.user.service;

import com.wan.framework.user.exception.UserException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

import static com.wan.framework.user.constant.UserExceptionMessage.FAIL_CREATE_SALT;
import static com.wan.framework.user.constant.UserExceptionMessage.FAIL_HASH_PASSWORD;

@Service
@Slf4j
@RequiredArgsConstructor
public class PasswordService {

    @Value("${password.salt.size}")
    private Integer saltSize;

    @Value("${password.iteration}")
    private Integer passwordIteration;

    // ---------- Utility: Salt & Hash ----------

    public String generateSaltBase64() {
        try {
            byte[] salt = SecureRandom.getInstanceStrong().generateSeed(saltSize);
            return Base64.getEncoder().encodeToString(salt);
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to generate salt", e);
            throw new UserException(FAIL_CREATE_SALT, e);
        }
    }

    /**
     * PBKDF2WithHmacSHA256 해시 생성
     *
     * @param plainPassword  원문 비밀번호
     * @param saltBase64     Base64 인코딩된 솔트
     * @return Base64 인코딩된 해시 문자열
     */
    public String hashPassword(String plainPassword, String saltBase64) {
        if (plainPassword == null || saltBase64 == null) {
            throw new UserException(FAIL_HASH_PASSWORD);
        }
        try {
            byte[] salt = Base64.getDecoder().decode(saltBase64);
            KeySpec spec = new PBEKeySpec(plainPassword.toCharArray(), salt, this.passwordIteration, 256);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.error("Password hashing failed", e);
            throw new UserException(FAIL_HASH_PASSWORD, e);
        }
    }

    public boolean verifyPassword(String plainPassword, String saltBase64, String expectedHashBase64) {
        String hashedInput = hashPassword(plainPassword, saltBase64);
        byte[] a = Base64.getDecoder().decode(hashedInput);
        byte[] b = Base64.getDecoder().decode(expectedHashBase64);
        return MessageDigest.isEqual(a, b);
    }
}
