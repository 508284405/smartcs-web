package com.leyue.smartcs.knowledge.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.leyue.smartcs.common.dao.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户知识库权限关系数据对象，对应t_kb_user_kb_rel表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_kb_user_kb_rel")
public class UserKnowledgeBaseRelDO extends BaseDO {
    
    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;
    
    /**
     * 知识库ID
     */
    @TableField("knowledge_base_id")
    private Long knowledgeBaseId;
    
    /**
     * 角色 reader/writer/admin
     */
    @TableField("role")
    private String role;
} 