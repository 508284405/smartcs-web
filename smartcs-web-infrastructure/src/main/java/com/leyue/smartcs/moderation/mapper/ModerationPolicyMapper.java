package com.leyue.smartcs.moderation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.moderation.dataobject.ModerationPolicyDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * 审核策略配置Mapper接口
 */
@Mapper
public interface ModerationPolicyMapper extends BaseMapper<ModerationPolicyDO> {

    /**
     * 根据场景查询可用的策略列表（按优先级排序）
     */
    @Select("SELECT * FROM t_moderation_policy WHERE scenario = #{scenario} AND is_active = 1 ORDER BY priority ASC, created_at DESC")
    List<ModerationPolicyDO> findByScenarioOrderByPriority(@Param("scenario") String scenario);

    /**
     * 根据编码查询策略
     */
    @Select("SELECT * FROM t_moderation_policy WHERE code = #{code} AND is_active = 1")
    ModerationPolicyDO findByCode(@Param("code") String code);

    /**
     * 根据策略类型查询策略列表
     */
    @Select("SELECT * FROM t_moderation_policy WHERE policy_type = #{policyType} AND is_active = 1 ORDER BY priority ASC")
    List<ModerationPolicyDO> findByPolicyType(@Param("policyType") String policyType);

    /**
     * 查询所有可用策略（按优先级排序）
     */
    @Select("SELECT * FROM t_moderation_policy WHERE is_active = 1 ORDER BY priority ASC, scenario ASC, created_at DESC")
    List<ModerationPolicyDO> findAllActiveOrderByPriority();

    /**
     * 根据模板ID查询关联的策略列表
     */
    @Select("SELECT * FROM t_moderation_policy WHERE template_id = #{templateId} AND is_active = 1")
    List<ModerationPolicyDO> findByTemplateId(@Param("templateId") Long templateId);
}