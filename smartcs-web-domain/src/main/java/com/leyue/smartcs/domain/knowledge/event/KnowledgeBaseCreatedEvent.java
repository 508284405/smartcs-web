package com.leyue.smartcs.domain.knowledge.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 知识库创建事件
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeBaseCreatedEvent {
    
    /**
     * 知识库ID
     */
    private Long knowledgeBaseId;
    
    /**
     * 知识库名称
     */
    private String knowledgeBaseName;
    
    /**
     * 创建者ID
     */
    private Long ownerId;
    
    /**
     * 事件发生时间
     */
    private Long eventTime;
} 