package com.leyue.smartcs.dto.moderation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/**
 * 创建审核策略命令
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ModerationPolicyCreateCmd {

    /**
     * 策略名称
     */
    private String name;

    /**
     * 策略编码，用于程序识别
     */
    private String code;

    /**
     * 策略描述
     */
    private String description;

    /**
     * 适用场景
     */
    private String scenario;

    /**
     * 策略类型
     */
    private String policyType;

    /**
     * 默认风险等级
     */
    private String defaultRiskLevel;

    /**
     * 默认处理动作
     */
    private String defaultAction;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 策略配置参数
     */
    private Map<String, Object> configParams;

    /**
     * 关联的prompt模板ID
     */
    private Long templateId;

    /**
     * 创建者
     */
    private String createdBy;
}