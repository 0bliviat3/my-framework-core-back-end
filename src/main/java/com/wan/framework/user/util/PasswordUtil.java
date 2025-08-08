package com.wan.framework.user.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class PasswordUtil {

    private PasswordUtil() {
    }

    public static String hashPassword(String plainPassword, String salt, int iterationCount) {
        try {
            PBEKeySpec spec = new PBEKeySpec(
                    plainPassword.toCharArray(),
                    salt.getBytes(StandardCharsets.UTF_8),
                    iterationCount,
                    256 // 256-bit key
            );

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            //TODO: exception 처리
            throw new RuntimeException("Password hash generation failed", e);
        }
    }

    public static boolean verifyPassword(String plainPassword, String salt, int iterationCount, String expectedHash) {
        String hashedInput = hashPassword(plainPassword, salt, iterationCount);
        return hashedInput.equals(expectedHash);
    }


}
