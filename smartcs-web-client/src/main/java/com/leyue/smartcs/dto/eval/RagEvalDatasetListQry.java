package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * RAG评估数据集列表查询
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalDatasetListQry {
    
    /**
     * 数据集ID（用于过滤特定数据集）
     */
    private String datasetId;
    
    /**
     * 创建者用户ID
     */
    private Long creatorId;
    
    /**
     * 领域类型过滤
     */
    private String domain;
    
    /**
     * 状态过滤：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 标签过滤（包含任一标签即可）
     */
    private List<String> tags;
    
    /**
     * 搜索关键词（在名称和描述中搜索）
     */
    private String searchKeyword;
    
    /**
     * 创建时间范围 - 开始时间
     */
    private Long createdFrom;
    
    /**
     * 创建时间范围 - 结束时间
     */
    private Long createdTo;
    
    /**
     * 页码，从1开始
     */
    private Integer pageNum;
    
    /**
     * 每页数量，默认10
     */
    private Integer pageSize;
    
    /**
     * 排序字段：name, createdAt, updatedAt, totalCases, runCount
     */
    private String sortField;
    
    /**
     * 排序方向：asc, desc，默认desc
     */
    private String sortOrder;
}