package com.leyue.smartcs.domain.intent.entity;

import com.leyue.smartcs.domain.intent.valueobject.SlotTemplate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 意图快照项实体
 * 
 * @author Claude
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntentSnapshotItem {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 快照ID
     */
    private Long snapshotId;
    
    /**
     * 意图ID
     */
    private Long intentId;
    
    /**
     * 意图编码
     */
    private String intentCode;
    
    /**
     * 意图名称
     */
    private String intentName;
    
    /**
     * 版本ID
     */
    private Long versionId;
    
    /**
     * 版本号
     */
    private String version;
    
    /**
     * 标签列表
     */
    private java.util.List<String> labels;
    
    /**
     * 边界列表
     */
    private java.util.List<String> boundaries;
    
    /**
     * 槽位模板（JSON格式存储）
     */
    private SlotTemplate slotTemplate;
    
    /**
     * 意图字典关键词列表
     */
    private java.util.List<String> keywords;
    
    /**
     * 意图匹配模式列表
     */
    private java.util.List<String> patterns;
    
    /**
     * 创建时间
     */
    private Long createdAt;
}