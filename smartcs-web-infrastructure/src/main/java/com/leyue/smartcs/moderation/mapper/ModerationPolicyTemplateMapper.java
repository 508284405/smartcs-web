package com.leyue.smartcs.moderation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.moderation.dataobject.ModerationPolicyTemplateDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * 审核策略模板Mapper接口
 */
@Mapper
public interface ModerationPolicyTemplateMapper extends BaseMapper<ModerationPolicyTemplateDO> {

    /**
     * 根据编码查询模板
     */
    @Select("SELECT * FROM t_moderation_policy_template WHERE code = #{code} AND is_active = 1 ORDER BY version DESC LIMIT 1")
    ModerationPolicyTemplateDO findByCode(@Param("code") String code);

    /**
     * 根据编码和版本查询模板
     */
    @Select("SELECT * FROM t_moderation_policy_template WHERE code = #{code} AND version = #{version} AND is_active = 1")
    ModerationPolicyTemplateDO findByCodeAndVersion(@Param("code") String code, @Param("version") String version);

    /**
     * 根据模板类型查询模板列表
     */
    @Select("SELECT * FROM t_moderation_policy_template WHERE template_type = #{templateType} AND is_active = 1 ORDER BY version DESC, created_at DESC")
    List<ModerationPolicyTemplateDO> findByTemplateType(@Param("templateType") String templateType);

    /**
     * 根据语言查询模板列表
     */
    @Select("SELECT * FROM t_moderation_policy_template WHERE language = #{language} AND is_active = 1 ORDER BY template_type ASC, version DESC")
    List<ModerationPolicyTemplateDO> findByLanguage(@Param("language") String language);

    /**
     * 查询所有可用模板（按类型和版本排序）
     */
    @Select("SELECT * FROM t_moderation_policy_template WHERE is_active = 1 ORDER BY template_type ASC, code ASC, version DESC")
    List<ModerationPolicyTemplateDO> findAllActiveOrderByTypeAndVersion();

    /**
     * 根据编码查询所有版本的模板
     */
    @Select("SELECT * FROM t_moderation_policy_template WHERE code = #{code} ORDER BY version DESC, created_at DESC")
    List<ModerationPolicyTemplateDO> findAllVersionsByCode(@Param("code") String code);

    /**
     * 查询指定编码的最新版本号
     */
    @Select("SELECT MAX(version) FROM t_moderation_policy_template WHERE code = #{code}")
    String findLatestVersionByCode(@Param("code") String code);

    /**
     * 根据模板类型和语言查询模板
     */
    @Select("SELECT * FROM t_moderation_policy_template WHERE template_type = #{templateType} AND language = #{language} AND is_active = 1 ORDER BY version DESC LIMIT 1")
    ModerationPolicyTemplateDO findByTemplateTypeAndLanguage(@Param("templateType") String templateType, @Param("language") String language);
}