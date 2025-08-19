package com.leyue.smartcs.domain.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * RAG评估数据集领域模型
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalDataset {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 数据集唯一标识符
     */
    private String datasetId;
    
    /**
     * 数据集名称
     */
    private String name;
    
    /**
     * 数据集描述
     */
    private String description;
    
    /**
     * 领域类型（如：customer_service、knowledge_base等）
     */
    private String domain;
    
    /**
     * 语言类型
     */
    private String language;
    
    /**
     * 总测试用例数
     */
    private Integer totalCases;
    
    /**
     * 活跃测试用例数
     */
    private Integer activeCases;
    
    /**
     * 创建者用户ID
     */
    private Long creatorId;
    
    /**
     * 创建者姓名
     */
    private String creatorName;
    
    /**
     * 标签信息
     */
    private List<String> tags;
    
    /**
     * 扩展元数据
     */
    private Map<String, Object> metadata;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 是否删除：0-否，1-是
     */
    private Integer isDeleted;
    
    /**
     * 创建人
     */
    private Long createdBy;
    
    /**
     * 更新人
     */
    private Long updatedBy;
    
    /**
     * 创建时间（毫秒时间戳）
     */
    private Long createdAt;
    
    /**
     * 更新时间（毫秒时间戳）
     */
    private Long updatedAt;
    
    /**
     * 检查数据集是否启用
     */
    public boolean isEnabled() {
        return status != null && status == 1;
    }
    
    /**
     * 检查数据集是否被删除
     */
    public boolean isDeleted() {
        return isDeleted != null && isDeleted == 1;
    }
    
    /**
     * 更新测试用例计数
     */
    public void updateCaseCounts(int total, int active) {
        this.totalCases = total;
        this.activeCases = active;
        this.updatedAt = System.currentTimeMillis();
    }
    
    /**
     * 添加标签
     */
    public void addTag(String tag) {
        if (this.tags != null && !this.tags.contains(tag)) {
            this.tags.add(tag);
            this.updatedAt = System.currentTimeMillis();
        }
    }
    
    /**
     * 移除标签
     */
    public void removeTag(String tag) {
        if (this.tags != null && this.tags.contains(tag)) {
            this.tags.remove(tag);
            this.updatedAt = System.currentTimeMillis();
        }
    }
}