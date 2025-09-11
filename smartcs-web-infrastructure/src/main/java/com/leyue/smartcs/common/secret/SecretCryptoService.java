package com.leyue.smartcs.common.secret;

import com.leyue.smartcs.config.SecretConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 密钥加密解密服务
 * 使用AES-GCM模式对敏感信息进行加密，支持密钥轮换
 * 
 * @author Claude
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SecretCryptoService {
    
    private final SecretConfig secretConfig;
    private final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * 加密结果封装类
     */
    public static class EncryptResult {
        private final byte[] ciphertext;
        private final byte[] iv;
        private final String kid;
        
        public EncryptResult(byte[] ciphertext, byte[] iv, String kid) {
            this.ciphertext = ciphertext;
            this.iv = iv;
            this.kid = kid;
        }
        
        public byte[] getCiphertext() { return ciphertext; }
        public byte[] getIv() { return iv; }
        public String getKid() { return kid; }
    }
    
    /**
     * 为模型提供商API Key加密
     * 
     * @param plainApiKey 明文API Key
     * @return 加密结果
     */
    public EncryptResult encryptForProviderApiKey(String plainApiKey) {
        if (plainApiKey == null || plainApiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API Key不能为空");
        }
        
        try {
            // 获取当前活跃密钥
            String activeKid = secretConfig.getActiveKid();
            SecretKey secretKey = getSecretKey(activeKid);
            
            // 生成随机IV
            byte[] iv = new byte[secretConfig.getIvLength()];
            secureRandom.nextBytes(iv);
            
            // 执行加密
            Cipher cipher = Cipher.getInstance(secretConfig.getTransformation());
            GCMParameterSpec gcmSpec = new GCMParameterSpec(secretConfig.getTagLength(), iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);
            
            byte[] ciphertext = cipher.doFinal(plainApiKey.getBytes(StandardCharsets.UTF_8));
            
            log.debug("API Key已加密，密钥ID: {}, 密文长度: {}", activeKid, ciphertext.length);
            return new EncryptResult(ciphertext, iv, activeKid);
            
        } catch (Exception e) {
            log.error("API Key加密失败", e);
            throw new RuntimeException("API Key加密失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 解密模型提供商API Key
     * 
     * @param ciphertext 密文
     * @param iv 初始化向量
     * @param kid 密钥ID
     * @return 解密后的明文API Key
     */
    public String decryptProviderApiKey(byte[] ciphertext, byte[] iv, String kid) {
        if (ciphertext == null || ciphertext.length == 0) {
            throw new IllegalArgumentException("密文不能为空");
        }
        if (iv == null || iv.length == 0) {
            throw new IllegalArgumentException("初始化向量不能为空");
        }
        if (kid == null || kid.trim().isEmpty()) {
            throw new IllegalArgumentException("密钥ID不能为空");
        }
        
        try {
            // 获取指定密钥
            SecretKey secretKey = getSecretKey(kid);
            
            // 执行解密
            Cipher cipher = Cipher.getInstance(secretConfig.getTransformation());
            GCMParameterSpec gcmSpec = new GCMParameterSpec(secretConfig.getTagLength(), iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);
            
            byte[] plaintext = cipher.doFinal(ciphertext);
            String result = new String(plaintext, StandardCharsets.UTF_8);
            
            log.debug("API Key已解密，密钥ID: {}", kid);
            return result;
            
        } catch (Exception e) {
            log.error("API Key解密失败，密钥ID: {}", kid, e);
            throw new RuntimeException("API Key解密失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 检查密文是否有效（能否正常解密）
     * 
     * @param ciphertext 密文
     * @param iv 初始化向量
     * @param kid 密钥ID
     * @return 是否有效
     */
    public boolean isValidCiphertext(byte[] ciphertext, byte[] iv, String kid) {
        try {
            decryptProviderApiKey(ciphertext, iv, kid);
            return true;
        } catch (Exception e) {
            log.warn("密文验证失败，密钥ID: {}", kid, e);
            return false;
        }
    }
    
    /**
     * 生成用于测试的密钥（Base64编码）
     * 仅用于开发和测试环境
     * 
     * @return Base64编码的AES密钥
     */
    public static String generateTestKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256); // AES-256
            SecretKey secretKey = keyGen.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("密钥生成失败", e);
        }
    }

    public static void main(String[] args) {
        System.out.println(generateTestKey());
    }
    
    /**
     * 获取密钥对象
     * 
     * @param kid 密钥ID
     * @return 密钥对象
     */
    private SecretKey getSecretKey(String kid) {
        String keyBase64 = secretConfig.getKeyById(kid);
        byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
        return new SecretKeySpec(keyBytes, secretConfig.getAlgorithm());
    }
}