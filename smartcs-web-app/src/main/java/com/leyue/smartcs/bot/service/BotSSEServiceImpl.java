package com.leyue.smartcs.bot.service;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.function.ServerResponse.SseBuilder;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.alibaba.fastjson2.JSON;
import com.leyue.smartcs.api.BotSSEService;
import com.leyue.smartcs.bot.executor.ChatSSECmdExe;
import com.leyue.smartcs.dto.bot.BotChatSSERequest;
import com.leyue.smartcs.dto.bot.SSEMessage;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Bot SSE服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BotSSEServiceImpl implements BotSSEService {

    private final ChatSSECmdExe chatSSECmdExe;

    private final Executor commonThreadPoolExecutor;

    @Override
    @SneakyThrows
    public void chatSSE(BotChatSSERequest request, Object sseBuilder) {
        SseBuilder sse = (SseBuilder) sseBuilder;
        // 生成会话ID
        Long sessionId = request.getSessionId();

        // 异步处理聊天请求
        // 在 SSE（Server-Sent Events）场景里，客户端会保持与服务端的 HTTP 连接长时间不断开，服务端要持续、分片地把消息推送到这条连接上。这种“长轮询 / 长连接”如果直接由处理 HTTP 请求的主线程（Servlet 线程）来完成，会导致：
        // 线程长时间占用而无法释放，影响容器并发能力；
        // 其中的业务逻辑（模型推理、数据库 / 外部接口交互等）往往耗时且不可预估，更容易把线程池拖空；
        // 一旦逻辑里出现阻塞或异常，恢复成本高、影响面大。
        // SSE只是一座桥梁，无论是主线程还是其他任何线程都可以进行推流(在SSE关闭之前sse.complete())。
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture.runAsync(() -> {
            // 真正的业务推送由线程池里的工作线程负责，它们通过 SseBuilder 向同一 HTTP 连接不断 send() 数据。直到sse.complete()。
            RequestContextHolder.setRequestAttributes(attributes);
            try {
                // 发送开始消息
                sse.event("start").id("start_" + sessionId)
                        .send(JSON.toJSONString(SSEMessage.start(sessionId)));

                // 执行聊天命令
                chatSSECmdExe.execute(request, sse);

            } catch (Exception e) {
                log.error("SSE聊天处理失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
                try {
                    sse.event("error").id("error_" + sessionId).data(JSON.toJSONString(SSEMessage.error(sessionId, e.getMessage())));
                } catch (IOException ioException) {
                    log.error("发送错误消息失败: {}", ioException.getMessage());
                }
                sse.complete();
            } finally {
                RequestContextHolder.resetRequestAttributes();
            }
        }, commonThreadPoolExecutor);
        // Servlet 线程便结束本次请求处理，归还到应用服务器线程池，可去服务其它请求。
    }
}