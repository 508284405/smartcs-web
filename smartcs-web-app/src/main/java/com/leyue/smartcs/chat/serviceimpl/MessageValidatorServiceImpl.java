package com.leyue.smartcs.chat.serviceimpl;

import com.leyue.smartcs.api.MessageValidatorService;
import com.leyue.smartcs.dto.chat.ws.ChatMessage;
import com.leyue.smartcs.domain.chat.gateway.SessionGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 消息验证器实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageValidatorServiceImpl implements MessageValidatorService {

    private final SessionGateway sessionGateway;

    @Override
    public void validate(ChatMessage message) throws IllegalArgumentException {
        // 检查必填字段
        if (message == null) {
            throw new IllegalArgumentException("消息不能为空");
        }

        if (message.getSessionId() == null) {
            throw new IllegalArgumentException("会话ID不能为空");
        }

        if (!StringUtils.hasText(message.getContent())) {
            throw new IllegalArgumentException("消息内容不能为空");
        }

        // 验证会话是否存在
        boolean sessionExists = sessionGateway.checkSessionExists(message.getSessionId());
        if (!sessionExists) {
            throw new IllegalArgumentException("会话不存在或已关闭: " + message.getSessionId());
        }

        // 验证会话状态
        String sessionStatus = sessionGateway.getSessionStatus(message.getSessionId());
        if ("CLOSED".equals(sessionStatus)) {
            throw new IllegalArgumentException("会话已关闭，无法发送消息");
        }

        // 验证内容长度
        if (message.getContent() != null && message.getContent().length() > 5000) {
            throw new IllegalArgumentException("消息内容过长，最大允许5000字符");
        }

        // 验证内容类型
        if (!StringUtils.hasText(message.getMessageType())) {
            // 默认设置为文本类型
            message.setMessageType("TEXT");
        } else {
            // 验证内容类型是否有效
            String contentType = message.getMessageType().toUpperCase();
            if (!contentType.equals("TEXT") &&
                    !contentType.equals("IMAGE") &&
                    !contentType.equals("FILE") &&
                    !contentType.equals("AUDIO") &&
                    !contentType.equals("VIDEO")) {
                throw new IllegalArgumentException("无效的内容类型: " + message.getMessageType());
            }

            // 设置标准化的内容类型
            message.setMessageType(contentType);
        }

        log.debug("消息验证通过: {}", message);
    }
}
