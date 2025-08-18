package com.leyue.smartcs.dto.moderation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 违规分类DTO
 *
 * @author Claude
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ModerationCategoryDTO {

    /**
     * 分类ID
     */
    private Long id;

    /**
     * 父分类ID，NULL为一级分类
     */
    private Long parentId;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类编码，用于程序识别
     */
    private String code;

    /**
     * 分类描述
     */
    private String description;

    /**
     * 严重程度
     */
    private String severityLevel;

    /**
     * 处理动作
     */
    private String actionType;

    /**
     * 关键词（JSON数组）
     */
    private List<String> keywords;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 更新时间
     */
    private Long updateTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 子分类列表（用于树形结构）
     */
    private List<ModerationCategoryDTO> children;

    /**
     * 分类路径（如：一级分类/二级分类）
     */
    private String categoryPath;

    /**
     * 使用该分类的规则数量
     */
    private Integer ruleCount;

    /**
     * 该分类下的违规记录数量
     */
    private Integer violationCount;
}