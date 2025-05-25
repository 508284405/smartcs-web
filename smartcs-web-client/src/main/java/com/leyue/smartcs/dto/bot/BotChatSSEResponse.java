package com.leyue.smartcs.dto.bot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Bot SSE聊天响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BotChatSSEResponse {
    
    /**
     * 会话ID
     */
    private Long sessionId;
    
    /**
     * 机器人回答（可能是部分内容）
     */
    private String answer;
    
    /**
     * 是否完成
     */
    private Boolean finished = false;
    
    /**
     * 引用的知识来源列表
     */
    private List<KnowledgeSource> sources;
    
    /**
     * 使用的模型ID
     */
    private String modelId;
    
    /**
     * 处理时间（毫秒）
     */
    private Long processTime;
    
    /**
     * 错误信息（如果有）
     */
    private String error;
    
    /**
     * 知识来源
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KnowledgeSource {
        /**
         * 来源类型（FAQ/DOC）
         */
        private String type;
        
        /**
         * 内容ID
         */
        private Long contentId;
        
        /**
         * 来源标题
         */
        private String title;
        
        /**
         * 内容片段
         */
        private String snippet;
        
        /**
         * 相关度分数
         */
        private Float score;
    }
} 