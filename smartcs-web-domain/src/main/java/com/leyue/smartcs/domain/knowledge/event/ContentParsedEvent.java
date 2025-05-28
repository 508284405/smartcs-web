package com.leyue.smartcs.domain.knowledge.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 内容解析完成事件
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentParsedEvent {
    
    /**
     * 内容ID
     */
    private Long contentId;
    
    /**
     * 知识库ID
     */
    private Long knowledgeBaseId;
    
    /**
     * 提取的文本长度
     */
    private Integer textLength;
    
    /**
     * 处理者ID
     */
    private Long processerId;
    
    /**
     * 事件发生时间
     */
    private Long eventTime;
} 