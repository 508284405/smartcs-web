package com.leyue.smartcs.intent.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.leyue.smartcs.common.dao.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 意图目录数据对象，对应t_intent_catalog表
 * 
 * @author Claude
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_intent_catalog")
public class IntentCatalogDO extends BaseDO {
    
    /**
     * 目录名称
     */
    @TableField("name")
    private String name;
    
    /**
     * 目录编码
     */
    @TableField("code")
    private String code;
    
    /**
     * 描述
     */
    @TableField("description")
    private String description;
    
    /**
     * 父目录ID
     */
    @TableField("parent_id")
    private Long parentId;
    
    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder;
    
    /**
     * 创建者ID
     */
    @TableField("creator_id")
    private Long creatorId;
}