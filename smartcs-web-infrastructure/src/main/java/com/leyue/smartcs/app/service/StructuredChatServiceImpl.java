package com.leyue.smartcs.app.service;

import com.leyue.smartcs.domain.app.model.StructuredChatResponse;
import com.leyue.smartcs.domain.app.service.StructuredChatService;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 结构化聊天服务的LangChain4j技术实现
 * 实现Domain层定义的业务接口，封装LangChain4j结构化输出技术细节
 */
@Service("domainStructuredChatService")
@RequiredArgsConstructor
@Slf4j
public class StructuredChatServiceImpl implements StructuredChatService {

    // LangChain4j AI Services接口，用于结构化输出
    private interface LangChain4jStructuredChatService {
        
        @SystemMessage("""
            你是一个专业的AI助手。请根据用户的问题生成结构化的响应。
            
            响应要求：
            1. 根据问题内容确定合适的响应类型（回答、澄清、建议等）
            2. 评估回答的置信度（0-1）
            3. 如果基于知识库回答，请提供知识来源信息
            4. 建议1-3个相关的后续问题
            5. 如果问题不清楚，标记为需要澄清
            
            系统提示词：{{systemPrompt}}
            """)
        @UserMessage("{{userMessage}}")
        StructuredChatResponse generateStructuredResponse(@MemoryId String memoryId,
                                                        @V("systemPrompt") String systemPrompt,
                                                        @V("userMessage") String userMessage,
                                                        @V("variables") Map<String, Object> variables);

        @SystemMessage("""
            你是一个专业的知识问答助手。基于提供的知识库内容回答用户问题。
            
            回答要求：
            1. 优先使用知识库中的准确信息
            2. 如果知识库中没有相关信息，诚实说明
            3. 提供信息来源和置信度评估
            4. 建议相关的后续问题
            5. 保持回答的专业性和准确性
            """)
        @UserMessage("问题：{{userMessage}}")
        StructuredChatResponse generateKnowledgeBasedResponse(@MemoryId String memoryId,
                                                            @V("userMessage") String userMessage,
                                                            @V("knowledgeBaseId") Long knowledgeBaseId);

        @SystemMessage("""
            分析用户消息的意图，并生成相应的结构化响应。
            
            分析维度：
            1. 用户意图类型（问答、闲聊、求助、抱怨等）
            2. 问题的明确程度
            3. 所需的回答类型
            4. 是否需要额外信息
            
            根据分析结果生成合适的结构化响应。
            """)
        @UserMessage("用户消息：{{userMessage}}\n上下文：{{context}}")
        StructuredChatResponse analyzeIntentAndRespond(@V("userMessage") String userMessage,
                                                     @V("context") String context);

        @SystemMessage("""
            系统遇到了错误，请生成用户友好的错误响应。
            
            要求：
            1. 不要暴露技术细节
            2. 提供可能的解决建议
            3. 保持友好和专业的语调
            4. 建议用户可以尝试的替代方案
            """)
        @UserMessage("错误信息：{{errorMessage}}\n用户原始问题：{{userMessage}}")
        StructuredChatResponse generateErrorResponse(@V("errorMessage") String errorMessage,
                                                   @V("userMessage") String userMessage);
    }

    // 延迟初始化LangChain4j服务实例
    private volatile LangChain4jStructuredChatService langChain4jService;

