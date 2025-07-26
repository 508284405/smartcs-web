package com.leyue.smartcs.model.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.leyue.smartcs.common.dao.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 模型Prompt模板数据对象，对应t_model_prompt_template表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_model_prompt_template")
public class ModelPromptTemplateDO extends BaseDO {

    /**
     * 模板键（唯一标识）
     */
    private String templateKey;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 模板内容
     */
    private String templateContent;

    /**
     * 模板描述
     */
    private String description;

    /**
     * 支持的模型类型（逗号分隔）
     */
    private String modelTypes;

    /**
     * 模板变量（JSON格式）
     */
    private String variables;

    /**
     * 是否为系统内置模板
     */
    private Boolean isSystem;

    /**
     * 状态（ACTIVE/INACTIVE）
     */
    private String status;
}