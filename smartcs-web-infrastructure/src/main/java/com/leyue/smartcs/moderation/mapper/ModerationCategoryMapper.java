package com.leyue.smartcs.moderation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.moderation.dataobject.ModerationCategoryDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 内容审核违规分类Mapper
 */
@Mapper
public interface ModerationCategoryMapper extends BaseMapper<ModerationCategoryDO> {

    /**
     * 查找顶级分类（一级分类）
     */
    @Select("SELECT * FROM t_moderation_category WHERE parent_id IS NULL AND is_active = 1 ORDER BY sort_order ASC, id ASC")
    List<ModerationCategoryDO> findTopLevelCategories();

    /**
     * 根据父ID查找子分类
     */
    @Select("SELECT * FROM t_moderation_category WHERE parent_id = #{parentId} AND is_active = 1 ORDER BY sort_order ASC, id ASC")
    List<ModerationCategoryDO> findSubCategoriesByParentId(@Param("parentId") Long parentId);

    /**
     * 根据编码查找分类
     */
    @Select("SELECT * FROM t_moderation_category WHERE code = #{code}")
    ModerationCategoryDO findByCode(@Param("code") String code);

    /**
     * 查找所有启用的分类
     */
    @Select("SELECT * FROM t_moderation_category WHERE is_active = 1 ORDER BY parent_id ASC, sort_order ASC, id ASC")
    List<ModerationCategoryDO> findAllActiveCategories();

    /**
     * 根据严重程度查找分类
     */
    @Select("SELECT * FROM t_moderation_category WHERE severity_level = #{severityLevel} AND is_active = 1 ORDER BY sort_order ASC")
    List<ModerationCategoryDO> findBySeverityLevel(@Param("severityLevel") String severityLevel);

    /**
     * 统计子分类数量
     */
    @Select("SELECT COUNT(*) FROM t_moderation_category WHERE parent_id = #{parentId}")
    int countSubCategories(@Param("parentId") Long parentId);

    /**
     * 检查编码是否存在
     */
    @Select("SELECT COUNT(*) FROM t_moderation_category WHERE code = #{code} AND id != #{excludeId}")
    int countByCodeExcludeId(@Param("code") String code, @Param("excludeId") Long excludeId);
}