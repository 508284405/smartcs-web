package com.leyue.smartcs.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT配置类
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    
    /**
     * 公钥字符串，用于验证JWT签名
     */
    private String publicKey;
    
    /**
     * 时间漂移容忍度（秒），默认60秒
     */
    private long clockSkew = 60L;
} 