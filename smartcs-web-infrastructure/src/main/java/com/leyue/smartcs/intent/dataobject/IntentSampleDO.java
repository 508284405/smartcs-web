package com.leyue.smartcs.intent.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.leyue.smartcs.common.dao.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * 意图样本数据对象，对应t_intent_sample表
 * 
 * @author Claude
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_intent_sample")
public class IntentSampleDO extends BaseDO {
    
    /**
     * 版本ID
     */
    @TableField("version_id")
    private Long versionId;
    
    /**
     * 样本类型
     */
    @TableField("type")
    private String type;
    
    /**
     * 文本内容
     */
    @TableField("text")
    private String text;
    
    /**
     * 插槽信息
     */
    @TableField(value = "slots", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> slots;
    
    /**
     * 数据来源
     */
    @TableField("source")
    private String source;
    
    /**
     * 置信度分数
     */
    @TableField("confidence_score")
    private Double confidenceScore;
    
    /**
     * 标注者ID
     */
    @TableField("annotator_id")
    private Long annotatorId;
}