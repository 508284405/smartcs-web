package com.leyue.smartcs.dto.moderation;

import com.alibaba.cola.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 违规分类分页查询
 *
 * @author Claude
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ModerationCategoryPageQry extends PageQuery {

    /**
     * 分类名称（模糊查询）
     */
    private String name;

    /**
     * 分类编码（精确匹配）
     */
    private String code;

    /**
     * 父分类ID
     */
    private Long parentId;

    /**
     * 严重程度
     */
    private String severityLevel;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 是否只查询顶级分类
     */
    private Boolean onlyTopLevel;
}
