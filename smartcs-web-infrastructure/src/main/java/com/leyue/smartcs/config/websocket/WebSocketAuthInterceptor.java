package com.leyue.smartcs.config.websocket;

import com.leyue.smartcs.api.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * WebSocket认证拦截器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {
    private final UserService userService;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        Object raw = message.getPayload();
        String body;
        if (raw instanceof byte[] bytes) {
            // 最常见的情况：payload 是 byte[]，这里按 UTF-8 解码
            body = new String(bytes, StandardCharsets.UTF_8);
        } else {
            // 其他情况：直接调用 toString()
            body = raw.toString();
        }
        log.debug("WebSocket 拦截到消息，payload 转为字符串后内容：{}", body);
        return message;
    }
}
