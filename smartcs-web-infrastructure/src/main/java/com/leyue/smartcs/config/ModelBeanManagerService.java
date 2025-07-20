package com.leyue.smartcs.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.leyue.smartcs.domain.bot.BotProfile;
import com.leyue.smartcs.domain.bot.enums.ModelTypeEnum;
import com.leyue.smartcs.domain.bot.enums.VendorTypeEnum;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模型Bean管理服务
 * 负责根据机器人配置动态创建和销毁LangChain4j模型Bean
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ModelBeanManagerService {

    private final ApplicationContext applicationContext;

    // 存储已创建的Bean名称，用于管理Bean生命周期
    private final Map<String, String> beanRegistry = new ConcurrentHashMap<>();

    // 重启ModelBean
    public void restartModelBean(BotProfile botProfile) {
        destroyModelBean(botProfile);
        createModelBean(botProfile);
    }

    /**
     * 根据机器人配置创建对应的模型Bean
     *
     * @param botProfile 机器人配置
     * @return Bean名称
     */
    public String createModelBean(BotProfile botProfile) {
        try {
            String beanName = generateBeanName(botProfile);

            // 检查Bean是否已存在
            if (beanRegistry.containsKey(getBeanKey(botProfile))) {
                log.info("模型Bean已存在，botId: {}, beanName: {}", botProfile.getBotId(), beanName);
                return beanName;
            }

            DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) ((ConfigurableApplicationContext) applicationContext)
                    .getBeanFactory();

            // 根据厂商和模型类型创建不同的Bean
            if (botProfile.getVendor() == VendorTypeEnum.OPENAI) {
                if (botProfile.getModelType() == ModelTypeEnum.CHAT) {
                    // 只创建流式模型
                    createOpenAiStreamingChatModelBean(beanFactory, beanName, botProfile);
                } else if (botProfile.getModelType() == ModelTypeEnum.EMBEDDING) {
                    createOpenAiEmbeddingModelBean(beanFactory, beanName, botProfile);
                }
            }
            // TODO: 添加其他厂商的Bean创建逻辑

            // 注册Bean
            beanRegistry.put(getBeanKey(botProfile), beanName);
            log.info("成功创建模型Bean，botId: {}, beanName: {}", 
                    botProfile.getBotId(), beanName);

            return beanName;

        } catch (Exception e) {
            log.error("创建模型Bean失败，botId: {}, error: {}", botProfile.getBotId(), e.getMessage(), e);
            throw new RuntimeException("创建模型Bean失败: " + e.getMessage(), e);
        }
    }

    /**
     * 销毁模型Bean
     *
     * @param botProfile 机器人配置
     * @return 是否成功
     */
    public boolean destroyModelBean(BotProfile botProfile) {
        try {
            String beanKey = getBeanKey(botProfile);
            String beanName = beanRegistry.get(beanKey);

            if (beanName == null) {
                log.warn("未找到对应的Bean，botId: {}", botProfile.getBotId());
                return true;
            }

            DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) ((ConfigurableApplicationContext) applicationContext)
                    .getBeanFactory();

            if (beanFactory.containsBeanDefinition(beanName)) {
                beanFactory.removeBeanDefinition(beanName);
                beanRegistry.remove(beanKey);
                log.info("成功销毁模型Bean，botId: {}, beanName: {}", botProfile.getBotId(), beanName);
            }

            return true;

        } catch (Exception e) {
            log.error("销毁模型Bean失败，botId: {}, error: {}", botProfile.getBotId(), e.getMessage(), e);
            return false;
        }
    }



    /**
     * 创建OpenAI流式Chat模型Bean
     */
    private void createOpenAiStreamingChatModelBean(DefaultListableBeanFactory beanFactory, String beanName,
                                                   BotProfile botProfile) {
        JSONObject options = JSON.parseObject(botProfile.getOptions());

        // 使用LangChain4j创建OpenAiStreamingChatModel实例
        OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder builder =
                OpenAiStreamingChatModel.builder()
                        .baseUrl(botProfile.getBaseUrl())
                        .apiKey(botProfile.getApiKey());

        // 设置模型参数
        if (options.containsKey("model")) {
            builder.modelName(options.getString("model"));
        }
        if (options.containsKey("temperature")) {
            builder.temperature(options.getDouble("temperature"));
        }
        if (options.containsKey("maxTokens")) {
            builder.maxTokens(options.getIntValue("maxTokens"));
        }
        if (options.containsKey("topP")) {
            builder.topP(options.getDouble("topP"));
        }
        if (options.containsKey("frequencyPenalty")) {
            builder.frequencyPenalty(options.getDouble("frequencyPenalty"));
        }
        if (options.containsKey("presencePenalty")) {
            builder.presencePenalty(options.getDouble("presencePenalty"));
        }

        OpenAiStreamingChatModel streamingChatModel = builder.build();

        // 注册已构建的实例
        beanFactory.registerSingleton(beanName, streamingChatModel);
    }

    /**
     * 创建OpenAI Embedding模型Bean
     */
    private void createOpenAiEmbeddingModelBean(DefaultListableBeanFactory beanFactory, String beanName,
                                                BotProfile botProfile) {
        // 使用LangChain4j创建OpenAiEmbeddingModel实例
        EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .baseUrl(botProfile.getBaseUrl())
                .apiKey(botProfile.getApiKey())
                .build();

        // 注册已构建的实例
        beanFactory.registerSingleton(beanName, embeddingModel);
    }



    /**
     * 生成Bean名称
     */
    private String generateBeanName(BotProfile botProfile) {
        String modelType = botProfile.getModelType() == ModelTypeEnum.CHAT ? "streaming_chat" : botProfile.getModelType().getCode();
        return String.format("%s_%s_model_%d",
                botProfile.getVendor().getCode(),
                modelType,
                botProfile.getBotId());
    }

    /**
     * 生成Bean注册key
     */
    public String getBeanKey(BotProfile botProfile) {
        String modelType = botProfile.getModelType() == ModelTypeEnum.CHAT ? "streaming_chat" : botProfile.getModelType().getCode();
        return String.format("%s:%s:%d",
                botProfile.getVendor().getCode(),
                modelType,
                botProfile.getBotId());
    }

    /**
     * 根据BotProfile获取其要使用的模型对象
     */
    public Object getModelBean(BotProfile botProfile) {
        String beanKey = getBeanKey(botProfile);
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