package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * RAG评估测试用例创建命令
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalCaseCreateCmd {
    
    /**
     * 所属数据集ID
     */
    private String datasetId;
    
    /**
     * 测试问题
     */
    private String question;
    
    /**
     * 期望的回答摘要
     */
    private String expectedSummary;
    
    /**
     * 标准证据引用
     */
    private List<String> goldEvidenceRefs;
    
    /**
     * 标准上下文
     */
    private List<String> groundTruthContexts;
    
    /**
     * 难度标签
     */
    private String difficultyTag;
    
    /**
     * 类别标签
     */
    private String category;
    
    /**
     * 查询类型
     */
    private String queryType;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 创建者用户ID
     */
    private Long creatorId;
    
    /**
     * 扩展属性
     */
    private Map<String, Object> extraProperties;
    
    /**
     * 备注信息
     */
    private String remarks;
    
    /**
     * 优先级：1-低，2-中，3-高
     */
    private Integer priority;
    
    /**
     * 预期评估时间（分钟）
     */
    private Integer expectedEvaluationTime;
}
