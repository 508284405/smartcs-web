package com.leyue.smartcs.dto.moderation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审核策略分页查询
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ModerationPolicyPageQry {

    /**
     * 策略名称（模糊查询）
     */
    private String name;

    /**
     * 策略编码（精确查询）
     */
    private String code;

    /**
     * 适用场景
     */
    private String scenario;

    /**
     * 策略类型
     */
    private String policyType;

    /**
     * 是否启用
     */
    private Boolean isActive;

    /**
     * 模板ID
     */
    private Long templateId;

    /**
     * 页码（从1开始）
     */
    @Builder.Default
    private Integer pageNum = 1;

    /**
     * 页大小
     */
    @Builder.Default
    private Integer pageSize = 10;

    /**
     * 排序字段
     */
    @Builder.Default
    private String sortBy = "priority";

    /**
     * 排序方向（ASC/DESC）
     */
    @Builder.Default
    private String sortOrder = "ASC";
}