package com.leyue.smartcs.domain.app;

import com.leyue.smartcs.domain.app.enums.AppStatus;
import com.leyue.smartcs.domain.app.enums.AppType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * AI应用领域模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiApp {
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 应用名称
     */
    private String name;
    
    /**
     * 应用唯一编码
     */
    private String code;
    
    /**
     * 应用描述
     */
    private String description;
    
    /**
     * 应用类型
     */
    private AppType type;
    
    /**
     * 应用配置信息
     */
    private Map<String, Object> config;
    
    /**
     * 应用状态
     */
    private AppStatus status;
    
    /**
     * 应用图标
     */
    private String icon;
    
    /**
     * 应用标签
     */
    private List<String> tags;
    
    /**
     * 创建者
     */
    private String createdBy;
    
    /**
     * 更新者
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
     * 检查应用名称是否有效
     * @return 是否有效
     */
    public boolean isValidName() {
        return this.name != null && !this.name.trim().isEmpty() && this.name.length() <= 128;
    }
    
    /**
     * 检查应用编码是否有效
     * @return 是否有效
     */
    public boolean isValidCode() {
        return this.code != null && !this.code.trim().isEmpty() && this.code.length() <= 64;
    }
    
    /**
     * 检查应用是否可以使用
     * @return 是否可用
     */
    public boolean isUsable() {
        return this.status != null && this.status.isUsable();
    }
    
    /**
     * 检查应用是否可以编辑
     * @return 是否可编辑
     */
    public boolean isEditable() {
        return this.status != null && this.status.isEditable();
    }
    
    /**
     * 发布应用
     */
    public void publish() {
        if (this.status == AppStatus.DRAFT) {
            this.status = AppStatus.PUBLISHED;
            this.updatedAt = System.currentTimeMillis();
        }
    }
    
    /**
     * 停用应用
     */
    public void disable() {
        if (this.status == AppStatus.PUBLISHED) {
            this.status = AppStatus.DISABLED;
            this.updatedAt = System.currentTimeMillis();
        }
    }
    
    /**
     * 重新启用应用
     */
    public void enable() {
        if (this.status == AppStatus.DISABLED) {
            this.status = AppStatus.PUBLISHED;
            this.updatedAt = System.currentTimeMillis();
        }
    }
}