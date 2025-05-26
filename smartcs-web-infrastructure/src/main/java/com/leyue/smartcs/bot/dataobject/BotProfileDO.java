package com.leyue.smartcs.bot.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.leyue.smartcs.common.dao.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

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
     * 该 Bot 对外允许的最大 QPS
     */
    private Integer maxQps;

    /**
     * LLM 采样温度
     */
    private BigDecimal temperature;

    /**
     * 额外配置（如系统指令、插件开关等）
     */
    private String extraConfig;
} 