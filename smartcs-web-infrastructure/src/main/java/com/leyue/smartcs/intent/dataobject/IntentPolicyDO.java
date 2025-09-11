package com.leyue.smartcs.intent.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 意图策略数据对象，对应t_intent_policy表
 * 
 * @author Claude
 */
@Data
@TableName("t_intent_policy")
public class IntentPolicyDO {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 版本ID
     */
    @TableField("version_id")
    private Long versionId;
    
    /**
     * 阈值 tau
     */
    @TableField("threshold_tau")
    private BigDecimal thresholdTau;
    
    /**
     * 边际 delta
     */
    @TableField("margin_delta")
    private BigDecimal marginDelta;
    
    /**
     * 温度 T
     */
    @TableField("temp_t")
    private BigDecimal tempT;
    
    /**
     * 未知标签
     */
    @TableField("unknown_label")
    private String unknownLabel;
    
    /**
     * 渠道覆盖配置
     */
    @TableField(value = "channel_overrides", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> channelOverrides;
    
    /**
     * 创建者
     */
    @TableField("created_by")
    private String createdBy;
    
    /**
     * 更新者
     */
    @TableField("updated_by")
    private String updatedBy;
    
    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Long createdAt;
    
    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Long updatedAt;
}