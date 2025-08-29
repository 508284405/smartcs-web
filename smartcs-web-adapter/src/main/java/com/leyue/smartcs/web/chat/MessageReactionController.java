package com.leyue.smartcs.web.chat;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.api.MessageService;
import com.leyue.smartcs.dto.chat.AddReactionCmd;
import com.leyue.smartcs.dto.chat.ReactionDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 消息表情反应控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/chat/messages")
@RequiredArgsConstructor
@Tag(name = "消息表情反应", description = "消息表情反应相关API")
public class MessageReactionController {
    
    private final MessageService messageService;
    
    /**
     * 添加或切换表情反应
     */
    @PostMapping("/{msgId}/reactions")
    @Operation(summary = "添加或切换表情反应", description = "为消息添加表情反应，如果已存在则切换")
    public Response addReaction(
            @Parameter(description = "消息ID") @PathVariable String msgId,
            @Valid @RequestBody AddReactionCmd cmd) {
        
        log.info("添加表情反应请求: msgId={}, cmd={}", msgId, cmd);
        
        // 设置消息ID
        cmd.setMsgId(msgId);
        
        return messageService.addReaction(cmd);
    }
    
    /**
     * 获取消息的表情反应统计
     */
    @GetMapping("/{msgId}/reactions")
    @Operation(summary = "获取消息表情反应", description = "获取指定消息的所有表情反应统计")
    public SingleResponse<List<ReactionDTO>> getMessageReactions(
            @Parameter(description = "消息ID") @PathVariable String msgId) {
        
        log.info("查询消息表情反应: msgId={}", msgId);
        
        return messageService.getMessageReactions(msgId);
    }
    
    /**
     * 移除表情反应
     */
    @DeleteMapping("/{msgId}/reactions/{emoji}")
    @Operation(summary = "移除表情反应", description = "移除用户对消息的特定表情反应")
    public Response removeReaction(
            @Parameter(description = "消息ID") @PathVariable String msgId,
            @Parameter(description = "表情符号") @PathVariable String emoji,
            @Parameter(description = "会话ID") @RequestParam String sessionId) {
        
        log.info("移除表情反应请求: msgId={}, emoji={}, sessionId={}", msgId, emoji, sessionId);
        
        AddReactionCmd cmd = new AddReactionCmd();
        cmd.setMsgId(msgId);
        cmd.setSessionId(sessionId);
        cmd.setEmoji(emoji);
        cmd.setAction("remove");
        
        return messageService.addReaction(cmd);
    }
}