package com.leyue.smartcs.dto.app;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI应用聊天响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAppChatResponse {
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 消息ID
     */
    private String messageId;
    
    /**
     * 响应内容
     */
    private String content;
    
    /**
     * 是否完成
     */
    private Boolean finished;
    
    /**
     * 时间戳
     */
    private Long timestamp;
    
    /**
     * 处理时长（毫秒）
     */
    private Long processTime;
    
    /**
     * 知识来源（启用RAG时返回）
     */
    private List<KnowledgeSource> sources;
    
    /**
     * 错误信息
     */
    private String error;
    
    /**
     * 知识来源信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KnowledgeSource {
        /**
         * 来源类型
         */
        private String type;
        
        /**
         * 来源标题
         */
        private String title;
        
        /**
         * 来源内容
         */
        private String content;
        
        /**
         * 相似度分数
         */
        private Double score;
        
        /**
         * 来源URL或路径
         */
        private String url;
    }
}