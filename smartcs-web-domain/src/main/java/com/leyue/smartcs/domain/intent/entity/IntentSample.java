package com.leyue.smartcs.domain.intent.entity;

import com.leyue.smartcs.domain.intent.enums.SampleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 意图样本实体
 * 
 * @author Claude
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntentSample {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 版本ID
     */
    private Long versionId;
    
    /**
     * 样本类型
     */
    private SampleType type;
    
    /**
     * 文本内容
     */
    private String text;
    
    /**
     * 插槽信息
     */
    private Map<String, Object> slots;
    
    /**
     * 数据来源
     */
    private String source;
    
    /**
     * 置信度分数
     */
    private Double confidenceScore;
    
    /**
     * 标注者ID
     */
    private Long annotatorId;
    
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