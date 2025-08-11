package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * RAG评估数据集查询
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalDatasetGetQry {
    
    /**
     * 数据集ID
     */
    private String datasetId;
    
    /**
     * 是否包含详细信息
     */
    private Boolean includeDetails;
    
    /**
     * 是否包含测试用例统计
     */
    private Boolean includeCaseStats;
    
    /**
     * 是否包含运行历史
     */
    private Boolean includeRunHistory;
    
    /**
     * 是否包含元数据
     */
    private Boolean includeMetadata;
    
    /**
     * 是否包含权限信息
     */
    private Boolean includePermissions;
    
    /**
     * 是否包含标签信息
     */
    private Boolean includeTags;
    
    /**
     * 是否包含创建者信息
     */
    private Boolean includeCreatorInfo;
    
    /**
     * 是否包含版本历史
     */
    private Boolean includeVersionHistory;
    
    /**
     * 版本历史数量限制
     */
    private Integer versionHistoryLimit;
}