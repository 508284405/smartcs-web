package com.leyue.smartcs.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SecretConfig 配置测试
 * 验证密钥配置是否能够正确加载
 */
@SpringBootTest
@ActiveProfiles("dev")
class SecretConfigTest {

    @Autowired
    private SecretConfig secretConfig;

    @Test
    void testSecretConfigLoaded() {
        // 验证配置不为空
        assertNotNull(secretConfig, "SecretConfig 应该被正确注入");
        
        // 验证基本配置
        assertNotNull(secretConfig.getActiveKid(), "activeKid 不应为空");
        assertEquals("dev-key-2024", secretConfig.getActiveKid(), "activeKid 应该是 dev-key-2024");
        
        // 验证算法配置
        assertEquals("AES", secretConfig.getAlgorithm(), "算法应该是 AES");
        assertEquals("AES/GCM/NoPadding", secretConfig.getTransformation(), "转换模式应该是 AES/GCM/NoPadding");
        assertEquals(12, secretConfig.getIvLength(), "IV长度应该是 12");
        assertEquals(128, secretConfig.getTagLength(), "标签长度应该是 128");
        
        // 验证密钥映射
        assertNotNull(secretConfig.getKeys(), "密钥映射不应为空");
        assertTrue(secretConfig.getKeys().containsKey("dev-key-2024"), "应该包含开发密钥");
        assertEquals("7zMZ79gIhnvzDE+BpCc/kCyDWn/xU7Ku3YXvh7eqk10=", 
                     secretConfig.getKeys().get("dev-key-2024"), "开发密钥值应该正确");
        
        System.out.println("✅ SecretConfig 配置加载成功:");
        System.out.println("  - activeKid: " + secretConfig.getActiveKid());
        System.out.println("  - algorithm: " + secretConfig.getAlgorithm());
        System.out.println("  - keys count: " + secretConfig.getKeys().size());
    }
}