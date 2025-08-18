package com.leyue.smartcs.domain.intent.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 意图策略实体
 * 
 * @author Claude
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntentPolicy {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 版本ID
     */
    private Long versionId;
    
    /**
     * 阈值 tau
     */
    private BigDecimal thresholdTau;
    
    /**
     * 边际 delta
     */
    private BigDecimal marginDelta;
    
    /**
     * 温度 T
     */
    private BigDecimal tempT;
    
    /**
     * 未知标签
     */
    private String unknownLabel;
    
    /**
     * 渠道覆盖配置
     */
    private Map<String, Object> channelOverrides;
    
    /**
     * 创建时间
     */
    private Long createdAt;
    
    /**
     * 更新时间
     */
    private Long updatedAt;
}