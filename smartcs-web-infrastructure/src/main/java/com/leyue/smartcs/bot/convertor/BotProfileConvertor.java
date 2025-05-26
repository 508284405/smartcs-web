package com.leyue.smartcs.bot.convertor;

import com.leyue.smartcs.bot.dataobject.BotProfileDO;
import com.leyue.smartcs.domain.bot.BotProfile;
import com.leyue.smartcs.dto.bot.BotProfileCreateCmd;
import com.leyue.smartcs.dto.bot.BotProfileDTO;
import org.mapstruct.Mapper;

/**
 * 机器人配置转换器
 */
@Mapper(componentModel = "spring")
public interface BotProfileConvertor {
    /**
     * DO转领域对象
     * @param botProfileDO 数据对象
     * @return 领域对象
     */
    BotProfile toDomain(BotProfileDO botProfileDO);

    /**
     * 领域对象转DO
     * @param botProfile 领域对象
     * @return 数据对象
     */
    BotProfileDO toDO(BotProfile botProfile);

    /**
     * Cmd转领域对象
     * @param cmd 命令对象
     * @return 领域对象
     */
    BotProfile toDomain(BotProfileCreateCmd cmd);

    /**
     * 领域对象转DTO
     * @param botProfile 领域对象
     * @return DTO对象
     */
    BotProfileDTO toDTO(BotProfile botProfile);
} 