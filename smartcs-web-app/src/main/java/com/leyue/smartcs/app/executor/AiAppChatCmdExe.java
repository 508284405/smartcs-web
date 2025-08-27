package com.leyue.smartcs.app.executor;

import com.leyue.smartcs.rag.SmartChatService;
import com.leyue.smartcs.domain.common.gateway.IdGeneratorGateway;
import com.leyue.smartcs.dto.app.AiAppChatCmd;
import com.leyue.smartcs.dto.app.AiAppChatResponse;
import com.leyue.smartcs.dto.app.AiAppChatSSEMessage;
import com.leyue.smartcs.dto.app.RagComponentConfig;
import com.leyue.smartcs.model.gateway.ModelProvider;
import com.leyue.smartcs.rag.factory.RagAugmentorFactory;
import com.leyue.smartcs.moderation.service.LangChain4jModerationService;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.service.TokenStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * AI应用聊天命令执行器
 * 
 * <p>重构版本：完全基于LangChain4j框架的SmartChatService，支持自定义RAG配置。
 * 提供流式SSE响应，集成完整的RAG（检索增强生成）能力。</p>
 * 
 * <h3>主要功能:</h3>
 * <ul>
 *   <li>基于LangChain4j的SmartChatService进行AI聊天</li>
 *   <li>支持自定义RAG组件配置，允许前端动态调整检索参数</li>
 *   <li>自动验证和修正配置参数，确保系统稳定性</li>
 *   <li>流式SSE响应，提供实时的聊天体验</li>
 *   <li>完整的错误处理和日志记录</li>
 * </ul>
 * 
 * <h3>RAG配置支持:</h3>
 * <ul>
 *   <li>内容聚合器：控制返回结果的数量和质量</li>
 *   <li>查询转换器：控制查询扩展的复杂度</li>
 *   <li>查询路由器：选择性启用不同的检索器</li>
 *   <li>参数验证：自动修正超出范围的配置值</li>
 * </ul>
 * 
 * @see SmartChatService
 * @see RagComponentConfig
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AiAppChatCmdExe {

    private final ModelProvider modelProvider;
    private final RagAugmentorFactory ragAugmentorFactory;
    private final ChatMemoryStore chatMemoryStore;
    private final IdGeneratorGateway idGeneratorGateway;
    private final LangChain4jModerationService langChain4jModerationService;

    /**
     * 执行SSE聊天
     * 简化版本：支持内容审核和RAG增强，意图识别集成在RAG QueryTransformer中
     */
    public SseEmitter execute(AiAppChatCmd cmd) {
        SseEmitter emitter = new SseEmitter(cmd.getTimeout() != null ? cmd.getTimeout() : 30000L);
        String sessionId = generateSessionId(cmd);

        CompletableFuture.runAsync(() -> {
            try {
                sendSSEMessage(emitter, AiAppChatSSEMessage.start(sessionId));
                
                // 1. 内容审核预检（输入阶段）
                sendSSEMessage(emitter, AiAppChatSSEMessage.progress(sessionId, "正在进行内容安全检查..."));
                boolean inputSafe = performInputModeration(cmd.getMessage(), cmd.getModelId(), sessionId);
                if (!inputSafe) {
                    sendSSEMessage(emitter, AiAppChatSSEMessage.error(sessionId, "输入内容包含不当信息，请修改后重试"));
                    emitter.complete();
                    return;
                }
                
                // 2. 验证和处理RAG配置
                RagComponentConfig ragConfig = validateAndProcessRagConfig(cmd.getRagConfig());
                if (ragConfig != null) {
                    log.info("使用自定义RAG配置: sessionId={}, ragConfig={}", sessionId, ragConfig);
                } else {
                    log.info("使用默认RAG配置: sessionId={}", sessionId);
                }
                
                // 3. 动态创建SmartChatService实例并执行对话
                SmartChatService smartChatService = createSmartChatService(cmd.getModelId(), ragConfig);
                processChatStream(emitter, cmd, sessionId, smartChatService);
                
            } catch (Exception e) {
                handleError(emitter, cmd.getAppId(), sessionId, e);
            }
        });

        setupEmitterCallbacks(emitter, cmd.getAppId(), sessionId);
        return emitter;
    }

    /**
     * 处理流式聊天
     * 使用LangChain4j原生TokenStream，框架自动处理RAG和记忆，支持输出审核
     */
    private void processChatStream(SseEmitter emitter, AiAppChatCmd cmd, String sessionId, SmartChatService smartChatService) throws Exception {
        sendSSEMessage(emitter, AiAppChatSSEMessage.progress(sessionId, "正在生成AI回答..."));
        
        // 使用SmartChatService的流式聊天 - 框架自动处理RAG和记忆
        TokenStream tokenStream = smartChatService.chatStream(sessionId, cmd.getMessage());
        StringBuilder fullResponse = new StringBuilder();
        
        tokenStream
            .onPartialResponse(partialResponse -> {
                // 部分消息
                try {
                    fullResponse.append(partialResponse);
                    AiAppChatResponse dataResponse = AiAppChatResponse.builder()
                            .sessionId(sessionId)
                            .content(partialResponse)
                            .finished(false)
                            .timestamp(System.currentTimeMillis())
                            .build();
                    sendSSEMessage(emitter, AiAppChatSSEMessage.data(sessionId, dataResponse));
                } catch (Exception e) {
                    // 检查是否为客户端断开，如果是则只记录warning
                    if (isClientDisconnectException(e)) {
                        log.warn("发送流式消息时检测到客户端断开: sessionId={}", sessionId);
                    } else {
                        log.error("发送流式消息失败: sessionId={}", sessionId, e);
                    }
                }
            })
            .onCompleteResponse(response -> {
                // AI聊天完成 - 增加输出审核
                try {
                    log.info("AI聊天完成: sessionId={}, responseLength={}", 
                            sessionId, fullResponse.length());
                    
                    // 5. 输出内容审核（异步）
                    String finalContent = fullResponse.toString();
                    CompletableFuture.runAsync(() -> {
                        performOutputModeration(finalContent, cmd.getModelId(), sessionId);
                    });
                    
                    AiAppChatResponse completeResponse = AiAppChatResponse.builder()
                            .sessionId(sessionId)
                            .content(finalContent)
                            .finished(true)
                            .timestamp(System.currentTimeMillis())
                            .build();
                    sendSSEMessage(emitter, AiAppChatSSEMessage.complete(sessionId, completeResponse));
                } catch (Exception e) {
                    // 检查是否为客户端断开，如果是则只记录warning
                    if (isClientDisconnectException(e)) {
                        log.warn("发送完成消息时检测到客户端断开: sessionId={}", sessionId);
                    } else {
                        log.error("发送完成消息失败: sessionId={}", sessionId, e);
                    }
                } finally {
                    // 无论如何都要完成emitter
                    try {
                        emitter.complete();
                    } catch (Exception completeException) {
                        log.debug("完成emitter时出现异常: sessionId={}, error={}", sessionId, completeException.getMessage());
                    }
                }
            })
            .onError(throwable -> {
                // 使用统一的错误处理逻辑
                if (throwable instanceof Exception) {
                    handleError(emitter, cmd.getAppId(), sessionId, (Exception) throwable);
                } else {
                    // 对于非Exception的Throwable，包装为RuntimeException
                    RuntimeException wrappedException = new RuntimeException("TokenStream处理异常", throwable);
                    handleError(emitter, cmd.getAppId(), sessionId, wrappedException);
                }
            })
            .start();
    }

    /**
     * 验证和处理RAG配置
     * 
     * <p>对传入的RAG配置进行全面验证，确保所有参数都在合理范围内。
     * 对于超出范围的参数值，会自动修正为默认值并记录警告日志。</p>
     * 
     * <h3>验证规则:</h3>
     * <ul>
     *   <li><strong>内容聚合器</strong>：maxResults∈[1,50], minScore∈[0.0,1.0]</li>
     *   <li><strong>查询转换器</strong>：n∈[1,10]</li>
     *   <li><strong>Web搜索</strong>：maxResults∈[1,50], timeout∈[1,60]</li>
     *   <li><strong>知识库搜索</strong>：topK∈[1,100], scoreThreshold∈[0.0,1.0]</li>
     * </ul>
     * 
     * <h3>错误处理:</h3>
     * <ul>
     *   <li>参数超出范围时自动修正为默认值</li>
     *   <li>配置验证异常时返回null，使用系统默认配置</li>
     *   <li>所有验证过程都有详细的日志记录</li>
     * </ul>
     * 
     * @param ragConfig 原始RAG配置，可以为null
     * @return 验证后的RAG配置，如果为null或验证失败则返回null（表示使用默认配置）
     */
    private RagComponentConfig validateAndProcessRagConfig(RagComponentConfig ragConfig) {
        if (ragConfig == null) {
            return null;
        }
        
        try {
            // 验证内容聚合器配置
            RagComponentConfig.ContentAggregatorConfig contentConfig = ragConfig.getContentAggregatorOrDefault();
            if (contentConfig.getMaxResults() < 1 || contentConfig.getMaxResults() > 50) {
                log.warn("内容聚合器maxResults超出范围，使用默认值: {}", contentConfig.getMaxResults());
                contentConfig.setMaxResults(5);
            }
            if (contentConfig.getMinScore() < 0.0 || contentConfig.getMinScore() > 1.0) {
                log.warn("内容聚合器minScore超出范围，使用默认值: {}", contentConfig.getMinScore());
                contentConfig.setMinScore(0.5);
            }
            
            // 验证查询转换器配置
            RagComponentConfig.QueryTransformerConfig transformerConfig = ragConfig.getQueryTransformerOrDefault();
            if (transformerConfig.getN() < 1 || transformerConfig.getN() > 10) {
                log.warn("查询转换器n超出范围，使用默认值: {}", transformerConfig.getN());
                transformerConfig.setN(5);
            }
            
            // 验证Web搜索配置
            RagComponentConfig.WebSearchConfig webConfig = ragConfig.getWebSearchOrDefault();
            if (webConfig.getMaxResults() < 1 || webConfig.getMaxResults() > 50) {
                log.warn("Web搜索maxResults超出范围，使用默认值: {}", webConfig.getMaxResults());
                webConfig.setMaxResults(10);
            }
            if (webConfig.getTimeout() < 1 || webConfig.getTimeout() > 60) {
                log.warn("Web搜索timeout超出范围，使用默认值: {}", webConfig.getTimeout());
                webConfig.setTimeout(10);
            }
            
            // 验证知识库搜索配置
            RagComponentConfig.KnowledgeSearchConfig knowledgeConfig = ragConfig.getKnowledgeSearchOrDefault();
            if (knowledgeConfig.getTopK() < 1 || knowledgeConfig.getTopK() > 100) {
                log.warn("知识库搜索topK超出范围，使用默认值: {}", knowledgeConfig.getTopK());
                knowledgeConfig.setTopK(5);
            }
            if (knowledgeConfig.getScoreThreshold() < 0.0 || knowledgeConfig.getScoreThreshold() > 1.0) {
                log.warn("知识库搜索scoreThreshold超出范围，使用默认值: {}", knowledgeConfig.getScoreThreshold());
                knowledgeConfig.setScoreThreshold(0.7);
            }
            
            // 验证组件级模型ID配置
            // 验证内容聚合器的评分模型ID
            if (contentConfig.getScoringModelId() != null && contentConfig.getScoringModelId() <= 0) {
                log.warn("内容聚合器scoringModelId必须为正数，当前值: {}", contentConfig.getScoringModelId());
                contentConfig.setScoringModelId(null);
            }
            
            // 验证查询转换器的模型ID
            if (transformerConfig.getModelId() != null && transformerConfig.getModelId() <= 0) {
                log.warn("查询转换器modelId必须为正数，当前值: {}", transformerConfig.getModelId());
                transformerConfig.setModelId(null);
            }
            
            // 验证查询路由器的模型ID
            RagComponentConfig.QueryRouterConfig routerConfig = ragConfig.getQueryRouterOrDefault();
            if (routerConfig.getModelId() != null && routerConfig.getModelId() <= 0) {
                log.warn("查询路由器modelId必须为正数，当前值: {}", routerConfig.getModelId());
                routerConfig.setModelId(null);
            }
            
            log.debug("RAG配置验证完成: ragConfig={}", ragConfig);
            return ragConfig;
            
        } catch (Exception e) {
            log.error("RAG配置验证失败，将使用默认配置", e);
            return null;
        }
    }

    /**
     * 生成会话ID
     */
    private String generateSessionId(AiAppChatCmd cmd) {
        return idGeneratorGateway.generateIdStr();
    }

    /**
     * 处理错误 - 增强版本，支持客户端断开识别
     */
    private void handleError(SseEmitter emitter, Long appId, String sessionId, Exception e) {
        // 检查是否为客户端断开相关异常
        if (isClientDisconnectException(e)) {
            log.warn("客户端断开连接: appId={}, sessionId={}, error={}", 
                     appId, sessionId, e.getMessage());
        } else {
            log.error("AI应用聊天处理失败: appId={}, sessionId={}, error={}", 
                     appId, sessionId, e.getMessage(), e);
        }
        
        try {
            // 只有在非客户端断开的情况下才尝试发送错误消息
            if (!isClientDisconnectException(e)) {
                sendSSEMessage(emitter, AiAppChatSSEMessage.error(sessionId, "聊天处理失败: " + e.getMessage()));
            }
        } catch (Exception ioException) {
            // 发送错误消息失败，可能也是客户端断开
            if (isClientDisconnectException(ioException)) {
                log.warn("发送错误消息时检测到客户端断开: sessionId={}", sessionId);
            } else {
                log.error("发送错误消息失败: sessionId={}", sessionId, ioException);
            }
        } finally {
            // 确保始终完成emitter
            try {
                emitter.complete();
            } catch (Exception completeException) {
                log.debug("完成emitter时出现异常: sessionId={}, error={}", sessionId, completeException.getMessage());
            }
        }
    }
    
    /**
     * 判断是否为客户端断开相关异常
     */
    private boolean isClientDisconnectException(Exception e) {
        if (e == null) return false;
        
        String message = e.getMessage();
        String className = e.getClass().getSimpleName();
        
        // 检查异常类型
        if (e instanceof java.io.IOException || 
            e instanceof java.net.SocketException ||
            e instanceof org.springframework.web.context.request.async.AsyncRequestNotUsableException ||
            className.contains("ClientAbort")) {
            return true;
        }
        
        // 检查异常消息中的关键词
        if (message != null) {
            String lowerMessage = message.toLowerCase();
            return lowerMessage.contains("broken pipe") ||
                   lowerMessage.contains("connection reset") ||
                   lowerMessage.contains("client abort") ||
                   lowerMessage.contains("connection closed") ||
                   lowerMessage.contains("connection was closed") ||
                   lowerMessage.contains("socket closed") ||
                   lowerMessage.contains("stream closed") ||
                   lowerMessage.contains("response already committed");
        }
        
        return false;
    }

    /**
     * 设置发射器回调
     */
    private void setupEmitterCallbacks(SseEmitter emitter, Long appId, String sessionId) {
        emitter.onTimeout(() -> {
            log.warn("AI应用聊天超时: appId={}, sessionId={}", appId, sessionId);
            try {
                sendSSEMessage(emitter, AiAppChatSSEMessage.timeout(sessionId));
            } catch (IOException e) {
                log.error("发送超时消息失败", e);
            }
            emitter.complete();
        });

        emitter.onCompletion(() -> {
            log.info("AI应用聊天完成: appId={}, sessionId={}", appId, sessionId);
        });

        emitter.onError(throwable -> {
            log.error("AI应用聊天出错: appId={}, sessionId={}, error={}", 
                     appId, sessionId, throwable.getMessage(), throwable);
        });
    }

    /**
     * 发送SSE消息
     */
    private void sendSSEMessage(SseEmitter emitter, AiAppChatSSEMessage message) throws IOException {
        emitter.send(SseEmitter.event()
                .id(message.getId())
                .name(message.getType().getValue())
                .data(message));
    }
    
    /**
     * 执行输入内容审核
     * 使用AI模型进行快速内容预检
     */
    private boolean performInputModeration(String content, Long modelId, String sessionId) {
        try {
            log.debug("开始输入内容审核: sessionId={}, contentLength={}", sessionId, content.length());
            
            // 使用LangChain4j审核服务进行快速审核
            var moderationResult = langChain4jModerationService.quickModerate(content, modelId);
            var quickResult = moderationResult.get(5, java.util.concurrent.TimeUnit.SECONDS);
            
            // 检查审核结果，如果不安全则阻断
            boolean isBlocked = quickResult.isBlocked() || quickResult.requiresReview();
            if (isBlocked) {
                log.warn("输入内容被阻断: sessionId={}, result={}", sessionId, quickResult.getResult());
                return false;
            }
            
            log.debug("输入内容审核通过: sessionId={}, result={}", sessionId, quickResult.getResult());
            return true;
            
        } catch (Exception e) {
            // 审核失败时采用宽松策略，允许通过但记录日志
            log.warn("输入内容审核失败，采用宽松策略允许通过: sessionId={}", sessionId, e);
            return true;
        }
    }
    
    
    /**
     * 执行输出内容审核（异步）
     * 对AI生成的回答进行内容安全检查
     */
    private void performOutputModeration(String content, Long modelId, String sessionId) {
        try {
            log.debug("开始输出内容审核: sessionId={}, contentLength={}", sessionId, content.length());
            
            // 异步执行详细审核，不阻塞响应
            langChain4jModerationService.moderateContent(content, modelId)
                .thenAccept(moderationResult -> {
                    log.info("输出内容审核完成: sessionId={}, result={}", 
                            sessionId, moderationResult.getResult());
                    
                    // 如果检测到违规内容，记录日志并可能触发后续处理
                    var result = moderationResult.getResult();
                    if (result != null && (result.name().equals("REJECTED") || result.name().equals("NEEDS_REVIEW"))) {
                        log.warn("检测到输出违规内容: sessionId={}, result={}, violations={}", 
                                sessionId, result, moderationResult.getViolations());
                        // 这里可以添加违规内容的后续处理逻辑
                        // 例如：通知管理员、记录违规记录等
                    }
                })
                .exceptionally(throwable -> {
                    log.warn("输出内容审核失败: sessionId={}", sessionId, throwable);
                    return null;
                });
                
        } catch (Exception e) {
            log.warn("启动输出内容审核失败: sessionId={}", sessionId, e);
        }
    }
    
    /**
     * 创建智能聊天服务
     * 
     * @param modelId 模型ID
     * @param ragConfig RAG配置
     * @return SmartChatService实例
     */
    private SmartChatService createSmartChatService(Long modelId, RagComponentConfig ragConfig) {
        log.info("创建SmartChatService: modelId={}, ragConfig={}", modelId, ragConfig);
        
        try {
            // 获取模型对应的ChatModel和StreamingChatModel
            ChatModel chatModel = modelProvider.getChatModel(modelId);
            StreamingChatModel streamingChatModel = modelProvider.getStreamingChatModel(modelId);
            
            // 创建RAG增强器
            RetrievalAugmentor retrievalAugmentor = ragAugmentorFactory.createRetrievalAugmentor(modelId, ragConfig);
            
            // 使用LangChain4j AiServices框架创建推理服务
            return AiServices.builder(SmartChatService.class)
                    .chatModel(chatModel)
                    .streamingChatModel(streamingChatModel)
                    .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                            .id(memoryId)
                            .maxMessages(20)
                            .chatMemoryStore(chatMemoryStore)
                            .build())
                    .retrievalAugmentor(retrievalAugmentor)
                    .build();
            
        } catch (Exception e) {
            log.error("创建SmartChatService失败: modelId={}, ragConfig={}", modelId, ragConfig, e);
            throw new RuntimeException("无法创建SmartChatService: " + e.getMessage(), e);
        }
    }
}