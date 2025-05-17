package com.leyue.smartcs.bot.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.dto.bot.BotChatRequest;
import com.leyue.smartcs.dto.bot.BotChatResponse;
import com.leyue.smartcs.domain.bot.gateway.KnowledgeGateway;
import com.leyue.smartcs.domain.bot.gateway.LLMGateway;
import com.leyue.smartcs.domain.bot.gateway.PromptTemplateGateway;
import com.leyue.smartcs.domain.bot.gateway.SessionGateway;
import com.leyue.smartcs.domain.bot.model.Conversation;
import com.leyue.smartcs.domain.bot.model.Message;
import com.leyue.smartcs.domain.bot.model.PromptTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 聊天命令执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatCmdExe {
    
    private final LLMGateway llmGateway;
    private final KnowledgeGateway knowledgeGateway;
    private final SessionGateway sessionGateway;
    private final PromptTemplateGateway promptTemplateGateway;
    
    // 默认检索数量
    private static final int DEFAULT_TOP_K = 5;
    // 默认相似度阈值
    private static final float DEFAULT_THRESHOLD = 0.7f;
    // 默认模板标识
    private static final String DEFAULT_TEMPLATE_KEY = "RAG_QUERY";
    // 历史消息数量限制
    private static final int MAX_HISTORY_MESSAGES = 10;
    
    /**
     * 执行聊天命令
     * @param request 聊天请求
     * @return 聊天响应
     */
    public SingleResponse<BotChatResponse> execute(BotChatRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("执行聊天命令: {}", request);
        
        try {
            // 参数校验
            if (request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
                throw new BizException("问题不能为空");
            }
            
            // 生成会话ID
            String sessionId = request.getSessionId();
            if (sessionId == null || sessionId.trim().isEmpty()) {
                sessionId = UUID.randomUUID().toString();
            }
            
            // 从会话中获取历史消息
            Conversation conversation;
            Optional<Conversation> conversationOpt = sessionGateway.getConversation(sessionId);
            
            if (conversationOpt.isPresent()) {
                conversation = conversationOpt.get();
            } else {
                conversation = new Conversation();
                conversation.setSessionId(sessionId);
                conversation.setCreatedAt(System.currentTimeMillis());
            }
            
            // 添加用户问题
            conversation.addUserMessage(request.getQuestion());
            
            // 获取知识检索结果
            List<Map<String, Object>> searchResults = getSearchResults(request.getQuestion(), request.getTopK());
            
            // 构建Prompt
            String prompt = buildPrompt(request, conversation, searchResults);
            
            // 调用LLM生成回答
            Map<String, Object> options = new HashMap<>();
            if (request.getTemperature() != null) {
                options.put("temperature", request.getTemperature().doubleValue());
            }
            if (request.getMaxTokens() != null) {
                options.put("maxTokens", request.getMaxTokens());
            }
            if (request.getModel() != null && !request.getModel().isEmpty()) {
                options.put("model", request.getModel());
            }
            
            String answer = llmGateway.generateAnswer(prompt, options);
            
            // 添加助手回答
            conversation.addAssistantMessage(answer);
            
            // 保存会话
            sessionGateway.saveConversation(conversation);
            
            // 构建响应
            BotChatResponse response = buildResponse(sessionId, answer, searchResults, request.getModel(), startTime);
            
            log.info("聊天命令执行完成，耗时: {}ms", System.currentTimeMillis() - startTime);
            return SingleResponse.of(response);
            
        } catch (Exception e) {
            log.error("聊天命令执行失败: {}", e.getMessage(), e);
            throw new BizException("聊天命令执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取知识检索结果
     * @param question 问题
     * @param topK 返回数量
     * @return 检索结果
     */
    private List<Map<String, Object>> getSearchResults(String question, Integer topK) {
        int k = topK != null && topK > 0 ? topK : DEFAULT_TOP_K;
        
        // 首先尝试文本检索
        List<Map<String, Object>> textResults = knowledgeGateway.searchByText(question, k);
        
        if (!textResults.isEmpty()) {
            return textResults;
        }
        
        // 如果文本检索没有结果，则尝试向量检索（需要先生成嵌入向量）
        try {
            List<byte[]> embeddings = llmGateway.generateEmbeddings(List.of(question));
            if (!embeddings.isEmpty()) {
                return knowledgeGateway.searchByVector(embeddings.get(0), k, DEFAULT_THRESHOLD);
            }
        } catch (Exception e) {
            log.warn("向量检索失败: {}", e.getMessage());
        }
        
        return new ArrayList<>();
    }
    
    /**
     * 构建Prompt
     * @param request 聊天请求
     * @param conversation 对话
     * @param searchResults 检索结果
     * @return Prompt
     */
    private String buildPrompt(BotChatRequest request, Conversation conversation, List<Map<String, Object>> searchResults) {
        // 获取Prompt模板
        PromptTemplate promptTemplate = promptTemplateGateway.findByTemplateKey(DEFAULT_TEMPLATE_KEY)
                .orElseGet(()->{
                    return PromptTemplate.builder()
                            .templateKey(DEFAULT_TEMPLATE_KEY)
                            .templateContent("""
                                    {{question}}
                                    {{history}}
                                    """)
                            .build();
                });
        
        // 准备变量
        Map<String, Object> variables = new HashMap<>();
        
        // 设置问题
        variables.put("question", request.getQuestion());
        
        // 设置历史消息
        if (Boolean.TRUE.equals(request.getIncludeHistory())) {
            List<Message> recentMessages = conversation.getRecentMessages(MAX_HISTORY_MESSAGES);
            if (recentMessages.size() > 1) {
                // 移除最后一条消息（当前用户问题）
                recentMessages = recentMessages.subList(0, recentMessages.size() - 1);
                String history = recentMessages.stream()
                        .map(msg -> msg.getRole() + ": " + msg.getContent())
                        .collect(Collectors.joining("\n"));
                variables.put("history", history);
            } else {
                variables.put("history", "");
            }
        } else {
            variables.put("history", "");
        }
        
        // 设置知识文档
        if (!searchResults.isEmpty()) {
            StringBuilder docs = new StringBuilder();
            for (int i = 0; i < searchResults.size(); i++) {
                Map<String, Object> result = searchResults.get(i);
                String type = (String) result.get("type");
                if ("FAQ".equals(type)) {
                    docs.append("FAQ ").append(i + 1).append(":\n");
                    docs.append("问题: ").append(result.get("question")).append("\n");
                    docs.append("答案: ").append(result.get("answer")).append("\n\n");
                } else if ("DOC".equals(type)) {
                    docs.append("文档 ").append(i + 1).append(" (").append(result.get("docTitle")).append("):\n");
                    docs.append(result.get("content")).append("\n\n");
                }
            }
            variables.put("docs", docs.toString());
        } else {
            variables.put("docs", "无相关知识");
        }
        
        // 填充模板
        return promptTemplate.format(variables);
    }
    
    /**
     * 构建响应
     * @param sessionId 会话ID
     * @param answer 回答
     * @param searchResults 检索结果
     * @param modelId 模型ID
     * @param startTime 开始时间
     * @return 聊天响应
     */
    private BotChatResponse buildResponse(String sessionId, String answer, List<Map<String, Object>> searchResults, 
                                         String modelId, long startTime) {
        // 构建知识来源
        List<BotChatResponse.KnowledgeSource> sources = new ArrayList<>();
        for (Map<String, Object> result : searchResults) {
            String type = (String) result.get("type");
            BotChatResponse.KnowledgeSource source = BotChatResponse.KnowledgeSource.builder()
                    .type(type)
                    .contentId(result.get("id") instanceof Long ? (Long) result.get("id") : null)
                    .title("FAQ".equals(type) ? (String) result.get("question") : (String) result.get("docTitle"))
                    .snippet("FAQ".equals(type) ? (String) result.get("answer") : (String) result.get("content"))
                    .score(result.get("score") instanceof Float ? (Float) result.get("score") : 0f)
                    .build();
            sources.add(source);
        }
        
        // 构建响应
        return BotChatResponse.builder()
                .sessionId(sessionId)
                .answer(answer)
                .sources(sources)
                .modelId(modelId)
                .processTime(System.currentTimeMillis() - startTime)
                .build();
    }
} 