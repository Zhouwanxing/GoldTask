package com.zhou.goldtask.utils.aes;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AESUtil {
    private static final String AES = "AES";

    public static String encryptByMyKey(String data, String key) throws Exception {
        return encrypt(data, AESKeyUtil.deriveKey(key));
    }

    public static String decryptByMyKey(String data, String key) throws Exception {
        return decrypt(data, AESKeyUtil.deriveKey(key));
    }

    // 加密
    public static String encrypt(String data, SecretKeySpec secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(AES);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    // 解密
    public static String decrypt(String encryptedData, SecretKeySpec secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(AES);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedData = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedData, StandardCharsets.UTF_8);
    }


    private static String normalizeBase64(String f) {
        StringBuilder fBuilder = new StringBuilder(f.replace("-", "+").replace("_", "/"));
        while (fBuilder.length() % 4 != 0) {
            fBuilder.append("=");
        }
        return fBuilder.toString();
    }

    public static String decryptJsonBase64(String f, String key, String iv) throws Exception {
        // 1. 解析密钥和 IV
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] ivBytes = iv.getBytes(StandardCharsets.UTF_8);

        // 2. 解析 Base64 密文
        byte[] encrypted = Base64.getDecoder().decode(normalizeBase64(f));

        // 3. 初始化 AES CBC
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);

        // 4. 解密
        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted, StandardCharsets.UTF_8);
    }
}