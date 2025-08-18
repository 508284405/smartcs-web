package com.leyue.smartcs.intent.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.leyue.smartcs.common.dao.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * 意图快照数据对象，对应t_intent_snapshot表
 * 
 * @author Claude
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_intent_snapshot")
public class IntentSnapshotDO extends BaseDO {
    
    /**
     * 快照名称
     */
    @TableField("name")
    private String name;
    
    /**
     * 快照编码
     */
    @TableField("code")
    private String code;
    
    /**
     * 作用域
     */
    @TableField("scope")
    private String scope;
    
    /**
     * 作用域选择器
     */
    @TableField(value = "scope_selector", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> scopeSelector;
    
    /**
     * 状态
     */
    @TableField("status")
    private String status;
    
    /**
     * ETag
     */
    @TableField("etag")
    private String etag;
    
    /**
     * 创建者ID
     */
    @TableField("created_by_id")
    private Long createdById;
    
    /**
     * 发布者ID
     */
    @TableField("published_by_id")
    private Long publishedById;
    
    /**
     * 发布时间
     */
    @TableField("published_at")
    private Long publishedAt;
}