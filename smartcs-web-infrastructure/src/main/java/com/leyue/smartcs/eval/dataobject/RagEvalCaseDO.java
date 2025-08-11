package com.leyue.smartcs.eval.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.leyue.smartcs.common.dao.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * RAG评估测试用例数据对象
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_rag_eval_case", autoResultMap = true)
public class RagEvalCaseDO extends BaseDO {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
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
    private String goldEvidenceRefs;
    
    /**
     * 标准上下文（用于Context Precision/Recall计算）
     */
    private String groundTruthContexts;
    
    /**
     * 难度标签：easy, medium, hard
     */
    private String difficultyTag;
    
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
}