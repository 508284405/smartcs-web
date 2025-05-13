package com.leyue.smartcs.knowledge.dto;

import lombok.Data;

/**
 * FAQ数据传输对象
 */
@Data
public class FaqDTO {
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 问题文本
     */
    private String question;
    
    /**
     * 答案文本
     */
    private String answer;
    
    /**
     * 命中次数
     */
    private Long hitCount;
    
    /**
     * 版本号
     */
    private Integer version;
    
    /**
     * 创建时间（毫秒时间戳）
     */
    private Long createdAt;
    
    /**
     * 更新时间（毫秒时间戳）
     */
    private Long updatedAt;
} 