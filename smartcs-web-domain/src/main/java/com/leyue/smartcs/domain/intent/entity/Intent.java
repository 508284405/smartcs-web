package com.leyue.smartcs.domain.intent.entity;

import com.leyue.smartcs.domain.intent.enums.IntentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 意图实体
 * 
 * @author Claude
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Intent {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 目录ID
     */
    private Long catalogId;
    
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
     * 状态
     */
    private IntentStatus status;
    
    /**
     * 创建者ID
     */
    private Long creatorId;
    
    /**
     * 是否删除
     */
    private Boolean isDeleted;
    
    /**
     * 创建时间
     */
    private Long createdAt;
    
    /**
     * 更新时间
     */
    private Long updatedAt;
}