package com.leyue.smartcs.rag.memory;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.leyue.smartcs.domain.ltm.domainservice.LTMDomainService;
import com.leyue.smartcs.domain.ltm.domainservice.LTMDomainService.LTMContext;
import com.leyue.smartcs.domain.ltm.domainservice.LTMDomainService.MemoryFormationRequest;
import com.leyue.smartcs.domain.ltm.domainservice.LTMDomainService.MemoryRetrievalRequest;
import com.leyue.smartcs.domain.ltm.entity.EpisodicMemory;
import com.leyue.smartcs.service.TracingSupport;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * LTM增强的Redis聊天记忆存储
 * 在原有ChatMemoryStore基础上集成长期记忆功能
 */
@Component("ltmEnhancedChatMemoryStore")
@RequiredArgsConstructor
@Slf4j
public class LTMEnhancedRedisChatMemoryStore implements ChatMemoryStore {

    private final ChatMemoryStore baseChatMemoryStore;
    private final LTMDomainService ltmDomainService;

    @Value("${smartcs.ai.ltm.enabled:true}")
    private boolean ltmEnabled;

    @Value("${smartcs.ai.ltm.context.max-results:5}")
    private int maxLtmResults;

    @Value("${smartcs.ai.ltm.context.threshold:0.8}")
    private double ltmThreshold;

    @Value("${smartcs.ai.ltm.memory-formation.enabled:true}")
    private boolean memoryFormationEnabled;

    @Override
    @SentinelResource(value = "ltm-enhanced-memory:getMessages",
            blockHandler = "getMessagesBlockHandler",
            fallback = "getMessagesFallback")
    public List<ChatMessage> getMessages(Object memoryId) {
        log.debug("获取LTM增强的聊天记忆: memoryId={}", memoryId);

        // 获取基础聊天记忆
        List<ChatMessage> baseMessages = baseChatMemoryStore.getMessages(memoryId);
        
        if (!ltmEnabled) {
            return baseMessages;
        }

        // 提取用户ID（假设memoryId格式为 "userId:sessionId"）
        Long userId = extractUserIdFromMemoryId(memoryId);
        if (userId == null) {
            log.warn("无法从memoryId提取用户ID: {}", memoryId);
            return baseMessages;
        }

        try {
            // 获取LTM上下文
            LTMContext ltmContext = retrieveLTMContext(userId, baseMessages);
            
            if (!ltmContext.isEmpty()) {
                // 将LTM上下文集成到消息列表中
                return integrateContextIntoMessages(baseMessages, ltmContext);
            }
        } catch (Exception e) {
            log.warn("获取LTM上下文失败，使用基础消息: userId={}, error={}", userId, e.getMessage());
        }

        return baseMessages;
    }

    /**
     * 获取消息的降级方法
     */
    public List<ChatMessage> getMessagesFallback(Object memoryId, Throwable e) {
        log.warn("LTM增强获取消息失败，使用基础存储: memoryId={}", memoryId, e);
        return baseChatMemoryStore.getMessages(memoryId);
    }

    public List<ChatMessage> getMessagesBlockHandler(Object memoryId, BlockException ex) {
        log.warn("LTM增强获取消息触发限流/降级: memoryId={}, rule={}", memoryId, ex.getRule());
        return getMessagesFallback(memoryId, ex);
    }

    @Override
    @SentinelResource(value = "ltm-enhanced-memory:updateMessages",
            blockHandler = "updateMessagesBlockHandler",
            fallback = "updateMessagesFallback")
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        log.debug("更新LTM增强的聊天记忆: memoryId={}", memoryId);

        // 更新基础聊天记忆
        baseChatMemoryStore.updateMessages(memoryId, messages);

        if (!ltmEnabled || !memoryFormationEnabled) {
            return;
        }

        // 提取用户ID和会话ID
        Long userId = extractUserIdFromMemoryId(memoryId);
        Long sessionId = extractSessionIdFromMemoryId(memoryId);
        
        if (userId == null) {
            log.warn("无法从memoryId提取用户ID，跳过记忆形成: {}", memoryId);
            return;
        }