    @Override
    public StructuredChatResponse generateStructuredResponse(String sessionId, String systemPrompt, 
                                                           String userMessage, Map<String, Object> variables) {
        try {
            validateInputs(sessionId, userMessage);
            
            LangChain4jStructuredChatService service = getLangChain4jService();
            return service.generateStructuredResponse(sessionId, systemPrompt, userMessage, variables);
            
        } catch (Exception e) {
            log.error("生成结构化响应失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
            return createErrorResponse("生成结构化响应失败: " + e.getMessage(), userMessage);
        }
    }

    @Override
    public StructuredChatResponse generateKnowledgeBasedResponse(String sessionId, String userMessage, 
                                                               Long knowledgeBaseId) {
        try {
            validateInputs(sessionId, userMessage);
            if (knowledgeBaseId == null) {
                throw new IllegalArgumentException("知识库ID不能为空");
            }
            
            LangChain4jStructuredChatService service = getLangChain4jService();
            return service.generateKnowledgeBasedResponse(sessionId, userMessage, knowledgeBaseId);
            
        } catch (Exception e) {
            log.error("生成知识库响应失败: sessionId={}, knowledgeBaseId={}, error={}", 
                     sessionId, knowledgeBaseId, e.getMessage(), e);
            return createErrorResponse("生成知识库响应失败: " + e.getMessage(), userMessage);
        }
    }

    @Override
    public StructuredChatResponse analyzeIntentAndRespond(String userMessage, String context) {
        try {
            if (userMessage == null || userMessage.trim().isEmpty()) {
                throw new IllegalArgumentException("用户消息不能为空");
            }
            
            LangChain4jStructuredChatService service = getLangChain4jService();
            return service.analyzeIntentAndRespond(userMessage, context != null ? context : "");
            
        } catch (Exception e) {
            log.error("分析用户意图失败: error={}", e.getMessage(), e);
            return createErrorResponse("分析用户意图失败: " + e.getMessage(), userMessage);
        }
    }

    @Override
    public StructuredChatResponse generateErrorResponse(String errorMessage, String userMessage) {
        try {
            LangChain4jStructuredChatService service = getLangChain4jService();
            return service.generateErrorResponse(errorMessage, userMessage);
            
        } catch (Exception e) {
            log.error("生成错误响应失败: error={}", e.getMessage(), e);
            // 如果连错误响应都生成失败，则返回简单的默认响应
            return createFallbackErrorResponse(errorMessage, userMessage);
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            return getLangChain4jService() != null;
        } catch (Exception e) {
            log.warn("检查结构化聊天服务可用性失败", e);
            return false;
        }
    }

    @Override
    public boolean validateResponse(StructuredChatResponse response) {
        if (response == null) {
            return false;
        }
        
        // 检查基本字段
        if (response.getContent() == null || response.getContent().trim().isEmpty()) {
            log.warn("结构化响应内容为空");
            return false;
        }
        
        // 检查响应类型
        if (response.getType() == null) {
            log.warn("结构化响应类型为空");
            return false;
        }
        
        // 检查置信度范围
        if (response.getConfidence() != null && 
            (response.getConfidence() < 0.0 || response.getConfidence() > 1.0)) {
            log.warn("结构化响应置信度超出范围: {}", response.getConfidence());
            return false;
        }
        
        return true;
    }

    /**
     * 获取LangChain4j服务实例（延迟初始化）
     */
    private LangChain4jStructuredChatService getLangChain4jService() {
        if (langChain4jService == null) {
            synchronized (this) {
                if (langChain4jService == null) {
                    // 这里需要根据实际情况配置ChatModel
                    // 为了演示，暂时返回null，实际使用时需要配置
                    // langChain4jService = AiServices.builder(LangChain4jStructuredChatService.class)
                    //     .chatModel(chatModel)
                    //     .build();
                    throw new RuntimeException("LangChain4j结构化服务尚未配置，需要注入ChatModel");
                }
            }
        }
        return langChain4jService;
    }

    /**
     * 验证输入参数
     */
    private void validateInputs(String sessionId, String userMessage) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("会话ID不能为空");
        }
        if (userMessage == null || userMessage.trim().isEmpty()) {
            throw new IllegalArgumentException("用户消息不能为空");
        }
    }

    /**
     * 创建错误响应
     */
    private StructuredChatResponse createErrorResponse(String errorMessage, String userMessage) {
        return StructuredChatResponse.builder()
                .content("抱歉，处理您的请求时遇到了错误。请稍后重试或联系技术支持。")
                .type(StructuredChatResponse.ResponseType.ERROR)
                .confidence(0.0)
                .needsClarification(false)
                .generatedAt(java.time.LocalDateTime.now())
                .build();
    }

    /**
     * 创建备用错误响应
     */
    private StructuredChatResponse createFallbackErrorResponse(String errorMessage, String userMessage) {
        return StructuredChatResponse.builder()
                .content("系统当前不可用，请稍后重试。")
                .type(StructuredChatResponse.ResponseType.ERROR)
                .confidence(0.0)
                .needsClarification(false)
                .generatedAt(java.time.LocalDateTime.now())
                .build();
    }
}