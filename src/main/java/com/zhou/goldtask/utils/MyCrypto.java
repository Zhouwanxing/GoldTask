package com.zhou.goldtask.utils;

import com.zhou.goldtask.entity.EnvConfig;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Base64;

@Component
public class MyCrypto {
    @Resource
    private EnvConfig envConfig;

    public String encrypt(String input) {
        if (input == null || "".equals(input)) {
            return "";
        }
        byte[] keyBytes = envConfig.getMyKey().getBytes();
        byte[] inputBytes = input.getBytes();
        byte[] result = new byte[inputBytes.length];

        for (int i = 0; i < inputBytes.length; i++) {
            result[i] = (byte) (inputBytes[i] ^ keyBytes[i % keyBytes.length]);
        }

        return Base64.getEncoder().encodeToString(result);
    }

    public String decrypt(String base64Input) {
        byte[] keyBytes = envConfig.getMyKey().getBytes();
        byte[] inputBytes = Base64.getDecoder().decode(base64Input);
        byte[] result = new byte[inputBytes.length];

        for (int i = 0; i < inputBytes.length; i++) {
            result[i] = (byte) (inputBytes[i] ^ keyBytes[i % keyBytes.length]);
        }
        return new String(result);
    }
}