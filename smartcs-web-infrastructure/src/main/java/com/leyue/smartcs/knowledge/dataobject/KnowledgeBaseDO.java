package com.leyue.smartcs.knowledge.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.leyue.smartcs.common.dao.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 知识库数据对象，对应t_kb_knowledge_base表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_kb_knowledge_base")
public class KnowledgeBaseDO extends BaseDO {
    
    /**
     * 知识库名称
     */
    @TableField("name")
    private String name;
    
    /**
     * 知识库唯一编码
     */
    @TableField("code")
    private String code;
    
    /**
     * 描述信息
     */
    @TableField("description")
    private String description;
    
    /**
     * 创建者ID
     */
    @TableField("owner_id")
    private Long ownerId;
    
    /**
     * 可见性 public/private
     */
    @TableField("visibility")
    private String visibility;
} 