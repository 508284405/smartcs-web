package com.leyue.smartcs.dto.moderation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 关键词规则DTO
 *
 * @author Claude
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ModerationKeywordRuleDTO {

    /**
     * 规则ID
     */
    private Long id;

    /**
     * 规则名称
     */
    private String name;

    /**
     * 关键词内容
     */
    private String keyword;

    /**
     * 匹配类型
     */
    private String matchType;

    /**
     * 违规分类编码
     */
    private String categoryCode;

    /**
     * 违规分类名称
     */
    private String categoryName;

    /**
     * 处理动作
     */
    private String actionType;

    /**
     * 严重程度
     */
    private String severityLevel;

    /**
     * 相似度阈值
     */
    private Double similarityThreshold;

    /**
     * 语言
     */
    private String language;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 描述
     */
    private String description;

    /**
     * 标签
     */
    private List<String> tags;

    /**
     * 生效时间
     */
    private Long effectiveTime;

    /**
     * 失效时间
     */
    private Long expireTime;

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
     * 命中次数
     */
    private Integer hitCount;

    /**
     * 最后命中时间
     */
    private Long lastHitTime;
}