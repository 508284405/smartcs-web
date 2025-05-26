package com.leyue.smartcs.bot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.bot.dataobject.BotProfileDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 机器人配置Mapper接口
 */
@Mapper
public interface BotProfileMapper extends BaseMapper<BotProfileDO> {
} 