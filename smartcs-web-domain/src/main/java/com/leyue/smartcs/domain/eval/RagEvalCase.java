package com.leyue.smartcs.domain.eval;

import com.leyue.smartcs.domain.eval.enums.DifficultyTag;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * RAG评估测试用例领域模型
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalCase {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 测试用例唯一标识符
     */
    private String caseId;
    
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
     * 标准证据引用（包含文档片段、FAQ等）
     */
    private List<EvidenceReference> goldEvidenceRefs;
    
    /**
     * 标准上下文（用于Context Precision/Recall计算）
     */
    private List<GroundTruthContext> groundTruthContexts;
    
    /**
     * 难度标签
     */
    private DifficultyTag difficultyTag;
    
    /**
     * 类别标签（如：factual, reasoning, multi-hop等）
     */
    private String category;
    
    /**
     * 查询类型：simple, complex, ambiguous
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
     * 扩展元数据（如原始数据源信息）
     */
    private Map<String, Object> metadata;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 是否删除：0-否，1-是
     */
    private Integer isDeleted;
    
    /**
     * 创建人
     */
    private Long createdBy;
    
    /**
     * 更新人
     */
    private Long updatedBy;
    
    /**
     * 创建时间（毫秒时间戳）
     */
    private Long createdAt;
    
    /**
     * 更新时间（毫秒时间戳）
     */
    private Long updatedAt;
    
    /**
     * 检查测试用例是否启用
     */
    public boolean isEnabled() {
        return status != null && status == 1;
    }
    
    /**
     * 检查测试用例是否被删除
     */
    public boolean isDeleted() {
        return isDeleted != null && isDeleted == 1;
    }
    
    /**
     * 检查是否有标准证据
     */
    public boolean hasGoldEvidence() {
        return goldEvidenceRefs != null && !goldEvidenceRefs.isEmpty();
    }
    
    /**
     * 检查是否有标准上下文
     */
    public boolean hasGroundTruthContexts() {
        return groundTruthContexts != null && !groundTruthContexts.isEmpty();
    }
    
    /**
     * 标准证据引用
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EvidenceReference {
        /**
         * 证据类型：document, faq, knowledge_chunk
         */
        private String evidenceType;
        
        /**
         * 证据ID
         */
        private String evidenceId;
        
        /**
         * 证据内容
         */
        private String content;
        
        /**
         * 相关性权重
         */
        private Double relevanceWeight;
        
        /**
         * 证据元数据
         */
        private Map<String, Object> metadata;
    }
    
    /**
     * 标准上下文
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GroundTruthContext {
        /**
         * 上下文ID
         */
        private String contextId;
        
        /**
         * 上下文内容
         */
        private String content;
        
        /**
         * 期望排名
         */
        private Integer expectedRank;
        
        /**
         * 相关性分数
         */
        private Double relevanceScore;
        
        /**
         * 上下文元数据
         */
        private Map<String, Object> metadata;
    }
}