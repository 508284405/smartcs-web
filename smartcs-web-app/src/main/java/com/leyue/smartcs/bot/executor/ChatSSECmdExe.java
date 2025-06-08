package com.leyue.smartcs.bot.executor;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.alibaba.cola.exception.BizException;
import com.alibaba.fastjson2.JSON;
import com.leyue.smartcs.domain.bot.gateway.LLMGateway;
import com.leyue.smartcs.domain.chat.Session;
import com.leyue.smartcs.domain.chat.domainservice.SessionDomainService;
import com.leyue.smartcs.domain.chat.enums.SessionState;
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

    private final LLMGateway llmGateway;
    private final SessionDomainService sessionDomainService;

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

            // 对比接收人ID，不同则切换targetId
            if (session.getAgentId() != request.getTargetBotId()) {
                sessionDomainService.updateSessionAgent(sessionId, request.getTargetBotId());
            }

            // 发送进度消息：开始处理
            sendProgressMessage(emitter, sessionId, "开始处理您的问题...");

                // 发送进度消息：检索知识
            sendProgressMessage(emitter, sessionId, "正在检索相关知识...");
            
            // 发送进度消息：调用AI
            sendProgressMessage(emitter, sessionId, "正在调用AI生成回答...");

            // 发送进度消息：生成回答
            sendProgressMessage(emitter, sessionId, "正在生成回答...");

            // 根据是否选择kb或者内容id，决定是否使用RAG
            boolean isRag = request.getKnowledgeBaseId() != null || request.getContentId() != null;

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
            }, isRag);

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