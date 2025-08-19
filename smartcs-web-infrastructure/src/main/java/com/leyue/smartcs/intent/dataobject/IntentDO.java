package com.leyue.smartcs.intent.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.leyue.smartcs.common.dao.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * 意图数据对象，对应t_intent表
 * 
 * @author Claude
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_intent")
public class IntentDO extends BaseDO {
    
    /**
     * 目录ID
     */
    @TableField("catalog_id")
    private Long catalogId;
    
    /**
     * 意图名称
     */
    @TableField("name")
    private String name;
    
    /**
     * 意图编码
     */
    @TableField("code")
    private String code;
    
    /**
     * 意图描述
     */
    @TableField("description")
    private String description;
    
    /**
     * 标签数组
     */
    @TableField(value = "labels", typeHandler = JacksonTypeHandler.class)
    private List<String> labels;
    
    /**
     * 边界定义
     */
    @TableField(value = "boundaries", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> boundaries;
    
    /**
     * 当前活跃版本ID
     */
    @TableField("current_version_id")
    private Long currentVersionId;
    
    /**
     * 状态
     */
    @TableField("status")
    private String status;
    
    /**
     * 创建者ID
     */
    @TableField("creator_id")
    private Long creatorId;
}