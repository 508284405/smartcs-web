package com.leyue.smartcs.bot.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.leyue.smartcs.common.dao.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Bot Prompt模板数据对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_bot_prompt_template")
public class BotPromptTemplateDO extends BaseDO {
    
    /**
     * 模板标识
     */
    private String templateKey;
    
    /**
     * 模板内容
     */
    private String templateContent;
} 