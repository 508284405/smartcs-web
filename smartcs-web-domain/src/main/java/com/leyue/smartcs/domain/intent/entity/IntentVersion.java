package com.leyue.smartcs.domain.intent.entity;

import com.leyue.smartcs.domain.intent.enums.VersionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 意图版本实体
 * 
 * @author Claude
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntentVersion {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 意图ID
     */
    private Long intentId;
    
    /**
     * 版本号
     */
    private String versionNumber;
    
    /**
     * 版本标识
     */
    private String version;
    
    /**
     * 版本名称
     */
    private String versionName;
    
    /**
     * 配置快照
     */
    private Map<String, Object> configSnapshot;
    
    /**
     * 状态
     */
    private VersionStatus status;
    
    /**
     * 样本数量
     */
    private Integer sampleCount;
    
    /**
     * 准确率分数
     */
    private BigDecimal accuracyScore;
    
    /**
     * 变更说明
     */
    private String changeNote;
    
    /**
     * 创建者ID
     */
    private Long createdBy;
    
    /**
     * 审批者ID
     */
    private Long approvedBy;
    
    /**
     * 审批时间
     */
    private Long approvedAt;
    
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