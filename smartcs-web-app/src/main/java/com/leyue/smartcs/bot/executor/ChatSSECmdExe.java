package com.leyue.smartcs.bot.executor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.alibaba.cola.exception.BizException;
import com.alibaba.fastjson2.JSON;
import com.leyue.smartcs.config.context.UserContext;
import com.leyue.smartcs.domain.bot.BotProfile;
import com.leyue.smartcs.domain.bot.PromptTemplate;
import com.leyue.smartcs.domain.bot.gateway.BotProfileGateway;
import com.leyue.smartcs.domain.bot.gateway.LLMGateway;
import com.leyue.smartcs.domain.bot.gateway.PromptTemplateGateway;
import com.leyue.smartcs.domain.chat.Message;
import com.leyue.smartcs.domain.chat.Session;
import com.leyue.smartcs.domain.chat.domainservice.MessageDomainService;
import com.leyue.smartcs.domain.chat.domainservice.SessionDomainService;
import com.leyue.smartcs.domain.chat.enums.MessageType;
import com.leyue.smartcs.domain.chat.enums.SenderRole;
import com.leyue.smartcs.domain.chat.enums.SessionState;
import com.leyue.smartcs.domain.common.Constants;
import com.leyue.smartcs.domain.knowledge.Chunk;
import com.leyue.smartcs.domain.knowledge.gateway.ChunkGateway;
import com.leyue.smartcs.domain.knowledge.gateway.SearchGateway;
import com.leyue.smartcs.dto.bot.BotChatSSERequest;
import com.leyue.smartcs.dto.bot.BotChatSSEResponse;
import com.leyue.smartcs.dto.bot.SSEMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SSE聊天命令执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatSSECmdExe {

    private final MessageDomainService messageDomainService;
    private final LLMGateway llmGateway;
    private final PromptTemplateGateway promptTemplateGateway;
    private final SearchGateway searchGateway;
    private final ChunkGateway chunkGateway;
    private final BotProfileGateway botProfileGateway;
    private final SessionDomainService sessionDomainService;

    // 历史消息数量限制
    private static final int MAX_HISTORY_MESSAGES = 50;

    /**
     * 执行SSE聊天命令
     */
    public void execute(BotChatSSERequest request, SseEmitter emitter) throws IOException {
        long startTime = System.currentTimeMillis();
        Long sessionId = request.getSessionId();

        try {
            // 参数校验
            if (request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
                throw new BizException("问题不能为空");
            }

            Session session = sessionDomainService.findById(sessionId);
            if (session == null) {
                throw new BizException("会话不存在");
            }
            // 如果会话状态为排队中，则更新为进行中
            if (session.getSessionState() == SessionState.WAITING) {
                sessionDomainService.updateSessionStatus(sessionId, SessionState.ACTIVE);
            }

            // 如果会话名称为空，则更新名称(question的前15个字符)；注意索引越界问题
            if (session.getSessionName() == null || session.getSessionName().isEmpty()) {
                sessionDomainService.updateSessionName(sessionId, request.getQuestion().length() > 15
                        ? request.getQuestion().substring(0, 15)
                        : request.getQuestion());
            }

            // 发送进度消息：开始处理
            sendProgressMessage(emitter, sessionId, "开始处理您的问题...");

            // 从chat message服务获取历史消息
            List<Message> historyMessages = new ArrayList<>();
            if (Boolean.TRUE.equals(request.getIncludeHistory())) {
                historyMessages = messageDomainService.getSessionMessagesWithPagination(sessionId, 0,
                        MAX_HISTORY_MESSAGES);
            }

            // 存储用户问题到chat message服务
            messageDomainService.sendMessage(
                    sessionId,
                    UserContext.getCurrentUser().getId(),
                    SenderRole.USER,
                    MessageType.TEXT,
                    request.getQuestion(),
                    null);

            // 发送进度消息：检索知识
            sendProgressMessage(emitter, sessionId, "正在检索相关知识...");

            // 获取知识检索结果
            List<Map<String, Object>> searchResults = getSearchResults(request, 5);

            // 发送进度消息：构建提示
            sendProgressMessage(emitter, sessionId, "正在构建AI提示...");

            // 构建Messages
            List<org.springframework.ai.chat.messages.Message> messages = buildMessages(request, historyMessages, searchResults);
            log.info("构建的Messages数量: {}", messages.size());

            // 发送进度消息：调用AI
            sendProgressMessage(emitter, sessionId, "正在调用AI生成回答...");

            // 使用流式生成
            StringBuilder fullAnswer = new StringBuilder();
            llmGateway.generateAnswerStream(messages, request.getTargetBotId(), (chunk) -> {
                try {
                    fullAnswer.append(chunk);

                    // 发送流式数据
                    BotChatSSEResponse response = BotChatSSEResponse.builder()
                            .sessionId(sessionId)
                            .answer(chunk)
                            .finished(false)
                            .build();

                    sendDataMessage(emitter, sessionId, response);
                } catch (IOException e) {
                    log.error("发送流式数据失败: {}", e.getMessage());
                    throw new RuntimeException(e);
                }
            });

            // 存储消息
            messageDomainService.sendMessage(
                    sessionId,
                    request.getTargetBotId(),
                    SenderRole.BOT,
                    MessageType.TEXT,
                    fullAnswer.toString(),
                    null);

            // 构建最终响应
            BotChatSSEResponse finalResponse = buildFinalResponse(sessionId, fullAnswer.toString(), searchResults,
                    startTime);

            // 发送完成消息
            sendCompleteMessage(emitter, sessionId, finalResponse);

            log.info("SSE聊天命令执行完成，耗时: {}ms", System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error("SSE聊天命令执行失败: {}", e.getMessage(), e);
            sendErrorMessage(emitter, sessionId, e.getMessage());
        }
    }

    /**
     * 获取知识检索结果
     */
    private List<Map<String, Object>> getSearchResults(BotChatSSERequest request, Integer topK) {
        List<Map<String, Object>> results = new ArrayList<>();

        try {
            int searchTopK = topK != null ? topK : 5;

            int remainingCount = searchTopK - results.size();
            Map<Long, Double> embSearchResults = searchGateway.searchTopK(Constants.EMBEDDING_INDEX_REDISEARCH,
                    request.getQuestion(), remainingCount, request.getKnowledgeBaseId(), request.getContentId());
            for (Map.Entry<Long, Double> entry : embSearchResults.entrySet()) {
                Chunk embedding = chunkGateway.findById(entry.getKey());
                if (embedding != null) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("type", "DOC");
                    result.put("contentId", embedding.getContentId());
                    result.put("title", "文档段落");
                    result.put("snippet", embedding.getText());
                    result.put("score", entry.getValue());
                    results.add(result);
                }
            }

        } catch (Exception e) {
            log.error("知识检索失败: {}", e.getMessage(), e);
        }

        return results;
    }

    /**
     * 构建Messages
     */
    private List<org.springframework.ai.chat.messages.Message> buildMessages(BotChatSSERequest request, List<Message> historyMessages,
            List<Map<String, Object>> searchResults) {
        try {
            List<org.springframework.ai.chat.messages.Message> messages = new ArrayList<>();
            
            Optional<BotProfile> bOptional = botProfileGateway.findById(request.getTargetBotId());
            if (!bOptional.isPresent()) {
                throw new BizException("机器人不存在");
            }
            BotProfile botProfile = bOptional.get();
            if (botProfile.getEnabled() == null || !botProfile.getEnabled()) {
                throw new BizException("机器人已禁用");
            }

            // 构建系统消息
            StringBuilder systemContent = new StringBuilder();
            
            // 添加prompt模板
            Optional<PromptTemplate> templateOpt = promptTemplateGateway.findByTemplateKey(botProfile.getPromptKey());
            if (templateOpt.isPresent()) {
                systemContent.append(templateOpt.get().getTemplateContent()).append("\n\n");
            }

            // 添加知识文档
            if (searchResults.size() > 0) {
                StringBuilder docs = new StringBuilder();
                for (Map<String, Object> result : searchResults) {
                    docs.append("标题：").append(result.get("title")).append("\n");
                    docs.append("内容：").append(result.get("snippet")).append("\n\n");
                }
                systemContent.append("知识内容：\n").append(docs).append("\n\n");
            }
            
            if (systemContent.length() > 0) {
                messages.add(new SystemMessage(systemContent.toString()));
            }

            // 添加历史对话消息
            if (request.getIncludeHistory() && historyMessages.size() > 0) {
                for (Message historyMessage : historyMessages) {
                    if (historyMessage.getSenderRole() == SenderRole.USER) {
                        messages.add(new UserMessage(historyMessage.getContent()));
                    } else if (historyMessage.getSenderRole() == SenderRole.BOT || historyMessage.getSenderRole() == SenderRole.AGENT) {
                        messages.add(new AssistantMessage(historyMessage.getContent()));
                    }
                }
            }

            // 添加当前用户问题
            messages.add(new UserMessage(request.getQuestion()));
            
            return messages;
        } catch (Exception e) {
            log.error("构建Messages失败: {}", e.getMessage(), e);
            List<org.springframework.ai.chat.messages.Message> fallbackMessages = new ArrayList<>();
            fallbackMessages.add(new UserMessage("请回答用户问题：" + request.getQuestion()));
            return fallbackMessages;
        }
    }

    /**
     * 构建最终响应
     */
    private BotChatSSEResponse buildFinalResponse(Long sessionId, String answer,
            List<Map<String, Object>> searchResults, long startTime) {
        List<BotChatSSEResponse.KnowledgeSource> sources = searchResults.stream()
                .map(result -> BotChatSSEResponse.KnowledgeSource.builder()
                        .type((String) result.get("type"))
                        .contentId(((Number) result.get("contentId")).longValue())
                        .title((String) result.get("title"))
                        .snippet((String) result.get("snippet"))
                        .score(((Number) result.get("score")).floatValue())
                        .build())
                .collect(Collectors.toList());

        return BotChatSSEResponse.builder()
                .sessionId(sessionId)
                .answer(answer)
                .finished(true)
                .sources(sources)
                .processTime(System.currentTimeMillis() - startTime)
                .build();
    }

    /**
     * 发送进度消息
     */
    private void sendProgressMessage(SseEmitter emitter, Long sessionId, String message) throws IOException {
        SSEMessage sseMessage = SSEMessage.progress(sessionId, message);
        emitter.send(SseEmitter.event()
                .id(sseMessage.getId())
                .name("progress")
                .data(JSON.toJSONString(sseMessage)));
    }

    /**
     * 发送数据消息
     */
    private void sendDataMessage(SseEmitter emitter, Long sessionId, Object data) throws IOException {
        SSEMessage sseMessage = SSEMessage.data(sessionId, data);
        emitter.send(SseEmitter.event()
                .id(sseMessage.getId())
                .name("data")
                .data(JSON.toJSONString(sseMessage)));
    }

    /**
     * 发送完成消息
     */
    private void sendCompleteMessage(SseEmitter emitter, Long sessionId, Object finalData) throws IOException {
        SSEMessage sseMessage = SSEMessage.complete(sessionId, finalData);
        emitter.send(SseEmitter.event()
                .id(sseMessage.getId())
                .name("complete")
                .data(JSON.toJSONString(sseMessage)));
        emitter.complete();
    }

    /**
     * 发送错误消息
     */
    private void sendErrorMessage(SseEmitter emitter, Long sessionId, String error) throws IOException {
        SSEMessage sseMessage = SSEMessage.error(sessionId, error);
        emitter.send(SseEmitter.event()
                .id(sseMessage.getId())
                .name("error")
                .data(JSON.toJSONString(sseMessage)));
        emitter.complete();
    }
}