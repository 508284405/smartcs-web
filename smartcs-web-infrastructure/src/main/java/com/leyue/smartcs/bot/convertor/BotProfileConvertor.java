package com.leyue.smartcs.bot.convertor;

import com.leyue.smartcs.bot.dataobject.BotProfileDO;
import com.leyue.smartcs.domain.bot.BotProfile;
import com.leyue.smartcs.domain.bot.enums.ModelTypeEnum;
import com.leyue.smartcs.domain.bot.enums.VendorTypeEnum;
import com.leyue.smartcs.dto.bot.BotProfileCreateCmd;
import com.leyue.smartcs.dto.bot.BotProfileDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

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
    @Mapping(target = "vendor", expression = "java(vendorFromCode(botProfileDO.getVendor()))")
    @Mapping(target = "modelType", expression = "java(modelTypeFromCode(botProfileDO.getModelType()))")
    @Mapping(target = "botId", source = "id")
    BotProfile toDomain(BotProfileDO botProfileDO);

    /**
     * 领域对象转DO
     * @param botProfile 领域对象
     * @return 数据对象
     */
    @Mapping(target = "vendor", expression = "java(vendorToCode(botProfile.getVendor()))")
    @Mapping(target = "modelType", expression = "java(modelTypeToCode(botProfile.getModelType()))")
    @Mapping(target = "id", source = "botId")
    BotProfileDO toDO(BotProfile botProfile);

    /**
     * Cmd转领域对象
     * @param cmd 命令对象
     * @return 领域对象
     */
    @Mapping(target = "vendor", expression = "java(vendorFromCode(cmd.getVendor()))")
    @Mapping(target = "modelType", expression = "java(modelTypeFromCode(cmd.getModelType()))")
    BotProfile toDomain(BotProfileCreateCmd cmd);

    /**
     * 领域对象转DTO
     * @param botProfile 领域对象
     * @return DTO对象
     */
    @Mapping(target = "vendor", expression = "java(vendorToCode(botProfile.getVendor()))")
    @Mapping(target = "modelType", expression = "java(modelTypeToCode(botProfile.getModelType()))")
    BotProfileDTO toDTO(BotProfile botProfile);
    
    /**
     * 厂商代码转枚举
     */
    default VendorTypeEnum vendorFromCode(String code) {
        return code != null ? VendorTypeEnum.fromCode(code) : null;
    }
    
    /**
     * 厂商枚举转代码
     */
    default String vendorToCode(VendorTypeEnum vendor) {
        return vendor != null ? vendor.getCode() : null;
    }
    
    /**
     * 模型类型代码转枚举
     */
    default ModelTypeEnum modelTypeFromCode(String code) {
        return code != null ? ModelTypeEnum.fromCode(code) : null;
    }
    
    /**
     * 模型类型枚举转代码
     */
    default String modelTypeToCode(ModelTypeEnum modelType) {
        return modelType != null ? modelType.getCode() : null;
    }
} 