        try {
            // 异步处理记忆形成
            CompletableFuture.runAsync(() -> formMemoriesFromMessages(userId, sessionId, messages))
                .exceptionally(throwable -> {
                    log.warn("记忆形成处理失败: userId={}, error={}", userId, throwable.getMessage());
                    return null;
                });
        } catch (Exception e) {
            log.warn("启动记忆形成处理失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    /**
     * 更新消息的降级方法
     */
    public void updateMessagesFallback(Object memoryId, List<ChatMessage> messages, Throwable e) {
        log.warn("LTM增强更新消息失败，使用基础存储: memoryId={}", memoryId, e);
        baseChatMemoryStore.updateMessages(memoryId, messages);
    }

    public void updateMessagesBlockHandler(Object memoryId, List<ChatMessage> messages, BlockException ex) {
        log.warn("LTM增强更新消息触发限流/降级: memoryId={}, rule={}", memoryId, ex.getRule());
        updateMessagesFallback(memoryId, messages, ex);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        log.debug("删除LTM增强的聊天记忆: memoryId={}", memoryId);
        baseChatMemoryStore.deleteMessages(memoryId);

        // Note: LTM记忆通常不随聊天记忆删除而删除，因为它们是长期保存的
        // 如果需要删除LTM记忆，应该通过专门的管理接口进行
    }

    /**
     * 从memoryId提取用户ID
     */
    private Long extractUserIdFromMemoryId(Object memoryId) {
        if (memoryId == null) {
            return null;
        }
        
        String memoryIdStr = memoryId.toString();
        // 假设格式为 "userId:sessionId" 或 "userId"
        String[] parts = memoryIdStr.split(":");
        
        try {
            return Long.valueOf(parts[0]);
        } catch (NumberFormatException e) {
            log.debug("无法解析用户ID: {}", memoryIdStr);
            return null;
        }
    }

    /**
     * 从memoryId提取会话ID
     */
    private Long extractSessionIdFromMemoryId(Object memoryId) {
        if (memoryId == null) {
            return null;
        }
        
        String memoryIdStr = memoryId.toString();
        String[] parts = memoryIdStr.split(":");
        
        if (parts.length >= 2) {
            try {
                return Long.valueOf(parts[1]);
            } catch (NumberFormatException e) {
                log.debug("无法解析会话ID: {}", memoryIdStr);
            }
        }
        
        return null;
    }

    /**
     * 检索LTM上下文
     */
    private LTMContext retrieveLTMContext(Long userId, List<ChatMessage> messages) {
        // 构建查询上下文
        String query = extractQueryFromMessages(messages);
        Map<String, Object> context = buildContextFromMessages(messages);

        MemoryRetrievalRequest request = new MemoryRetrievalRequest(
            userId, 
            query, 
            null, // queryVector，如果有embedding模型可以生成
            context, 
            maxLtmResults, 
            ltmThreshold
        );

        return ltmDomainService.retrieveMemoryContext(request);
    }

    /**
     * 从消息中提取查询内容
     */
    private String extractQueryFromMessages(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return "";
        }

        // 获取最后几条用户消息作为查询上下文
        return messages.stream()
            .filter(msg -> msg instanceof UserMessage)
            .map(ChatMessage::text)
            .reduce((first, second) -> second) // 获取最后一条
            .orElse("");
    }

    /**
     * 从消息中构建上下文信息
     */
    private Map<String, Object> buildContextFromMessages(List<ChatMessage> messages) {
        Map<String, Object> context = new HashMap<>();
        
        if (messages != null && !messages.isEmpty()) {
            // 统计消息类型
            long userMessageCount = messages.stream().filter(msg -> msg instanceof UserMessage).count();
            long aiMessageCount = messages.stream().filter(msg -> msg instanceof AiMessage).count();
            
            context.put("user_message_count", userMessageCount);
            context.put("ai_message_count", aiMessageCount);
            context.put("total_message_count", messages.size());
            context.put("conversation_active", true);
            context.put("timestamp", System.currentTimeMillis());
        }
        
        return context;
    }

    /**
     * 将LTM上下文集成到消息列表中
     */
    private List<ChatMessage> integrateContextIntoMessages(List<ChatMessage> baseMessages, LTMContext ltmContext) {
        List<ChatMessage> enhancedMessages = new ArrayList<>(baseMessages);

        // 构建LTM上下文描述
        String ltmContextDescription = buildLTMContextDescription(ltmContext);

        if (!ltmContextDescription.isEmpty()) {
            // 在系统消息或消息开头插入LTM上下文
            SystemMessage ltmContextMessage = SystemMessage.from(
                "基于用户历史记忆的上下文信息：\n" + ltmContextDescription + 
                "\n\n请结合这些历史信息为用户提供个性化的帮助。"
            );
            
            // 将LTM上下文插入到消息列表开头（在现有系统消息之后）
            int insertIndex = findSystemMessageInsertIndex(enhancedMessages);
            enhancedMessages.add(insertIndex, ltmContextMessage);
        }

        return enhancedMessages;
    }

    /**
     * 查找系统消息插入位置
     */
    private int findSystemMessageInsertIndex(List<ChatMessage> messages) {
        // 查找最后一个系统消息的位置
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (messages.get(i) instanceof SystemMessage) {
                return i + 1;
            }
        }
        // 如果没有系统消息，插入到开头
        return 0;
    }

