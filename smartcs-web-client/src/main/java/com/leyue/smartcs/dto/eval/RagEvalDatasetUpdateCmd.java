package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * RAG评估数据集更新命令
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalDatasetUpdateCmd {
    
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
     * 备注信息
     */
    private String remarks;
}