package com.leyue.smartcs.dto.intent;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 意图DTO
 * 
 * @author Claude
 */
@Data
public class IntentDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 目录ID
     */
    private Long catalogId;
    
    /**
     * 目录名称
     */
    private String catalogName;
    
    /**
     * 意图名称
     */
    private String name;
    
    /**
     * 意图编码
     */
    private String code;
    
    /**
     * 意图描述
     */
    private String description;
    
    /**
     * 标签数组
     */
    private List<String> labels;
    
    /**
     * 边界定义
     */
    private Map<String, Object> boundaries;
    
    /**
     * 当前活跃版本ID
     */
    private Long currentVersionId;
    
    /**
     * 当前版本号
     */
    private String currentVersionNumber;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 创建者ID
     */
    private Long creatorId;
    
    /**
     * 创建时间
     */
    private Long createdAt;
    
    /**
     * 更新时间
     */
    private Long updatedAt;
}