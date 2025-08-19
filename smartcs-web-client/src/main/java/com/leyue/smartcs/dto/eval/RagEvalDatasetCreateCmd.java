package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * RAG评估数据集创建命令
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalDatasetCreateCmd {
    
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
     * 预期测试用例数量
     */
    private Integer expectedCaseCount;
}