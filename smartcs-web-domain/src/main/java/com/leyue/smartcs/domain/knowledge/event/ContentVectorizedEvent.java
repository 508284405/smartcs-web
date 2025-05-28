package com.leyue.smartcs.domain.knowledge.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 内容向量化完成事件
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentVectorizedEvent {
    
    /**
     * 内容ID
     */
    private Long contentId;
    
    /**
     * 知识库ID
     */
    private Long knowledgeBaseId;
    
    /**
     * 生成的切片数量
     */
    private Integer chunkCount;
    
    /**
     * 向量维度
     */
    private Integer vectorDim;
    
    /**
     * 向量化提供方
     */
    private String provider;
    
    /**
     * 处理者ID
     */
    private Long processerId;
    
    /**
     * 事件发生时间
     */
    private Long eventTime;
} 