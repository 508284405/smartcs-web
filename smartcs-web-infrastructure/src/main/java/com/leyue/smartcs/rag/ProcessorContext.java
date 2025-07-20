package com.leyue.smartcs.rag;

import com.leyue.smartcs.domain.rag.model.Rule;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 处理器上下文
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessorContext {

    /**
     * 处理规则
     */
    private Rule processRule;

    /**
     * 嵌入模型实例
     */
    private EmbeddingModel embeddingModelInstance;

    /**
     * 关键词列表（可选）
     */
    private List<String> keywordsList;

    /**
     * 额外上下文参数
     */
    private Map<String, Object> extraParams;
} 