package com.leyue.smartcs.domain.knowledge.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FAQ领域模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Faq {
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 问题文本
     */
    private String question;
    
    /**
     * 答案文本
     */
    private String answer;
    
    /**
     * 命中次数
     */
    private Long hitCount;
    
    /**
     * 版本号
     */
    private Integer version;
    
    /**
     * 创建时间（毫秒时间戳）
     */
    private Long createdAt;
    
    /**
     * 更新时间（毫秒时间戳）
     */
    private Long updatedAt;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 增加命中次数
     * @return 增加后的命中次数
     */
    public Long incrementHitCount() {
        this.hitCount = (this.hitCount == null ? 0 : this.hitCount) + 1;
        return this.hitCount;
    }
    
    /**
     * 是否可用（未删除且已启用）
     * @return 是否可用
     */
    public Boolean isAvailable() {
        return this.enabled != null && this.enabled;
    }
} 