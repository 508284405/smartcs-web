package com.leyue.smartcs.domain.intent.entity;

import com.leyue.smartcs.domain.intent.enums.SnapshotStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 意图快照实体
 * 
 * @author Claude
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntentSnapshot {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 快照名称
     */
    private String name;
    
    /**
     * 快照编码
     */
    private String code;
    
    /**
     * 作用域
     */
    private String scope;
    
    /**
     * 作用域选择器
     */
    private Map<String, Object> scopeSelector;
    
    /**
     * 状态
     */
    private SnapshotStatus status;
    
    /**
     * ETag
     */
    private String etag;
    
    /**
     * 快照项目列表
     */
    private List<IntentSnapshotItem> items;
    
    /**
     * 创建者ID
     */
    private Long createdBy;
    
    /**
     * 发布者ID
     */
    private Long publishedBy;
    
    /**
     * 发布时间
     */
    private Long publishedAt;
    
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