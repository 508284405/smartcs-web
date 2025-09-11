package com.leyue.smartcs.app.orchestrator;

import com.leyue.smartcs.moderation.service.LangChain4jModerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * 内容审核编排服务
 * 专门处理内容审核相关的集成流程
 * 注：意图识别已集成到RAG QueryTransformer中，会话管理使用LangChain4j原生能力
 * 
 * @author Claude
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class IntelligentChatOrchestrator {

    private final LangChain4jModerationService langChain4jModerationService;

    /**
     * 执行输入内容审核（同步）
     */
    public boolean performInputModeration(String content, Long modelId, String sessionId) {
        try {
            log.debug("开始输入内容审核: sessionId={}, contentLength={}", sessionId, content.length());
            
            var moderationResult = langChain4jModerationService.quickModerate(content, modelId);
            var quickResult = moderationResult.get(5, java.util.concurrent.TimeUnit.SECONDS);
            
            boolean isBlocked = quickResult.isBlocked() || quickResult.requiresReview();
            if (isBlocked) {
                log.warn("输入内容被阻断: sessionId={}, result={}", sessionId, quickResult.getResult());
                return false;
            }
            
            log.debug("输入内容审核通过: sessionId={}, result={}", sessionId, quickResult.getResult());
            return true;
            
        } catch (Exception e) {
            log.warn("输入内容审核失败，采用宽松策略允许通过: sessionId={}", sessionId, e);
            return true;
        }
    }

    /**
     * 执行输出审核（异步）
     */
    public CompletableFuture<Void> performOutputModeration(String content, Long modelId, String sessionId) {
        return langChain4jModerationService.moderateContent(content, modelId)
            .thenAccept(moderationResult -> {
                log.info("输出内容审核完成: sessionId={}, result={}", 
                        sessionId, moderationResult.getResult());
                
                // 检查违规内容
                var result = moderationResult.getResult();
                if (result != null && (result.name().equals("REJECTED") || result.name().equals("NEEDS_REVIEW"))) {
                    log.warn("检测到输出违规内容: sessionId={}, result={}, violations={}", 
                            sessionId, result, moderationResult.getViolations());
                    
                    // 触发违规处理流程
                    handleViolationContent(sessionId, moderationResult);
                }
            })
            .exceptionally(throwable -> {
                log.warn("输出内容审核失败: sessionId={}", sessionId, throwable);
                return null;
            });
    }

    /**
     * 处理违规内容
     */
    private void handleViolationContent(String sessionId, Object moderationResult) {
        // 这里可以实现违规内容的后续处理逻辑
        // 例如：发送通知、记录违规日志、触发人工审核等
        log.warn("触发违规内容处理流程: sessionId={}", sessionId);
        
        // 可以发送事件到消息队列进行异步处理
        // eventPublisher.publishEvent(new ViolationContentEvent(sessionId, moderationResult));
    }
}