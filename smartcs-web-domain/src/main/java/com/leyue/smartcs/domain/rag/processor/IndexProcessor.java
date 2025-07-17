package com.leyue.smartcs.domain.rag.processor;

import com.leyue.smartcs.domain.rag.model.*;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;

import java.util.List;

/**
 * 分段处理器抽象类
 */
public abstract class IndexProcessor {
    
    /**
     * 获取分割器
     * @param rule 分段规则
     * @param automatic 是否自动模式
     * @param model 嵌入模型
     * @return TokenTextSplitter
     */
    protected TokenTextSplitter getSplitter(SegmentationRule rule, boolean automatic, EmbeddingModel model) {
        int maxTokens = rule.getMaxTokens();
        int chunkOverlap = rule.getChunkOverlap();
        String separator = rule.getSeparator();
        
        // 校验自定义长度
        if (!automatic) {
            if (maxTokens < 50 || maxTokens > 10000) {  // 假设最大为10000
                throw new IllegalArgumentException("Custom segment length should be between 50 and 10000.");
            }
        }
        
        // 创建分割器
        if (separator != null && !separator.isEmpty()) {
            separator = separator.replace("\\n", "\n");
            return new TokenTextSplitter(rule.getMaxChunkSize(),rule.getMinChunkSize(), chunkOverlap, chunkOverlap, rule.isKeepSeparator());
        } else {
            // 自动分割
            return new TokenTextSplitter();  // 默认值
        }
    }
    
    /**
     * 抽取文档
     */
    public abstract List<Document> extract(ExtractSetting setting, ProcessorContext ctx);
    
    /**
     * 转换文档（分块）
     */
    public abstract List<Document> transform(List<Document> docs, ProcessorContext ctx);
    
    /**
     * 加载到向量库
     */
    public abstract void load(Dataset dataset, List<Document> docs, boolean withKeywords, ProcessorContext ctx);
    
    /**
     * 清理
     */
    public abstract void clean(Dataset dataset, List<String> nodeIds, boolean withKeywords, ProcessorContext ctx);
    
    /**
     * 检索
     */
    public abstract List<Document> retrieve(RetrieveParams params);
} 