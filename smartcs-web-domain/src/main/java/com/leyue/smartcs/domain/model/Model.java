package com.leyue.smartcs.domain.model;

import com.leyue.smartcs.domain.model.enums.FetchFrom;
import com.leyue.smartcs.domain.model.enums.ModelStatus;
import com.leyue.smartcs.domain.model.enums.ModelType;
import lombok.Data;
import java.util.Arrays;
import java.util.List;

/**
 * 模型实例领域模型
 */
@Data
public class Model {
    
    /**
     * 主键
     */
    private Long id;
    
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
     * 模型类型
     */
    private ModelType modelType;
    
    /**
     * 能力标签（逗号分隔）
     */
    private String features;
    
    /**
     * 来源
     */
    private FetchFrom fetchFrom;
    
    /**
     * 其他属性（如context_size, mode等，JSON格式）
     */
    private String modelProperties;
    
    /**
     * 是否废弃
     */
    private Boolean deprecated;
    
    /**
     * 状态
     */
    private ModelStatus status;
    
    /**
     * 是否负载均衡
     */
    private Boolean loadBalancingEnabled;
    
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
     * 验证模型配置是否有效
     */
    public boolean isValid() {
        return modelKey != null && !modelKey.trim().isEmpty()
                && label != null && !label.trim().isEmpty()
                && providerId != null
                && modelType != null
                && status != null;
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
     * 是否启用
     */
    public boolean isActive() {
        return status == ModelStatus.ACTIVE && !Boolean.TRUE.equals(deprecated);
    }
    
    /**
     * 启用模型
     */
    public void enable() {
        this.status = ModelStatus.ACTIVE;
        this.updatedAt = System.currentTimeMillis();
    }
    
    /**
     * 禁用模型
     */
    public void disable() {
        this.status = ModelStatus.INACTIVE;
        this.updatedAt = System.currentTimeMillis();
    }
    
    /**
     * 废弃模型
     */
    public void deprecate() {
        this.deprecated = true;
        this.updatedAt = System.currentTimeMillis();
    }
    
    /**
     * 获取特性列表
     */
    public List<String> getFeaturesList() {
        if (features == null || features.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.asList(features.split(","));
    }
    
    /**
     * 设置特性列表
     */
    public void setFeaturesList(List<String> featuresList) {
        if (featuresList == null || featuresList.isEmpty()) {
            this.features = "";
        } else {
            this.features = String.join(",", featuresList);
        }
    }
}