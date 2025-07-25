package com.leyue.smartcs.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import com.leyue.smartcs.domain.model.Provider;
import com.leyue.smartcs.domain.model.enums.ProviderType;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 模型Bean管理服务
 * 负责根据模型提供商配置动态创建和销毁LangChain4j模型Bean
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ModelBeanManagerService {

    private final ApplicationContext applicationContext;

    // 存储已创建的Bean名称，用于管理Bean生命周期
    private final Map<String, String> beanRegistry = new ConcurrentHashMap<>();

    // 重启ModelBean
    public void restartModelBean(Provider provider, String modelType) {
        destroyModelBean(provider, modelType);
        createModelBean(provider, modelType);
    }

    /**
     * 根据模型提供商配置创建对应的模型Bean
     *
     * @param provider 模型提供商配置
     * @param modelType 模型类型 ("chat", "embedding")
     * @return Bean名称
     */
    public String createModelBean(Provider provider, String modelType) {
        try {
            String beanName = generateBeanName(provider, modelType);

            // 检查Bean是否已存在
            if (beanRegistry.containsKey(getBeanKey(provider, modelType))) {
                log.info("模型Bean已存在，providerId: {}, modelType: {}, beanName: {}", provider.getId(), modelType, beanName);
                return beanName;
            }

            DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) ((ConfigurableApplicationContext) applicationContext)
                    .getBeanFactory();

            // 根据提供商和模型类型创建不同的Bean
            if (provider.getProviderType().isOpenAiCompatible()) {
                if ("chat".equals(modelType)) {
                    // 只创建流式模型
                    createOpenAiStreamingChatModelBean(beanFactory, beanName, provider);
                } else if ("embedding".equals(modelType)) {
                    createOpenAiEmbeddingModelBean(beanFactory, beanName, provider);
                }
            }
            // TODO: 添加其他厂商的Bean创建逻辑

            // 注册Bean
            beanRegistry.put(getBeanKey(provider, modelType), beanName);
            log.info("成功创建模型Bean，providerId: {}, modelType: {}, beanName: {}", 
                    provider.getId(), modelType, beanName);

            return beanName;

        } catch (Exception e) {
            log.error("创建模型Bean失败，providerId: {}, modelType: {}, error: {}", provider.getId(), modelType, e.getMessage(), e);
            throw new RuntimeException("创建模型Bean失败: " + e.getMessage(), e);
        }
    }

    /**
     * 销毁模型Bean
     *
     * @param provider 模型提供商配置
     * @param modelType 模型类型
     * @return 是否成功
     */
    public boolean destroyModelBean(Provider provider, String modelType) {
        try {
            String beanKey = getBeanKey(provider, modelType);
            String beanName = beanRegistry.get(beanKey);

            if (beanName == null) {
                log.warn("未找到对应的Bean，providerId: {}, modelType: {}", provider.getId(), modelType);
                return true;
            }

            DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) ((ConfigurableApplicationContext) applicationContext)
                    .getBeanFactory();

            if (beanFactory.containsBeanDefinition(beanName)) {
                beanFactory.removeBeanDefinition(beanName);
                beanRegistry.remove(beanKey);
                log.info("成功销毁模型Bean，providerId: {}, modelType: {}, beanName: {}", provider.getId(), modelType, beanName);
            }

            return true;

        } catch (Exception e) {
            log.error("销毁模型Bean失败，providerId: {}, modelType: {}, error: {}", provider.getId(), modelType, e.getMessage(), e);
            return false;
        }
    }



    /**
     * 创建OpenAI流式Chat模型Bean
     */
    private void createOpenAiStreamingChatModelBean(DefaultListableBeanFactory beanFactory, String beanName,
                                                   Provider provider) {
        // 使用LangChain4j创建OpenAiStreamingChatModel实例
        OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder builder =
                OpenAiStreamingChatModel.builder()
                        .baseUrl(provider.getEndpoint())
                        .apiKey(provider.getApiKey());

        // 使用默认配置或从环境变量读取具体参数
        // TODO: 后续可以考虑从配置文件或数据库读取模型参数
        
        OpenAiStreamingChatModel streamingChatModel = builder.build();

        // 注册已构建的实例
        beanFactory.registerSingleton(beanName, streamingChatModel);
    }

    /**
     * 创建OpenAI Embedding模型Bean
     */
    private void createOpenAiEmbeddingModelBean(DefaultListableBeanFactory beanFactory, String beanName,
                                                Provider provider) {
        // 使用LangChain4j创建OpenAiEmbeddingModel实例
        EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .baseUrl(provider.getEndpoint())
                .apiKey(provider.getApiKey())
                .build();

        // 注册已构建的实例
        beanFactory.registerSingleton(beanName, embeddingModel);
    }



    /**
     * 生成Bean名称
     */
    private String generateBeanName(Provider provider, String modelType) {
        String actualModelType = "chat".equals(modelType) ? "streaming_chat" : modelType;
        return String.format("%s_%s_model_%d",
                provider.getProviderType().getKey(),
                actualModelType,
                provider.getId());
    }

    /**
     * 生成Bean注册key
     */
    public String getBeanKey(Provider provider, String modelType) {
        String actualModelType = "chat".equals(modelType) ? "streaming_chat" : modelType;
        return String.format("%s:%s:%d",
                provider.getProviderType().getKey(),
                actualModelType,
                provider.getId());
    }

    /**
     * 根据Provider和模型类型获取其要使用的模型对象
     */
    public Object getModelBean(Provider provider, String modelType) {
        String beanKey = getBeanKey(provider, modelType);
        String beanName = beanRegistry.get(beanKey);
        if (beanName == null) {
            return null;
        }
        return applicationContext.getBean(beanName);
    }

    // 获取第一个Bean
    public Object getFirstModelBean() {
        return applicationContext.getBean(beanRegistry.values().iterator().next());
    }
}