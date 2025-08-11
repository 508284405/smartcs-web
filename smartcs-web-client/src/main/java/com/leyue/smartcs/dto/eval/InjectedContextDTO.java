package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 注入的上下文信息DTO
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InjectedContextDTO {
    
    /**
     * 上下文ID
     */
    private String contextId;
    
    /**
     * 上下文类型：KNOWLEDGE_BASE, DOCUMENT, DATABASE, API
     */
    private String contextType;
    
    /**
     * 上下文内容
     */
    private String content;
    
    /**
     * 上下文来源
     */
    private String source;
    
    /**
     * 相关性分数
     */
    private Double relevanceScore;
    
    /**
     * 置信度
     */
    private Double confidence;
    
    /**
     * 元数据
     */
    private Map<String, Object> metadata;
    
    /**
     * 是否被使用
     */
    private Boolean isUsed;
    
    /**
     * 使用位置
     */
    private String usageLocation;
    
    /**
     * 注入时间
     */
    private Long injectionTime;
}
