package com.leyue.smartcs.bot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.bot.dataobject.BotProfileDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Bot Profile Mapper接口
 */
@Mapper
public interface BotProfileMapper extends BaseMapper<BotProfileDO> {
    
    /**
     * 根据prompt模板key查询使用该模板的机器人数量
     * @param promptKey 模板标识
     * @return 使用该模板的机器人数量
     */
    int countByPromptKey(@Param("promptKey") String promptKey);
} 