    /**
     * 构建LTM上下文描述
     */
    private String buildLTMContextDescription(LTMContext ltmContext) {
        StringBuilder contextDesc = new StringBuilder();

        // 情景记忆
        if (ltmContext.getEpisodicMemories() != null && !ltmContext.getEpisodicMemories().isEmpty()) {
            contextDesc.append("相关历史对话：\n");
            ltmContext.getEpisodicMemories().stream()
                .limit(3) // 限制数量
                .forEach(memory -> {
                    contextDesc.append("- ").append(memory.getContent()).append("\n");
                });
            contextDesc.append("\n");
        }

        // 语义记忆
        if (ltmContext.getSemanticMemories() != null && !ltmContext.getSemanticMemories().isEmpty()) {
            contextDesc.append("用户相关知识：\n");
            ltmContext.getSemanticMemories().stream()
                .limit(3)
                .forEach(memory -> {
                    contextDesc.append("- ").append(memory.getConcept())
                             .append(": ").append(memory.getKnowledge()).append("\n");
                });
            contextDesc.append("\n");
        }

        // 程序性记忆（偏好和规则）
        if (ltmContext.getProceduralMemories() != null && !ltmContext.getProceduralMemories().isEmpty()) {
            contextDesc.append("用户偏好和习惯：\n");
            ltmContext.getProceduralMemories().stream()
                .filter(memory -> memory.getIsActive())
                .limit(3)
                .forEach(memory -> {
                    contextDesc.append("- ").append(memory.getPatternName())
                             .append(": ").append(memory.getPatternDescription()).append("\n");
                });
        }

        return contextDesc.toString().trim();
    }

    /**
     * 从消息形成记忆
     */
    private void formMemoriesFromMessages(Long userId, Long sessionId, List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }

        try {
            // 获取最新的对话片段
            List<ChatMessage> recentMessages = messages.stream()
                .skip(Math.max(0, messages.size() - 4)) // 获取最后4条消息
                .collect(Collectors.toList());

            // 构建记忆内容
            String content = recentMessages.stream()
                .map(msg -> msg.type().toString() + ": " + msg.text())
                .collect(Collectors.joining("\n"));

            // 构建上下文
            Map<String, Object> context = new HashMap<>();
            context.put("message_count", recentMessages.size());
            context.put("conversation_turn", messages.size() / 2); // 粗略估算对话轮数
            context.put("last_message_type", messages.get(messages.size() - 1).type().toString());

            // 创建记忆形成请求
            MemoryFormationRequest request = new MemoryFormationRequest(
                userId,
                sessionId,
                content,
                context,
                System.currentTimeMillis()
            );

            // 调用LTM服务形成记忆
            ltmDomainService.formMemory(request);

            log.debug("成功形成记忆: userId={}, sessionId={}", userId, sessionId);
        } catch (Exception e) {
            log.warn("记忆形成失败: userId={}, sessionId={}, error={}", userId, sessionId, e.getMessage());
        }
    }
}
