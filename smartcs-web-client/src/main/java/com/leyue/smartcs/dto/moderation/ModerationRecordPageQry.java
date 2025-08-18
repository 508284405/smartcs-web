package com.leyue.smartcs.dto.moderation;

import com.alibaba.cola.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 审核记录分页查询
 *
 * @author Claude
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ModerationRecordPageQry extends PageQuery {

    /**
     * 记录ID
     */
    private String recordId;

    /**
     * 内容类型
     */
    private String contentType;

    /**
     * 内容来源
     */
    private String source;

    /**
     * 风险等级
     */
    private String riskLevel;

    /**
     * 审核状态
     */
    private String status;

    /**
     * 审核结果
     */
    private String result;

    /**
     * 审核方式
     */
    private String reviewType;

    /**
     * 审核员
     */
    private String reviewer;

    /**
     * 违规分类编码
     */
    private String categoryCode;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 内容关键词（模糊搜索）
     */
    private String contentKeyword;

    /**
     * 开始时间
     */
    private Long startTime;

    /**
     * 结束时间
     */
    private Long endTime;

    /**
     * 是否只查询待审核记录
     */
    private Boolean onlyPending;
}