package com.leyue.smartcs.model.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import com.leyue.smartcs.common.dao.BaseDO;
import com.leyue.smartcs.domain.model.enums.ProviderType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 模型提供商数据对象，对应t_model_provider表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_model_provider")
public class ProviderDO extends BaseDO {

    /**
     * 提供商类型
     */
    private ProviderType providerType;

    /**
     * 名称
     */
    private String label;

    /**
     * 小图标URL
     */
    private String iconSmall;

    /**
     * 大图标URL
     */
    private String iconLarge;

    /**
     * API Key（全局）
     */
    private String apiKey;

    /**
     * API Endpoint
     */
    private String endpoint;

    /**
     * 支持的模型类型（逗号分隔）
     */
    private String supportedModelTypes;
}