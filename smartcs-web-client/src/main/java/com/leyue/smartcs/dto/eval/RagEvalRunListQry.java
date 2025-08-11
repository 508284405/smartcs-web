package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * RAG评估运行列表查询
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalRunListQry {
    
    /**
     * 数据集ID过滤
     */
    private String datasetId;
    
    /**
     * 应用ID过滤
     */
    private Long appId;
    
    /**
     * 模型ID过滤
     */
    private Long modelId;
    
    /**
     * 运行类型过滤
     */
    private String runType;
    
    /**
     * 状态过滤
     */
    private String status;
    
    /**
     * 发起人用户ID过滤
     */
    private Long initiatorId;
    
    /**
     * 搜索关键词（在运行名称和描述中搜索）
     */
    private String searchKeyword;
    
    /**
     * 开始时间范围 - 开始时间
     */
    private Long startTimeFrom;
    
    /**
     * 开始时间范围 - 结束时间
     */
    private Long startTimeTo;
    
    /**
     * 页码，从1开始
     */
    private Integer pageNum;
    
    /**
     * 每页数量，默认10
     */
    private Integer pageSize;
    
    /**
     * 排序字段：runId, startTime, createdAt, updatedAt
     */
    private String sortField;
    
    /**
     * 排序方向：asc, desc，默认desc
     */
    private String sortOrder;
}
