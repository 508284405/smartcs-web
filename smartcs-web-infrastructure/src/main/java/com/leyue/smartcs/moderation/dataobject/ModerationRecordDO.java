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
import java.util.Map;

/**
 * 内容审核记录数据对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_moderation_record")
public class ModerationRecordDO {

    /**
     * 记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 内容哈希值，用于去重和缓存
     */
    @TableField("content_hash")
    private String contentHash;

    /**
     * 原始待审核内容
     */
    @TableField("original_content")
    private String originalContent;

    /**
     * 内容类型
     */
    @TableField("content_type")
    private String contentType;

    /**
     * 源ID，如消息ID、知识库ID等
     */
    @TableField("source_id")
    private String sourceId;

    /**
     * 来源类型
     */
    @TableField("source_type")
    private String sourceType;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private String userId;

    /**
     * 会话ID（如果适用）
     */
    @TableField("session_id")
    private String sessionId;

    // 审核结果相关字段

    /**
     * 审核结果
     */
    @TableField("moderation_result")
    private String moderationResult;

    /**
     * 风险等级
     */
    @TableField("risk_level")
    private String riskLevel;

    /**
     * 置信度分数 0.0000-1.0000
     */
    @TableField("confidence_score")
    private BigDecimal confidenceScore;

    /**
     * 是否被阻断
     */
    @TableField("is_blocked")
    private Integer isBlocked;

    // 违规信息

    /**
     * 违规分类列表，JSON格式存储
     */
    @TableField(value = "violation_categories", typeHandler = JacksonTypeHandler.class)
    private Object violationCategories;

    /**
     * AI分析结果，JSON格式存储
     */
    @TableField(value = "ai_analysis_result", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> aiAnalysisResult;

    /**
     * 匹配的关键词列表，JSON格式存储
     */
    @TableField(value = "keyword_matches", typeHandler = JacksonTypeHandler.class)
    private Object keywordMatches;

    // 审核方式和耗时

    /**
     * 审核方式，多个用逗号分隔
     */
    @TableField("moderation_methods")
    private String moderationMethods;

    /**
     * 使用的AI模型名称
     */
    @TableField("ai_model_used")
    private String aiModelUsed;

    /**
     * 处理耗时（毫秒）
     */
    @TableField("processing_time_ms")
    private Long processingTimeMs;

    // 人工审核相关

    /**
     * 人工审核状态
     */
    @TableField("manual_review_status")
    private String manualReviewStatus;

    /**
     * 人工审核员ID
     */
    @TableField("manual_reviewer_id")
    private String manualReviewerId;

    /**
     * 人工审核备注
     */
    @TableField("manual_review_notes")
    private String manualReviewNotes;

    /**
     * 人工审核时间
     */
    @TableField("manual_reviewed_at")
    private Long manualReviewedAt;

    // 后续处理

    /**
     * 采取的行动
     */
    @TableField("action_taken")
    private String actionTaken;

    /**
     * 升级给谁处理
     */
    @TableField("escalated_to")
    private String escalatedTo;

    /**
     * 升级时间
     */
    @TableField("escalated_at")
    private Long escalatedAt;

    // 元数据

    /**
     * 客户端IP地址
     */
    @TableField("client_ip")
    private String clientIp;

    /**
     * 用户代理信息
     */
    @TableField("user_agent")
    private String userAgent;

    /**
     * 请求ID，用于追踪
     */
    @TableField("request_id")
    private String requestId;

    /**
     * 扩展元数据，JSON格式存储
     */
    @TableField(value = "metadata", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> metadata;

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