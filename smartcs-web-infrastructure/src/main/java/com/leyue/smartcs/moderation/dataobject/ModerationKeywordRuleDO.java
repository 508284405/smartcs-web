package com.leyue.smartcs.moderation.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 内容审核关键词规则数据对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_moderation_keyword_rule")
public class ModerationKeywordRuleDO {

    /**
     * 规则ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 规则名称
     */
    @TableField("rule_name")
    private String ruleName;

    /**
     * 关键词或正则表达式
     */
    @TableField("keyword")
    private String keyword;

    /**
     * 关联的违规分类ID
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 规则类型
     */
    @TableField("rule_type")
    private String ruleType;

    /**
     * 匹配模式
     */
    @TableField("match_mode")
    private String matchMode;

    /**
     * 是否大小写敏感
     */
    @TableField("case_sensitive")
    private Integer caseSensitive;

    /**
     * 严重程度权重
     */
    @TableField("severity_weight")
    private BigDecimal severityWeight;

    /**
     * 相似度阈值（用于模糊匹配）
     */
    @TableField("similarity_threshold")
    private BigDecimal similarityThreshold;

    /**
     * 上下文窗口大小
     */
    @TableField("context_window")
    private Integer contextWindow;

    /**
     * 白名单上下文，JSON格式存储
     */
    @TableField(value = "whitelist_contexts", typeHandler = JacksonTypeHandler.class)
    private Object whitelistContexts;

    /**
     * 是否启用
     */
    @TableField("is_active")
    private Integer isActive;

    /**
     * 优先级
     */
    @TableField("priority")
    private Integer priority;

    /**
     * 动作覆盖
     */
    @TableField("action_override")
    private String actionOverride;

    /**
     * 命中次数
     */
    @TableField("hit_count")
    private Long hitCount;

    /**
     * 最后命中时间
     */
    @TableField("last_hit_at")
    private Long lastHitAt;

    /**
     * 规则描述
     */
    @TableField("description")
    private String description;

    /**
     * 规则来源
     */
    @TableField("source")
    private String source;

    /**
     * 适用语言
     */
    @TableField("language")
    private String language;

    /**
     * 标签
     */
    @TableField("tags")
    private String tags;

    /**
     * 生效时间
     */
    @TableField("effective_from")
    private Long effectiveFrom;

    /**
     * 失效时间
     */
    @TableField("effective_until")
    private Long effectiveUntil;

    /**
     * 创建者
     */
    @TableField("created_by")
    private String createdBy;

    /**
     * 更新者
     */
    @TableField("updated_by")
    private String updatedBy;

    /**
     * 创建时间（毫秒时间戳）
     */
    @TableField("created_at")
    private Long createdAt;

    /**
     * 更新时间（毫秒时间戳）
     */
    @TableField("updated_at")
    private Long updatedAt;
}