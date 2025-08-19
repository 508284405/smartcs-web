package com.leyue.smartcs.common.secret;

import com.leyue.smartcs.config.SecretConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * SecretCryptoService 测试类
 */
@ExtendWith(MockitoExtension.class)
class SecretCryptoServiceTest {
    
    @Mock
    private SecretConfig secretConfig;
    
    private SecretCryptoService secretCryptoService;
    
    // 测试密钥（Base64编码的256位AES密钥）
    private static final String TEST_KEY = "dGVzdC1rZXktZm9yLXVuaXQtdGVzdC0yNTYtYml0cw==";
    private static final String TEST_KID = "test-key";
    
    @BeforeEach
    void setUp() {
        // 模拟配置
        Map<String, String> keys = new HashMap<>();
        keys.put(TEST_KID, TEST_KEY);
        
        when(secretConfig.getActiveKid()).thenReturn(TEST_KID);
        when(secretConfig.getKeyById(TEST_KID)).thenReturn(TEST_KEY);
        when(secretConfig.hasKey(TEST_KID)).thenReturn(true);
        when(secretConfig.getAlgorithm()).thenReturn("AES");
        when(secretConfig.getTransformation()).thenReturn("AES/GCM/NoPadding");
        when(secretConfig.getIvLength()).thenReturn(12);
        when(secretConfig.getTagLength()).thenReturn(128);
        
        secretCryptoService = new SecretCryptoService(secretConfig);
    }
    
    @Test
    void testEncryptAndDecrypt() {
        // 测试数据
        String originalApiKey = "sk-test-api-key-1234567890abcdef";
        
        // 执行加密
        SecretCryptoService.EncryptResult encryptResult = 
            secretCryptoService.encryptForProviderApiKey(originalApiKey);
        
        // 验证加密结果
        assertNotNull(encryptResult);
        assertNotNull(encryptResult.getCiphertext());
        assertNotNull(encryptResult.getIv());
        assertEquals(TEST_KID, encryptResult.getKid());
        assertTrue(encryptResult.getCiphertext().length > 0);
        assertTrue(encryptResult.getIv().length > 0);
        
        // 执行解密
        String decryptedApiKey = secretCryptoService.decryptProviderApiKey(
            encryptResult.getCiphertext(),
            encryptResult.getIv(),
            encryptResult.getKid()
        );
        
        // 验证解密结果
        assertEquals(originalApiKey, decryptedApiKey);
    }
    
    @Test
    void testEncryptEmptyString() {
        // 测试空字符串加密
        assertThrows(IllegalArgumentException.class, () -> {
            secretCryptoService.encryptForProviderApiKey("");
        });
    }
    
    @Test
    void testEncryptNullString() {
        // 测试null加密
        assertThrows(IllegalArgumentException.class, () -> {
            secretCryptoService.encryptForProviderApiKey(null);
        });
    }
    
    @Test
    void testDecryptInvalidCiphertext() {
        // 测试解密无效密文
        assertThrows(RuntimeException.class, () -> {
            secretCryptoService.decryptProviderApiKey(
                new byte[]{1, 2, 3}, 
                new byte[]{4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15},
                TEST_KID
            );
        });
    }
    
    @Test
    void testDecryptInvalidKid() {
        // 先加密一个正常的API Key
        String originalApiKey = "sk-test-api-key";
        SecretCryptoService.EncryptResult encryptResult = 
            secretCryptoService.encryptForProviderApiKey(originalApiKey);
        
        // 使用错误的KID解密
        assertThrows(RuntimeException.class, () -> {
            secretCryptoService.decryptProviderApiKey(
                encryptResult.getCiphertext(),
                encryptResult.getIv(),
                "invalid-kid"
            );
        });
    }
    
    @Test
    void testIsValidCiphertext() {
        // 测试有效密文验证
        String originalApiKey = "sk-test-api-key";
        SecretCryptoService.EncryptResult encryptResult = 
            secretCryptoService.encryptForProviderApiKey(originalApiKey);
        
        assertTrue(secretCryptoService.isValidCiphertext(
            encryptResult.getCiphertext(),
            encryptResult.getIv(),
            encryptResult.getKid()
        ));
        
        // 测试无效密文验证
        assertFalse(secretCryptoService.isValidCiphertext(
            new byte[]{1, 2, 3},
            new byte[]{4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15},
            TEST_KID
        ));
    }
    
    @Test
    void testMultipleEncryptionsProduceDifferentCiphertexts() {
        // 测试同一明文多次加密产生不同密文（由于随机IV）
        String apiKey = "sk-test-same-content";
        
        SecretCryptoService.EncryptResult result1 = 
            secretCryptoService.encryptForProviderApiKey(apiKey);
        SecretCryptoService.EncryptResult result2 = 
            secretCryptoService.encryptForProviderApiKey(apiKey);
        
        // 密文应该不同（因为IV不同）
        assertNotEquals(result1.getCiphertext(), result2.getCiphertext());
        assertNotEquals(result1.getIv(), result2.getIv());
        
        // 但解密结果应该相同
        String decrypted1 = secretCryptoService.decryptProviderApiKey(
            result1.getCiphertext(), result1.getIv(), result1.getKid());
        String decrypted2 = secretCryptoService.decryptProviderApiKey(
            result2.getCiphertext(), result2.getIv(), result2.getKid());
        
        assertEquals(apiKey, decrypted1);
        assertEquals(apiKey, decrypted2);
    }
    
    @Test
    void testGenerateTestKey() {
        // 测试密钥生成
        String generatedKey = SecretCryptoService.generateTestKey();
        
        assertNotNull(generatedKey);
        assertFalse(generatedKey.isEmpty());
        
        // 测试生成的密钥应该可以用于加密解密
        Map<String, String> keys = new HashMap<>();
        keys.put("generated-key", generatedKey);
        
        when(secretConfig.getActiveKid()).thenReturn("generated-key");
        when(secretConfig.getKeyById("generated-key")).thenReturn(generatedKey);
        when(secretConfig.hasKey("generated-key")).thenReturn(true);
        
        SecretCryptoService testService = new SecretCryptoService(secretConfig);
        
        String testApiKey = "sk-generated-key-test";
        SecretCryptoService.EncryptResult result = testService.encryptForProviderApiKey(testApiKey);
        String decrypted = testService.decryptProviderApiKey(result.getCiphertext(), result.getIv(), result.getKid());
        
        assertEquals(testApiKey, decrypted);
    }
}