package com.leyue.smartcs.common.secret;

import com.leyue.smartcs.model.dataobject.ProviderDO;
import com.leyue.smartcs.model.mapper.ProviderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * API Key迁移工具
 * 用于将现有的明文API Key迁移为加密存储
 * 
 * 使用方法：
 * 1. 在application.yaml中设置 smartcs.migration.encrypt-api-keys=true
 * 2. 确保已正确配置 smartcs.secrets 相关配置
 * 3. 启动应用程序，迁移将自动执行
 * 
 * @author Claude
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
    name = "smartcs.migration.encrypt-api-keys", 
    havingValue = "true", 
    matchIfMissing = false
)
public class ApiKeyMigrationUtil implements CommandLineRunner {
    
    private final ProviderMapper providerMapper;
    private final SecretCryptoService secretCryptoService;
    
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("=== 开始API Key加密迁移 ===");
        
        try {
            // 查询所有需要迁移的提供商
            List<ProviderDO> providersToMigrate = findProvidersToMigrate();
            
            if (providersToMigrate.isEmpty()) {
                log.info("没有找到需要迁移的API Key，迁移完成");
                return;
            }
            
            log.info("找到 {} 个提供商需要进行API Key加密迁移", providersToMigrate.size());
            
            int successCount = 0;
            int failureCount = 0;
            
            for (ProviderDO provider : providersToMigrate) {
                try {
                    migrateProviderApiKey(provider);
                    successCount++;
                    log.info("提供商 {} (ID: {}) API Key加密成功", provider.getProviderType(), provider.getId());
                } catch (Exception e) {
                    failureCount++;
                    log.error("提供商 {} (ID: {}) API Key加密失败", provider.getProviderType(), provider.getId(), e);
                }
            }
            
            log.info("=== API Key加密迁移完成 ===");
            log.info("成功: {}, 失败: {}, 总计: {}", successCount, failureCount, providersToMigrate.size());
            
            if (failureCount > 0) {
                log.warn("存在失败的迁移记录，请检查日志并手动处理");
            } else {
                log.info("所有API Key加密迁移成功完成！");
                log.info("建议：");
                log.info("1. 验证加密结果正确性");
                log.info("2. 测试模型调用功能");
                log.info("3. 确认无误后，关闭迁移开关: smartcs.migration.encrypt-api-keys=false");
                log.info("4. 可选：清空原api_key字段数据");
            }
            
        } catch (Exception e) {
            log.error("API Key加密迁移过程中发生异常", e);
            throw e;
        }
    }
    
    /**
     * 查找需要迁移的提供商
     * 条件：有明文api_key但没有加密存储的记录
     */
    private List<ProviderDO> findProvidersToMigrate() {
        return providerMapper.selectByApiKeyToMigrate();
    }
    
    /**
     * 迁移单个提供商的API Key
     */
    private void migrateProviderApiKey(ProviderDO provider) {
        // 验证原始数据
        String originalApiKey = provider.getApiKey();
        if (originalApiKey == null || originalApiKey.trim().isEmpty()) {
            throw new IllegalStateException("原始API Key为空");
        }
        
        // 执行加密
        SecretCryptoService.EncryptResult encryptResult = 
            secretCryptoService.encryptForProviderApiKey(originalApiKey);
        
        // 更新数据库
        ProviderDO updateProvider = new ProviderDO();
        updateProvider.setId(provider.getId());
        updateProvider.setApiKeyCipher(encryptResult.getCiphertext());
        updateProvider.setApiKeyIv(encryptResult.getIv());
        updateProvider.setApiKeyKid(encryptResult.getKid());
        updateProvider.setUpdatedAt(System.currentTimeMillis());
        
        int updateResult = providerMapper.updateById(updateProvider);
        if (updateResult != 1) {
            throw new RuntimeException("数据库更新失败，受影响行数: " + updateResult);
        }
        
        // 验证加密结果
        ProviderDO updatedProvider = providerMapper.selectById(provider.getId());
        String decryptedApiKey = secretCryptoService.decryptProviderApiKey(
            updatedProvider.getApiKeyCipher(), 
            updatedProvider.getApiKeyIv(), 
            updatedProvider.getApiKeyKid()
        );
        
        if (!originalApiKey.equals(decryptedApiKey)) {
            throw new RuntimeException("加密验证失败：解密结果与原始数据不匹配");
        }
        
        log.debug("提供商 {} API Key加密验证通过", provider.getProviderType());
    }
    
    /**
     * 回滚迁移（仅用于测试环境）
     * 警告：此方法会将加密的API Key转换回明文存储，仅用于测试目的
     */
    @Transactional
    public void rollbackMigration() {
        log.warn("=== 开始回滚API Key加密迁移 ===");
        log.warn("警告：此操作将加密数据转换为明文存储，仅适用于测试环境！");
        
        List<ProviderDO> encryptedProviders = providerMapper.selectByEncryptedApiKey();
        
        if (encryptedProviders.isEmpty()) {
            log.info("没有找到已加密的API Key，无需回滚");
            return;
        }
        
        log.warn("找到 {} 个已加密的提供商，开始回滚", encryptedProviders.size());
        
        for (ProviderDO provider : encryptedProviders) {
            try {
                // 解密API Key
                String decryptedApiKey = secretCryptoService.decryptProviderApiKey(
                    provider.getApiKeyCipher(),
                    provider.getApiKeyIv(),
                    provider.getApiKeyKid()
                );
                
                // 更新为明文存储
                ProviderDO updateProvider = new ProviderDO();
                updateProvider.setId(provider.getId());
                updateProvider.setApiKey(decryptedApiKey);
                updateProvider.setApiKeyCipher(null);
                updateProvider.setApiKeyIv(null);
                updateProvider.setApiKeyKid(null);
                updateProvider.setUpdatedAt(System.currentTimeMillis());
                
                providerMapper.updateById(updateProvider);
                
                log.warn("提供商 {} API Key已回滚为明文存储", provider.getProviderType());
                
            } catch (Exception e) {
                log.error("提供商 {} API Key回滚失败", provider.getProviderType(), e);
            }
        }
        
        log.warn("=== API Key加密迁移回滚完成 ===");
    }
}