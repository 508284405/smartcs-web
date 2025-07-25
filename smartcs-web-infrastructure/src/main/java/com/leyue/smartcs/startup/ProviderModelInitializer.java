package com.leyue.smartcs.startup;

import com.leyue.smartcs.config.ModelBeanManagerService;
import com.leyue.smartcs.domain.model.Provider;
import com.leyue.smartcs.domain.model.gateway.ProviderGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 应用启动时初始化模型提供商Bean
 * 从模型提供商配置表中获取启用的Provider配置，自动注册模型Bean
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProviderModelInitializer implements CommandLineRunner {

    private final ProviderGateway providerGateway;
    private final ModelBeanManagerService modelBeanManagerService;

    @Override
    public void run(String... args) throws Exception {
        log.info("开始初始化模型提供商Bean...");
        
        try {
            // 获取所有有效的模型提供商配置
            List<Provider> activeProviders = providerGateway.findAll();
            
            if (activeProviders.isEmpty()) {
                log.info("未找到有效的模型提供商配置，跳过模型Bean初始化");
                return;
            }
            
            log.info("找到 {} 个模型提供商配置，开始注册模型Bean", activeProviders.size());
            
            int successCount = 0;
            int failureCount = 0;
            int totalAttempts = 0;
            
            // 为每个模型提供商的每种支持的模型类型创建模型Bean
            for (Provider provider : activeProviders) {
                if (!provider.isValid()) {
                    log.warn("模型提供商配置无效，跳过 - providerId: {}, providerType: {}", 
                            provider.getId(), provider.getProviderType());
                    continue;
                }
                
                List<String> supportedModelTypes = provider.getSupportedModelTypesList();
                if (supportedModelTypes.isEmpty()) {
                    log.warn("模型提供商未配置支持的模型类型，跳过 - providerId: {}, providerType: {}", 
                            provider.getId(), provider.getProviderType());
                    continue;
                }
                
                // 为每种支持的模型类型创建Bean
                for (String modelType : supportedModelTypes) {
                    try {
                        totalAttempts++;
                        String beanName = modelBeanManagerService.createModelBean(provider, modelType);
                        log.info("成功注册模型Bean - providerId: {}, providerType: {}, modelType: {}, beanName: {}", 
                                provider.getId(), provider.getProviderType(), modelType, beanName);
                        successCount++;
                    } catch (Exception e) {
                        log.error("注册模型Bean失败 - providerId: {}, providerType: {}, modelType: {}, error: {}", 
                                provider.getId(), provider.getProviderType(), modelType, e.getMessage(), e);
                        failureCount++;
                    }
                }
            }
            
            log.info("模型Bean初始化完成 - 成功: {}, 失败: {}, 总计: {}", 
                    successCount, failureCount, totalAttempts);
                    
        } catch (Exception e) {
            log.error("初始化模型Bean过程中发生异常: {}", e.getMessage(), e);
            throw e;
        }
    }
} 