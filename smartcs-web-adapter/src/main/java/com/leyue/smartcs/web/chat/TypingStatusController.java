package com.leyue.smartcs.web.chat;

import com.alibaba.cola.dto.MultiResponse;
import com.leyue.smartcs.chat.service.TypingStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * 输入状态控制器
 */
@RestController
@RequestMapping("/api/chat/typing")
@RequiredArgsConstructor
public class TypingStatusController {
    
    private final TypingStatusService typingStatusService;
    
    /**
     * 获取会话中正在输入的用户列表
     *
     * @param sessionId 会话ID
     * @return 正在输入的用户ID列表
     */
    @GetMapping("/session/{sessionId}/users")
    public MultiResponse<String> getTypingUsers(@PathVariable Long sessionId) {
        Set<String> typingUsers = typingStatusService.getTypingUsers(sessionId);
        return MultiResponse.of(typingUsers);
    }
}