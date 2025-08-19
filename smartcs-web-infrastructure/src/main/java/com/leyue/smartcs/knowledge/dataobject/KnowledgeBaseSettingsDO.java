package com.leyue.smartcs.knowledge.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.leyue.smartcs.common.dao.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 知识库设置数据对象，对应t_kb_knowledge_base_settings表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_kb_knowledge_base_settings")
public class KnowledgeBaseSettingsDO extends BaseDO {
    
    /**
     * 知识库ID
     */
    @TableField("knowledge_base_id")
    private Long knowledgeBaseId;
    
    /**
     * 索引模式：high_quality(高质量)、economy(经济模式)
     */
    @TableField("indexing_mode")
    private String indexingMode;
    
    /**
     * 嵌入模型名称
     */
    @TableField("embedding_model")
    private String embeddingModel;
    
    /**
     * 向量搜索是否启用：0-禁用，1-启用
     */
    @TableField("vector_enabled")
    private Boolean vectorEnabled;
    
    /**
     * 向量搜索返回条数
     */
    @TableField("vector_top_k")
    private Integer vectorTopK;
    
    /**
     * 向量搜索相似度阈值
     */
    @TableField("vector_score_threshold")
    private BigDecimal vectorScoreThreshold;
    
    /**
     * 全文搜索是否启用：0-禁用，1-启用
     */
    @TableField("full_text_enabled")
    private Boolean fullTextEnabled;
    
    /**
     * 混合搜索是否启用：0-禁用，1-启用
     */
    @TableField("hybrid_enabled")
    private Boolean hybridEnabled;
    
    /**
     * 混合搜索重排是否启用：0-禁用，1-启用
     */
    @TableField("hybrid_rerank_enabled")
    private Boolean hybridRerankEnabled;
}