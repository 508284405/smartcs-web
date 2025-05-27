package com.leyue.smartcs.bot.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.leyue.smartcs.common.dao.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;



/**
 * 机器人配置数据对象，对应cs_bot_profile表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_cs_bot_profile")
public class BotProfileDO extends BaseDO {

    /**
     * 机器人名称
     */
    private String botName;

    /**
     * 使用的 LLM / 模型标识，如 gpt-4o、bge-large
     */
    private String modelName;

    /**
     * 默认 Prompt 模板 key，关联 bot_prompt_template
     */
    private String promptKey;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 模型厂商，如openai、deepseek等
     */
    private String vendor;

    /**
     * 模型类型，如chat、embedding、image、audio等
     */
    private String modelType;

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * API基础URL
     */
    private String baseUrl;

    /**
     * 模型具体配置（JSON格式），如具体模型4o-mini等
     */
    private String options;

    /**
     * 是否启用
     */
    private Boolean enabled;
} 