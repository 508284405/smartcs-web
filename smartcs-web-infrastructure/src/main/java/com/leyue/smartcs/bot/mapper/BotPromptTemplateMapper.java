package com.leyue.smartcs.bot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.bot.dataobject.BotPromptTemplateDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Bot Prompt模板Mapper接口
 */
@Mapper
public interface BotPromptTemplateMapper extends BaseMapper<BotPromptTemplateDO> {
    
    /**
     * 根据模板标识查询模板
     * @param templateKey 模板标识
     * @return 模板对象
     */
    BotPromptTemplateDO selectByTemplateKey(@Param("templateKey") String templateKey);
} 