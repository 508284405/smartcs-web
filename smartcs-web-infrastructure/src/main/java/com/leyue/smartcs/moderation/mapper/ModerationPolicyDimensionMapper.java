package com.leyue.smartcs.moderation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.moderation.dataobject.ModerationPolicyDimensionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

/**
 * 审核策略维度关联Mapper接口
 */
@Mapper
public interface ModerationPolicyDimensionMapper extends BaseMapper<ModerationPolicyDimensionDO> {

    /**
     * 根据策略ID查询关联的维度列表
     */
    @Select("SELECT pd.*, d.name as dimension_name, d.code as dimension_code, d.description as dimension_description, " +
            "d.check_guideline, d.severity_level, d.action_type, d.confidence_threshold, d.sort_order, d.category " +
            "FROM t_moderation_policy_dimension pd " +
            "LEFT JOIN t_moderation_dimension d ON pd.dimension_id = d.id " +
            "WHERE pd.policy_id = #{policyId} AND pd.is_active = 1 AND d.is_active = 1 " +
            "ORDER BY d.sort_order ASC, d.severity_level DESC")
    List<ModerationPolicyDimensionDO> findByPolicyIdWithDimensionInfo(@Param("policyId") Long policyId);

    /**
     * 根据维度ID查询关联的策略列表
     */
    @Select("SELECT pd.*, p.name as policy_name, p.code as policy_code, p.scenario, p.policy_type " +
            "FROM t_moderation_policy_dimension pd " +
            "LEFT JOIN t_moderation_policy p ON pd.policy_id = p.id " +
            "WHERE pd.dimension_id = #{dimensionId} AND pd.is_active = 1 AND p.is_active = 1 " +
            "ORDER BY p.priority ASC")
    List<ModerationPolicyDimensionDO> findByDimensionIdWithPolicyInfo(@Param("dimensionId") Long dimensionId);

    /**
     * 查询策略和维度的关联关系
     */
    @Select("SELECT * FROM t_moderation_policy_dimension WHERE policy_id = #{policyId} AND dimension_id = #{dimensionId}")
    ModerationPolicyDimensionDO findByPolicyIdAndDimensionId(@Param("policyId") Long policyId, @Param("dimensionId") Long dimensionId);

    /**
     * 批量更新策略维度关联的启用状态
     */
    @Update("UPDATE t_moderation_policy_dimension SET is_active = #{isActive}, updated_by = #{updatedBy}, updated_at = #{updatedAt} " +
            "WHERE policy_id = #{policyId} AND dimension_id IN (${dimensionIds})")
    int batchUpdateActiveStatus(@Param("policyId") Long policyId, 
                               @Param("dimensionIds") String dimensionIds, 
                               @Param("isActive") Boolean isActive,
                               @Param("updatedBy") String updatedBy,
                               @Param("updatedAt") Long updatedAt);

    /**
     * 根据策略ID查询所有维度关联（包括未启用的）
     */
    @Select("SELECT pd.*, d.name as dimension_name, d.code as dimension_code, d.description as dimension_description, " +
            "d.check_guideline, d.severity_level, d.action_type, d.confidence_threshold, d.sort_order, d.category " +
            "FROM t_moderation_policy_dimension pd " +
            "LEFT JOIN t_moderation_dimension d ON pd.dimension_id = d.id " +
            "WHERE pd.policy_id = #{policyId} " +
            "ORDER BY d.sort_order ASC, d.severity_level DESC")
    List<ModerationPolicyDimensionDO> findAllByPolicyId(@Param("policyId") Long policyId);

    /**
     * 根据策略ID删除所有维度关联
     */
    @Update("DELETE FROM t_moderation_policy_dimension WHERE policy_id = #{policyId}")
    int deleteByPolicyId(@Param("policyId") Long policyId);

    /**
     * 根据维度ID删除所有策略关联
     */
    @Update("DELETE FROM t_moderation_policy_dimension WHERE dimension_id = #{dimensionId}")
    int deleteByDimensionId(@Param("dimensionId") Long dimensionId);
}