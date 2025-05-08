package com.leyue.smartcs.domain.chat.validator;

import com.leyue.smartcs.api.chat.dto.websocket.ChatMessage;
import com.leyue.smartcs.domain.chat.gateway.SessionGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 消息验证器实现类
 */
@Slf4j
@Service
public class MessageValidatorImpl implements MessageValidator {

    private final SessionGateway sessionGateway;

    @Autowired
    public MessageValidatorImpl(SessionGateway sessionGateway) {
        this.sessionGateway = sessionGateway;
    }

    @Override
    public void validate(ChatMessage message) throws IllegalArgumentException {
        // 检查必填字段
        if (message == null) {
            throw new IllegalArgumentException("消息不能为空");
        }
        
        if (!StringUtils.hasText(message.getSessionId())) {
            throw new IllegalArgumentException("会话ID不能为空");
        }
        
        if (!StringUtils.hasText(message.getFromUserId())) {
            throw new IllegalArgumentException("发送者ID不能为空");
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
        if (!StringUtils.hasText(message.getContentType())) {
            // 默认设置为文本类型
            message.setContentType("TEXT");
        } else {
            // 验证内容类型是否有效
            String contentType = message.getContentType().toUpperCase();
            if (!contentType.equals("TEXT") && 
                !contentType.equals("IMAGE") && 
                !contentType.equals("FILE") && 
                !contentType.equals("AUDIO") && 
                !contentType.equals("VIDEO")) {
                throw new IllegalArgumentException("无效的内容类型: " + message.getContentType());
            }
            
            // 设置标准化的内容类型
            message.setContentType(contentType);
        }
        
        log.debug("消息验证通过: {}", message);
    }
}
