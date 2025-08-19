package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * RAG评估数据集DTO
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalDatasetDTO {
    
    /**
     * 数据集ID
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
     * 领域类型
     */
    private String domain;
    
    /**
     * 标签列表
     */
    private List<String> tags;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 是否公开：true-公开，false-私有
     */
    private Boolean isPublic;
    
    /**
     * 版本号
     */
    private String version;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 创建者用户ID
     */
    private Long creatorId;
    
    /**
     * 数据集来源
     */
    private String source;
    
    /**
     * 数据集大小（字节）
     */
    private Long size;
    
    /**
     * 测试用例数量
     */
    private Integer caseCount;
    
    /**
     * 运行次数
     */
    private Integer runCount;
    
    /**
     * 最后运行时间
     */
    private LocalDateTime lastRunTime;
    
    /**
     * 平均指标分数
     */
    private Map<String, Double> averageMetrics;
    
    /**
     * 扩展属性
     */
    private Map<String, Object> extraProperties;
    
    /**
     * 备注信息
     */
    private String remarks;
}