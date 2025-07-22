package com.leyue.smartcs.model.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.leyue.smartcs.common.dao.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 模型实例数据对象，对应t_model表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_model")
public class ModelDO extends BaseDO {

    /**
     * 关联provider.id
     */
    private Long providerId;

    /**
     * 模型唯一标识
     */
    private String modelKey;

    /**
     * 名称
     */
    private String label;

    /**
     * 模型类型（llm/tts等）
     */
    private String modelType;

    /**
     * 能力标签（逗号分隔）
     */
    private String features;

    /**
     * 来源（如predefined-model）
     */
    private String fetchFrom;

    /**
     * 其他属性（如context_size, mode等，JSON格式）
     */
    private String modelProperties;

    /**
     * 是否废弃
     */
    private Boolean deprecated;

    /**
     * 状态（active/inactive）
     */
    private String status;

    /**
     * 是否负载均衡
     */
    private Boolean loadBalancingEnabled;
}