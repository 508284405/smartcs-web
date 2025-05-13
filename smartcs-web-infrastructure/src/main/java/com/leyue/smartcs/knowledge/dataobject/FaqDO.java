package com.leyue.smartcs.knowledge.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.leyue.smartcs.common.dao.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * FAQ数据对象，对应cs_faq表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_cs_faq")
public class FaqDO extends BaseDO {
    
    /**
     * 问题文本
     */
    @TableField("question")
    private String question;
    
    /**
     * 答案文本
     */
    @TableField("answer_text")
    private String answer;
    
    /**
     * 命中次数
     */
    @TableField("hit_count")
    private Long hitCount;
    
    /**
     * 版本号
     */
    @TableField("version_no")
    private Integer version;
    
    /**
     * 是否启用
     */
    @TableField("enabled")
    private Boolean enabled;
} 