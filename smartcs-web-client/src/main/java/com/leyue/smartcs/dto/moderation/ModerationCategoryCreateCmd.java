package com.leyue.smartcs.dto.moderation;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 创建违规分类命令
 *
 * @author Claude
 */
@Data
public class ModerationCategoryCreateCmd {

    /**
     * 父分类ID，NULL为一级分类
     */
    private Long parentId;

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    private String name;

    /**
     * 分类编码，用于程序识别
     */
    @NotBlank(message = "分类编码不能为空")
    private String code;

    /**
     * 分类描述
     */
    private String description;

    /**
     * 严重程度
     */
    @NotBlank(message = "严重程度不能为空")
    private String severityLevel;

    /**
     * 处理动作
     */
    @NotBlank(message = "处理动作不能为空")
    private String actionType;

    /**
     * 关键词
     */
    private List<String> keywords;

    /**
     * 是否启用
     */
    @NotNull(message = "启用状态不能为空")
    private Boolean enabled;

    /**
     * 排序号
     */
    private Integer sortOrder;
}
