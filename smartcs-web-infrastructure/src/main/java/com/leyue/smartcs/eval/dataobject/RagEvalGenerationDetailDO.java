package com.leyue.smartcs.eval.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * RAG评估生成详情数据对象
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@TableName(value = "t_rag_eval_generation_detail", autoResultMap = true)
public class RagEvalGenerationDetailDO {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 运行ID
     */
    private String runId;
    
    /**
     * 测试用例ID
     */
    private String caseId;
    
    /**
     * 原始查询
     */
    private String queryOriginal;
    
    /**
     * 注入的上下文信息
     */
    private String injectedContexts;
    
    // ====== 提示构建 ======
    
    /**
     * 最终构建的提示词
     */
    private String finalPrompt;
    
    /**
     * 使用的提示模板
     */
    private String promptTemplate;
    
    /**
     * 提示Token数量
     */
    private Integer promptTokens;
    
    /**
     * 上下文注入策略
     */
    private String contextInjectionStrategy;
    
    // ====== 生成结果 ======
    
    /**
     * 生成的答案
     */
    private String generatedAnswer;
    
    /**
     * 答案Token数量
     */
    private Integer answerTokens;
    
    /**
     * 生成延迟（毫秒）
     */
    private Integer generationLatencyMs;
    
    /**
     * 使用的生成模型
     */
    private String modelUsed;
    
    /**
     * 模型参数（温度、TopP等）
     */
    private String modelParams;
    
    /**
     * 是否启用流式生成：0-否，1-是
     */
    private Integer streamingEnabled;
    
    // ====== 引用和依据 ======
    
    /**
     * 引用信息（来源文档、位置等）
     */
    private String citations;
    
    /**
     * 支持证据
     */
    private String supportingEvidence;
    
    /**
     * 引用准确性
     */
    private BigDecimal citationAccuracy;
    
    /**
     * 证据对齐度
     */
    private BigDecimal evidenceAlignment;
    
    // ====== RAGAS生成指标 ======
    
    /**
     * RAGAS Faithfulness分数
     */
    private BigDecimal faithfulnessScore;
    
    /**
     * RAGAS Answer Relevancy分数
     */
    private BigDecimal answerRelevancyScore;
    
    /**
     * 基于事实程度分数
     */
    private BigDecimal groundednessScore;
    
    // ====== 质量评估指标 ======
    
    /**
     * 事实正确性分数
     */
    private BigDecimal factualCorrectnessScore;
    
    /**
     * 完整性分数
     */
    private BigDecimal completenessScore;
    
    /**
     * 简洁性分数
     */
    private BigDecimal concisenessScore;
    
    /**
     * 连贯性分数
     */
    private BigDecimal coherenceScore;
    
    /**
     * 流畅性分数
     */
    private BigDecimal fluencyScore;
    
    // ====== 引用一致性检查 ======
    
    /**
     * 引用一致性分数
     */
    private BigDecimal citationConsistencyScore;
    
    /**
     * 是否检测到幻觉：0-否，1-是
     */
    private Integer hallucinationDetected;
    
    /**
     * 幻觉严重程度：low, medium, high
     */
    private String hallucinationSeverity;
    
    /**
     * 不支持的声明列表
     */
    private String unsupportedClaims;
    
    // ====== 与期望答案的比较 ======
    
    /**
     * 期望的答案（来自测试用例）
     */
    private String expectedAnswer;
    
    /**
     * 语义相似度
     */
    private BigDecimal semanticSimilarity;
    
    /**
     * BLEU分数
     */
    private BigDecimal bleuScore;
    
    /**
     * ROUGE-L分数
     */
    private BigDecimal rougeLScore;
    
    /**
     * BERTScore F1分数
     */
    private BigDecimal bertscoreF1;
    
    // ====== 成本分析 ======
    
    /**
     * 总Token数量
     */
    private Integer totalTokens;
    
    /**
     * 成本（美元）
     */
    private BigDecimal costUsd;
    
    /**
     * 成本分解（输入、输出Token成本）
     */
    private String costBreakdown;
    
    // ====== 错误和异常 ======
    
    /**
     * 是否有错误：0-否，1-是
     */
    private Integer hasError;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 错误阶段：prompt_build, generation, evaluation
     */
    private String errorStage;
    
    // ====== 调试和分析 ======
    
    /**
     * 生成步骤详情（流式生成时的中间结果）
     */
    private String generationSteps;
    
    /**
     * 注意力权重（如果可用）
     */
    private String attentionWeights;
    
    /**
     * 调试信息
     */
    private Map<String, Object> debugInfo;
    
    /**
     * 扩展元数据
     */
    private Map<String, Object> metadata;
    
    /**
     * 创建时间（毫秒时间戳）
     */
    private Long createdAt;

    /**
     * 更新时间（毫秒时间戳）
     */
    private Long updatedAt;
}