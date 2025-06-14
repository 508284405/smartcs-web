package com.leyue.smartcs.dto.bot;

import lombok.Data;

/**
 * Bot Prompt模板数据传输对象
 */
@Data
public class BotPromptTemplateDTO {
    
    /**
     * 模板ID
     */
    private Long id;
    
    /**
     * 模板标识
     */
    private String templateKey;
    
    /**
     * 模板内容
     */
    private String templateContent;
    
    /**
     * 是否删除
     */
    private Integer isDeleted;
    
    /**
     * 创建人
     */
    private String createdBy;
    
    /**
     * 更新人
     */
    private String updatedBy;
    
    /**
     * 创建时间
     */
    private Long createdAt;
    
    /**
     * 更新时间
     */
    private Long updatedAt;
} 