package com.leyue.smartcs.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * 密钥管理配置类，用于API Key等敏感信息的加密配置
 * 支持本地密钥托管，便于后期切换至KeyStore/Jasypt/Vault等方案
 * 
 * @author Claude
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "smartcs.secrets")
public class SecretConfig {
    
    /**
     * 当前使用的密钥ID（用于密钥轮换）
     */
    private String activeKid = "prod-key-2024";
    
    /**
     * 密钥映射表：密钥ID -> 密钥值（Base64编码）
     * 支持多个密钥存在，便于密钥轮换
     */
    private Map<String, String> keys;
    
    /**
     * 加密算法，默认使用AES-GCM
     */
    private String algorithm = "AES";
    
    /**
     * AES-GCM加密模式
     */
    private String transformation = "AES/GCM/NoPadding";
    
    /**
     * GCM模式IV长度（字节）
     */
    private int ivLength = 12;
    
    /**
     * GCM认证标签长度（位）
     */
    private int tagLength = 128;
    
    /**
     * 获取当前激活的密钥
     * 
     * @return 当前激活的密钥（Base64编码）
     */
    public String getActiveKey() {
        if (keys == null || keys.isEmpty()) {
            throw new IllegalStateException("未配置加密密钥");
        }
        
        String key = keys.get(activeKid);
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalStateException("未找到密钥ID为 " + activeKid + " 的密钥");
        }
        
        return key;
    }
    
    /**
     * 根据密钥ID获取密钥
     * 
     * @param kid 密钥ID
     * @return 密钥值（Base64编码）
     */
    public String getKeyById(String kid) {
        if (keys == null || keys.isEmpty()) {
            throw new IllegalStateException("未配置加密密钥");
        }
        
        String key = keys.get(kid);
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalStateException("未找到密钥ID为 " + kid + " 的密钥");
        }
        
        return key;
    }
    
    /**
     * 检查是否存在指定的密钥ID
     * 
     * @param kid 密钥ID
     * @return 是否存在
     */
    public boolean hasKey(String kid) {
        return keys != null && keys.containsKey(kid) 
               && keys.get(kid) != null && !keys.get(kid).trim().isEmpty();
    }
}