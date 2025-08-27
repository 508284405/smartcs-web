package com.leyue.smartcs.moderation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.moderation.dataobject.ModerationDimensionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * 审核维度配置Mapper接口
 */
@Mapper
public interface ModerationDimensionMapper extends BaseMapper<ModerationDimensionDO> {

    /**
     * 根据分类查询维度列表（按排序权重排序）
     */
    @Select("SELECT * FROM t_moderation_dimension WHERE category = #{category} AND is_active = 1 ORDER BY sort_order ASC, severity_level DESC")
    List<ModerationDimensionDO> findByCategoryOrderBySortOrder(@Param("category") String category);

    /**
     * 根据编码查询维度
     */
    @Select("SELECT * FROM t_moderation_dimension WHERE code = #{code} AND is_active = 1")
    ModerationDimensionDO findByCode(@Param("code") String code);

    /**
     * 根据严重程度查询维度列表
     */
    @Select("SELECT * FROM t_moderation_dimension WHERE severity_level = #{severityLevel} AND is_active = 1 ORDER BY sort_order ASC")
    List<ModerationDimensionDO> findBySeverityLevel(@Param("severityLevel") String severityLevel);

    /**
     * 查询所有可用维度（按排序权重排序）
     */
    @Select("SELECT * FROM t_moderation_dimension WHERE is_active = 1 ORDER BY sort_order ASC, severity_level DESC, category ASC")
    List<ModerationDimensionDO> findAllActiveOrderBySortOrder();

    /**
     * 根据审核分类ID查询关联的维度列表
     */
    @Select("SELECT * FROM t_moderation_dimension WHERE category_id = #{categoryId} AND is_active = 1 ORDER BY sort_order ASC")
    List<ModerationDimensionDO> findByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 根据置信度阈值范围查询维度列表
     */
    @Select("SELECT * FROM t_moderation_dimension WHERE confidence_threshold BETWEEN #{minThreshold} AND #{maxThreshold} AND is_active = 1 ORDER BY confidence_threshold ASC")
    List<ModerationDimensionDO> findByConfidenceThresholdRange(@Param("minThreshold") Double minThreshold, @Param("maxThreshold") Double maxThreshold);
}