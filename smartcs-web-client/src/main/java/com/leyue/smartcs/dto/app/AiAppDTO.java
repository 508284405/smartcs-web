package com.leyue.smartcs.dto.app;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * AI应用DTO
 */
@Data
public class AiAppDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 应用名称
     */
    private String name;
    
    /**
     * 应用唯一编码
     */
    private String code;
    
    /**
     * 应用描述
     */
    private String description;
    
    /**
     * 应用类型
     */
    private String type;
    
    /**
     * 应用类型名称
     */
    private String typeName;
    
    /**
     * 应用类型描述
     */
    private String typeDescription;
    
    /**
     * 应用配置信息
     */
    private Map<String, Object> config;
    
    /**
     * 应用状态
     */
    private String status;
    
    /**
     * 应用状态名称
     */
    private String statusName;
    
    /**
     * 应用图标
     */
    private String icon;
    
    /**
     * 应用标签
     */
    private List<String> tags;
    
    /**
     * 创建者ID
     */
    private Long creatorId;
    
    /**
     * 创建者
     */
    private String createdBy;
    
    /**
     * 更新者
     */
    private String updatedBy;
    
    /**
     * 创建时间（毫秒时间戳）
     */
    private Long createdAt;
    
    /**
     * 更新时间（毫秒时间戳）
     */
    private Long updatedAt;
    
    /**
     * 是否可用
     */
    private Boolean usable;
    
    /**
     * 是否可编辑
     */
    private Boolean editable;
}