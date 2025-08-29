package com.leyue.smartcs.web.message;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.chat.executor.AddReactionCmdExe;
import com.leyue.smartcs.chat.executor.GetMessageReactionsQryExe;
import com.leyue.smartcs.dto.chat.AddReactionCmd;
import com.leyue.smartcs.dto.chat.ReactionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息表情反应控制器
 * 
 * @author Claude
 * @since 2024-08-29
 */
@Slf4j
@RestController
@RequestMapping("/api/chat/messages")
@RequiredArgsConstructor
public class MessageReactionController {
    
    private final AddReactionCmdExe addReactionCmdExe;
    private final GetMessageReactionsQryExe getMessageReactionsQryExe;
    
    /**
     * 添加表情反应
     */
    @PostMapping("/reactions")
    public Response addReaction(@RequestBody AddReactionRequest request) {
        log.info("收到添加表情反应请求: msgId={}, emoji={}, action={}", 
                request.getMsgId(), request.getEmoji(), request.getAction());
        
        try {
            AddReactionCmd cmd = AddReactionCmd.builder()
                    .msgId(request.getMsgId())
                    .sessionId(request.getSessionId().toString())
                    .emoji(request.getEmoji())
                    .name(request.getName())
                    .action(request.getAction())
                    .build();
            
            return addReactionCmdExe.execute(cmd);
            
        } catch (Exception e) {
            log.error("添加表情反应失败", e);
            return Response.buildFailure("ADD_REACTION_ERROR", "添加表情反应失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取消息的所有表情反应
     */
    @GetMapping("/{msgId}/reactions")
    public SingleResponse<List<ReactionDTO>> getMessageReactions(@PathVariable String msgId) {
        log.info("获取消息表情反应: msgId={}", msgId);
        
        try {
            return getMessageReactionsQryExe.execute(msgId);
        } catch (Exception e) {
            log.error("获取消息表情反应失败: msgId={}", msgId, e);
            return SingleResponse.buildFailure("GET_REACTIONS_ERROR", "获取表情反应失败: " + e.getMessage());
        }
    }
    
    /**
     * 移除表情反应
     */
    @DeleteMapping("/reactions")
    public Response removeReaction(@RequestBody RemoveReactionRequest request) {
        log.info("收到移除表情反应请求: msgId={}, emoji={}", request.getMsgId(), request.getEmoji());
        
        try {
            AddReactionCmd cmd = AddReactionCmd.builder()
                    .msgId(request.getMsgId())
                    .emoji(request.getEmoji())
                    .action("remove")
                    .build();
            
            return addReactionCmdExe.execute(cmd);
            
        } catch (Exception e) {
            log.error("移除表情反应失败", e);
            return Response.buildFailure("REMOVE_REACTION_ERROR", "移除表情反应失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量获取消息表情反应
     */
    @PostMapping("/reactions/batch")
    public SingleResponse<List<MessageReactionData>> getBatchReactions(@RequestBody BatchReactionsRequest request) {
        log.info("批量获取消息表情反应: msgIds count={}", request.getMsgIds().size());
        
        try {
            // TODO: 实现批量查询逻辑
            // 这里可以优化为批量查询，避免N+1问题
            return SingleResponse.buildFailure("NOT_IMPLEMENTED", "批量查询功能开发中");
            
        } catch (Exception e) {
            log.error("批量获取消息表情反应失败", e);
            return SingleResponse.buildFailure("GET_BATCH_REACTIONS_ERROR", "批量获取失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取表情反应统计
     */
    @GetMapping("/{msgId}/reactions/stats")
    public SingleResponse<ReactionStatsDTO> getReactionStats(@PathVariable String msgId) {
        log.info("获取表情反应统计: msgId={}", msgId);
        
        try {
            // TODO: 实现统计逻辑
            return SingleResponse.buildFailure("NOT_IMPLEMENTED", "统计功能开发中");
            
        } catch (Exception e) {
            log.error("获取表情反应统计失败: msgId={}", msgId, e);
            return SingleResponse.buildFailure("GET_STATS_ERROR", "获取统计失败: " + e.getMessage());
        }
    }
    
    // ==================== 内部类定义 ====================
    
    /**
     * 添加表情反应请求
     */
    public static class AddReactionRequest {
        private String msgId;
        private Long sessionId;
        private String emoji;
        private String name;
        private String action = "toggle";
        
        // Getters and Setters
        public String getMsgId() { return msgId; }
        public void setMsgId(String msgId) { this.msgId = msgId; }
        
        public Long getSessionId() { return sessionId; }
        public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
        
        public String getEmoji() { return emoji; }
        public void setEmoji(String emoji) { this.emoji = emoji; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
    }
    
    /**
     * 移除表情反应请求
     */
    public static class RemoveReactionRequest {
        private String msgId;
        private String emoji;
        
        // Getters and Setters
        public String getMsgId() { return msgId; }
        public void setMsgId(String msgId) { this.msgId = msgId; }
        
        public String getEmoji() { return emoji; }
        public void setEmoji(String emoji) { this.emoji = emoji; }
    }
    
    /**
     * 批量查询表情反应请求
     */
    public static class BatchReactionsRequest {
        private List<String> msgIds;
        
        // Getters and Setters
        public List<String> getMsgIds() { return msgIds; }
        public void setMsgIds(List<String> msgIds) { this.msgIds = msgIds; }
    }
    
    /**
     * 消息表情反应数据
     */
    public static class MessageReactionData {
        private String msgId;
        private List<ReactionDTO> reactions;
        
        // Getters and Setters
        public String getMsgId() { return msgId; }
        public void setMsgId(String msgId) { this.msgId = msgId; }
        
        public List<ReactionDTO> getReactions() { return reactions; }
        public void setReactions(List<ReactionDTO> reactions) { this.reactions = reactions; }
    }
    
    /**
     * 表情反应统计数据
     */
    public static class ReactionStatsDTO {
        private int totalReactions;
        private int uniqueUsers;
        private List<TopReactionDTO> topReactions;
        
        // Getters and Setters
        public int getTotalReactions() { return totalReactions; }
        public void setTotalReactions(int totalReactions) { this.totalReactions = totalReactions; }
        
        public int getUniqueUsers() { return uniqueUsers; }
        public void setUniqueUsers(int uniqueUsers) { this.uniqueUsers = uniqueUsers; }
        
        public List<TopReactionDTO> getTopReactions() { return topReactions; }
        public void setTopReactions(List<TopReactionDTO> topReactions) { this.topReactions = topReactions; }
    }
    
    /**
     * 热门表情反应数据
     */
    public static class TopReactionDTO {
        private String emoji;
        private String name;
        private int count;
        
        // Getters and Setters
        public String getEmoji() { return emoji; }
        public void setEmoji(String emoji) { this.emoji = emoji; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
    }
}