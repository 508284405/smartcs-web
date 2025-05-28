package com.leyue.smartcs.domain.knowledge.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 内容上传事件
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentUploadedEvent {
    
    /**
     * 内容ID
     */
    private Long contentId;
    
    /**
     * 知识库ID
     */
    private Long knowledgeBaseId;
    
    /**
     * 内容标题
     */
    private String title;
    
    /**
     * 内容类型
     */
    private String contentType;
    
    /**
     * 上传者ID
     */
    private Long uploaderId;
    
    /**
     * 事件发生时间
     */
    private Long eventTime;
} 