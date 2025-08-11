package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * RAG评估测试用例更新命令
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalCaseUpdateCmd {
    
    /**
     * 测试用例ID
     */
    private String caseId;
    
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
     * 期望检索到的相关文档数量
     */
    private Integer expectedRetrievalCount;
    
    /**
     * 评估备注
     */
    private String evaluationNotes;
    
    /**
     * 扩展元数据
     */
    private Map<String, Object> metadata;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
}
