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
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
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
import com.leyue.smartcs.dto.knowledge.SearchResultsDTO;

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
    private final VectorStore vectorStore;
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

            // // 从chat message服务获取历史消息
            // List<Message> historyMessages = new ArrayList<>();
            // if (Boolean.TRUE.equals(request.getIncludeHistory())) {
            //     historyMessages = messageDomainService.getSessionMessagesWithPagination(sessionId, 0,
            //             MAX_HISTORY_MESSAGES);
            // }

            // // 存储用户问题到chat message服务
            // messageDomainService.sendMessage(
            //         sessionId,
            //         UserContext.getCurrentUser().getId(),
            //         SenderRole.USER,
            //         MessageType.TEXT,
            //         request.getQuestion(),
            //         null);

            // 发送进度消息：检索知识
            sendProgressMessage(emitter, sessionId, "正在检索相关知识...");

            // 发送进度消息：调用AI
            sendProgressMessage(emitter, sessionId, "正在调用AI生成回答...");

            // 使用流式生成
            StringBuilder fullAnswer = new StringBuilder();
            llmGateway.generateAnswerStream(sessionId.toString(), request.getQuestion(), request.getTargetBotId(), (chunk) -> {
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

            // // 存储消息
            // messageDomainService.sendMessage(
            //         sessionId,
            //         request.getTargetBotId(),
            //         SenderRole.BOT,
            //         MessageType.TEXT,
            //         fullAnswer.toString(),
            //         null);

            // 构建最终响应
            BotChatSSEResponse finalResponse = buildFinalResponse(sessionId, fullAnswer.toString(),
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
     * 构建最终响应
     */
    private BotChatSSEResponse buildFinalResponse(Long sessionId, String answer,
            long startTime) {
        return BotChatSSEResponse.builder()
                .sessionId(sessionId)
                .answer(answer)
                .finished(true)
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