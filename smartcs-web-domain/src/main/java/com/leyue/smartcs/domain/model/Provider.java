package com.leyue.smartcs.domain.model;

import com.leyue.smartcs.domain.model.enums.ProviderType;
import lombok.Data;
import java.util.Arrays;
import java.util.List;

/**
 * 模型提供商领域模型
 */
@Data
public class Provider {
    
    /**
     * 主键
     */
    private Long id;
    
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
     * API Key（全局，仅用于创建和更新输入）
     */
    private String apiKey;
    
    /**
     * 是否已设置API Key（用于查询返回）
     */
    private Boolean hasApiKey;
    
    /**
     * API Endpoint
     */
    private String endpoint;
    
    /**
     * 支持的模型类型（逗号分隔）
     */
    private String supportedModelTypes;
    
    /**
     * 逻辑删除标识
     */
    private Integer isDeleted;
    
    /**
     * 创建人
     */
    private String createdBy;
    
    /**
     * 更新人
     */
    private String updatedBy;
    
    /**
     * 创建时间（毫秒时间戳）
     */
    private Long createdAt;
    
    /**
     * 更新时间（毫秒时间戳）
     */
    private Long updatedAt;
    
    /**
     * 验证提供商配置是否有效
     */
    public boolean isValid() {
        return providerType != null
                && endpoint != null && !endpoint.trim().isEmpty()
                && (hasApiKey == Boolean.TRUE || (apiKey != null && !apiKey.trim().isEmpty()));
    }
    
    /**
     * 是否已删除
     */
    public boolean isDeleted() {
        return isDeleted != null && isDeleted == 1;
    }
    
    /**
     * 标记为删除
     */
    public void markAsDeleted() {
        this.isDeleted = 1;
        this.updatedAt = System.currentTimeMillis();
    }
    
    /**
     * 获取支持的模型类型列表
     */
    public List<String> getSupportedModelTypesList() {
        if (supportedModelTypes == null || supportedModelTypes.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.asList(supportedModelTypes.split(","));
    }
    
    /**
     * 设置支持的模型类型列表
     */
    public void setSupportedModelTypesList(List<String> types) {
        if (types == null || types.isEmpty()) {
            this.supportedModelTypes = "";
        } else {
            this.supportedModelTypes = String.join(",", types);
        }
    }
}