package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * RAG评估生成详情列表查询
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalGenerationDetailListQry {
    
    /**
     * 运行ID
     */
    private String runId;
    
    /**
     * 测试用例ID过滤
     */
    private String caseId;
    
    /**
     * 生成质量分数范围（最小值）
     */
    private Double minGenerationScore;
    
    /**
     * 生成质量分数范围（最大值）
     */
    private Double maxGenerationScore;
    
    /**
     * 是否包含失败的生成
     */
    private Boolean includeFailedGenerations;
    
    /**
     * 是否包含详细的生成过程
     */
    private Boolean includeGenerationProcess;
    
    /**
     * 页码，从1开始
     */
    private Integer pageNum;
    
    /**
     * 每页大小
     */
    private Integer pageSize;
    
    /**
     * 排序字段
     */
    private String sortBy;
    
    /**
     * 排序方向：asc/desc
     */
    private String sortOrder;
}
