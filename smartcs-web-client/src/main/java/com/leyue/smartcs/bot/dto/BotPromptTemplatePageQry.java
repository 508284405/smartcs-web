package com.leyue.smartcs.bot.dto;

import com.alibaba.cola.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Bot Prompt模板分页查询
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BotPromptTemplatePageQry extends PageQuery {
    
    /**
     * 模板标识（模糊搜索）
     */
    private String templateKey;
} 