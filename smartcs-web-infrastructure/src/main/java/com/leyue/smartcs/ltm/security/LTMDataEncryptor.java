package com.leyue.smartcs.ltm.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

/**
 * LTM数据加密器
 * 提供记忆数据的加密和解密功能
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LTMDataEncryptor {

    @Value("${smartcs.ai.ltm.security.encryption.enabled:true}")
    private boolean encryptionEnabled;

    @Value("${smartcs.secrets.activeKid:dev-key-2024}")
    private String activeKid;

    @Value("${smartcs.secrets.algorithm:AES}")
    private String algorithm;

    @Value("${smartcs.secrets.transformation:AES/GCM/NoPadding}")
    private String transformation;

    @Value("${smartcs.secrets.ivLength:12}")
    private int ivLength;

    @Value("${smartcs.secrets.tagLength:128}")
    private int tagLength;

    @Value("#{${smartcs.secrets.keys}}")
    private Map<String, String> keyMap;

    private final SecureRandom secureRandom = new SecureRandom();

    // 加密内容标识前缀
    private static final String ENCRYPTED_PREFIX = "LTM_ENC:";

    /**
     * 加密敏感内容
     */
    public String encrypt(String plaintext) {
        if (!encryptionEnabled || plaintext == null || plaintext.isEmpty()) {
            return plaintext;
        }

        try {
            // 获取密钥
            String keyBase64 = keyMap.get(activeKid);
            if (keyBase64 == null) {
                log.warn("找不到密钥: kidx={}", activeKid);
                return plaintext;
            }

            byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, algorithm);

            // 生成随机IV
            byte[] iv = new byte[ivLength];
            secureRandom.nextBytes(iv);

            // 初始化cipher
            Cipher cipher = Cipher.getInstance(transformation);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(tagLength, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);

            // 加密
            byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);
            byte[] ciphertext = cipher.doFinal(plaintextBytes);

            // 组合结果：kid + iv + ciphertext
            String result = ENCRYPTED_PREFIX + activeKid + ":" + 
                          Base64.getEncoder().encodeToString(iv) + ":" +
                          Base64.getEncoder().encodeToString(ciphertext);

            log.debug("内容加密成功: length={}", plaintext.length());
            return result;

        } catch (Exception e) {
            log.error("内容加密失败: error={}", e.getMessage());
            return plaintext; // 加密失败时返回原文
        }
    }

    /**
     * 解密敏感内容
     */
    public String decrypt(String ciphertext) {
        if (!encryptionEnabled || !isEncrypted(ciphertext)) {
            return ciphertext;
        }

        try {
            // 解析加密格式：LTM_ENC:kid:iv:ciphertext
            String encryptedData = ciphertext.substring(ENCRYPTED_PREFIX.length());
            String[] parts = encryptedData.split(":", 3);
            
            if (parts.length != 3) {
                log.warn("加密格式无效");
                return ciphertext;
            }

            String kid = parts[0];
            byte[] iv = Base64.getDecoder().decode(parts[1]);
            byte[] encrypted = Base64.getDecoder().decode(parts[2]);

            // 获取密钥
            String keyBase64 = keyMap.get(kid);
            if (keyBase64 == null) {
                log.warn("找不到解密密钥: kid={}", kid);
                return ciphertext;
            }

            byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, algorithm);

            // 初始化cipher
            Cipher cipher = Cipher.getInstance(transformation);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(tagLength, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);

            // 解密
            byte[] plaintext = cipher.doFinal(encrypted);
            String result = new String(plaintext, StandardCharsets.UTF_8);

            log.debug("内容解密成功: length={}", result.length());
            return result;

        } catch (Exception e) {
            log.error("内容解密失败: error={}", e.getMessage());
            return ciphertext; // 解密失败时返回密文
        }
    }

    /**
     * 检查内容是否已加密
     */
    public boolean isEncrypted(String content) {
        return content != null && content.startsWith(ENCRYPTED_PREFIX);
    }

    /**
     * 批量加密
     */
    public Map<String, String> batchEncrypt(Map<String, String> data) {
        if (!encryptionEnabled || data == null) {
            return data;
        }

        return data.entrySet().stream()
            .collect(java.util.stream.Collectors.toMap(
                Map.Entry::getKey,
                entry -> encrypt(entry.getValue())
            ));
    }

    /**
     * 批量解密
     */
    public Map<String, String> batchDecrypt(Map<String, String> data) {
        if (!encryptionEnabled || data == null) {
            return data;
        }

        return data.entrySet().stream()
            .collect(java.util.stream.Collectors.toMap(
                Map.Entry::getKey,
                entry -> decrypt(entry.getValue())
            ));
    }

    /**
     * 检查加密功能状态
     */
    public EncryptionStatus getStatus() {
        return EncryptionStatus.builder()
            .enabled(encryptionEnabled)
            .activeKid(activeKid)
            .algorithm(algorithm)
            .transformation(transformation)
            .availableKeys(keyMap.keySet())
            .build();
    }

    /**
     * 加密状态信息
     */
    @lombok.Data
    @lombok.Builder
    public static class EncryptionStatus {
        private boolean enabled;
        private String activeKid;
        private String algorithm;
        private String transformation;
        private java.util.Set<String> availableKeys;
    }
}