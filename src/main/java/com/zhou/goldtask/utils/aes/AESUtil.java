package com.zhou.goldtask.utils.aes;

import javax.crypto.Cipher;
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
}