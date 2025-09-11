package com.leyue.smartcs.moderation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.moderation.dataobject.ModerationConfigDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 内容审核配置Mapper
 */
@Mapper
public interface ModerationConfigMapper extends BaseMapper<ModerationConfigDO> {

    /**
     * 根据配置键查找配置
     */
    @Select("SELECT * FROM t_moderation_config WHERE config_key = #{configKey} AND is_active = 1")
    ModerationConfigDO findByConfigKey(@Param("configKey") String configKey);

    /**
     * 根据分类查找配置
     */
    @Select("SELECT * FROM t_moderation_config WHERE category = #{category} AND is_active = 1 ORDER BY config_key ASC")
    List<ModerationConfigDO> findByCategory(@Param("category") String category);

    /**
     * 查找所有启用的配置
     */
    @Select("SELECT * FROM t_moderation_config WHERE is_active = 1 ORDER BY category ASC, config_key ASC")
    List<ModerationConfigDO> findAllActiveConfigs();

    /**
     * 查找系统配置
     */
    @Select("SELECT * FROM t_moderation_config WHERE is_system = 1 AND is_active = 1 ORDER BY category ASC, config_key ASC")
    List<ModerationConfigDO> findSystemConfigs();

    /**
     * 查找用户可配置的配置项
     */
    @Select("SELECT * FROM t_moderation_config WHERE is_system = 0 AND is_active = 1 ORDER BY category ASC, config_key ASC")
    List<ModerationConfigDO> findUserConfigs();

    /**
     * 检查配置键是否存在
     */
    @Select("SELECT COUNT(*) FROM t_moderation_config WHERE config_key = #{configKey} AND id != #{excludeId}")
    int countByConfigKeyExcludeId(@Param("configKey") String configKey, @Param("excludeId") Long excludeId);
}