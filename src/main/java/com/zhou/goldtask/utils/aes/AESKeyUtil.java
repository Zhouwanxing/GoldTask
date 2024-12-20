package com.zhou.goldtask.utils.aes;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class AESKeyUtil {
    private static final String AES = "AES";
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int KEY_SIZE = 128; // AES-128 位密钥
    private static final int ITERATIONS = 100_000;
    private static final int SALT_LENGTH = 16; // 盐长度

    // 生成随机盐（如果需要动态盐）
    public static String generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    // 使用固定字符串生成 AES 密钥
    public static SecretKeySpec deriveKey(String password) throws Exception {
        byte[] salt = "10086QWERTY".getBytes(StandardCharsets.UTF_8); // 固定盐，增强安全性
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_SIZE);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, AES);
    }
}