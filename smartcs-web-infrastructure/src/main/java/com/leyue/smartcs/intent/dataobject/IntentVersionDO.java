package com.leyue.smartcs.intent.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.leyue.smartcs.common.dao.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 意图版本数据对象，对应t_intent_version表
 * 
 * @author Claude
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_intent_version")
public class IntentVersionDO extends BaseDO {
    
    /**
     * 意图ID
     */
    @TableField("intent_id")
    private Long intentId;
    
    /**
     * 版本号
     */
    @TableField("version_number")
    private String versionNumber;
    
    /**
     * 版本名称
     */
    @TableField("version_name")
    private String versionName;
    
    /**
     * 配置快照
     */
    @TableField(value = "config_snapshot", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> configSnapshot;
    
    /**
     * 状态
     */
    @TableField("status")
    private String status;
    
    /**
     * 样本数量
     */
    @TableField("sample_count")
    private Integer sampleCount;
    
    /**
     * 准确率分数
     */
    @TableField("accuracy_score")
    private BigDecimal accuracyScore;
    
    /**
     * 变更说明
     */
    @TableField("change_note")
    private String changeNote;
    
    /**
     * 创建者ID
     */
    @TableField("created_by_id")
    private Long createdById;
    
    /**
     * 审批者ID
     */
    @TableField("approved_by_id")
    private Long approvedById;
    
    /**
     * 审批时间
     */
    @TableField("approved_at")
    private Long approvedAt;
}