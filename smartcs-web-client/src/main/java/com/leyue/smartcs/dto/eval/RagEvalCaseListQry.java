package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * RAG评估测试用例列表查询
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalCaseListQry {
    
    /**
     * 所属数据集ID
     */
    private String datasetId;
    
    /**
     * 类别标签过滤
     */
    private String category;
    
    /**
     * 难度标签过滤
     */
    private String difficultyTag;
    
    /**
     * 查询类型过滤
     */
    private String queryType;
    
    /**
     * 状态过滤：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 搜索关键词（在问题和期望答案中搜索）
     */
    private String searchKeyword;
    
    /**
     * 页码，从1开始
     */
    private Integer pageNum;
    
    /**
     * 每页数量，默认10
     */
    private Integer pageSize;
    
    /**
     * 排序字段：caseId, createdAt, updatedAt, difficultyTag, category
     */
    private String sortField;
    
    /**
     * 排序方向：asc, desc，默认desc
     */
    private String sortOrder;
}
