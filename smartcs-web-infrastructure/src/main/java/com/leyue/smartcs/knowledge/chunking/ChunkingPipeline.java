package com.leyue.smartcs.knowledge.chunking;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.leyue.smartcs.dto.knowledge.ChunkDTO;
import com.leyue.smartcs.knowledge.enums.DocumentTypeEnum;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 分块处理管道
 * 支持多个策略按照配置顺序串联执行
 */
@Data
@Builder
public class ChunkingPipeline {

    /**
     * 管道中的策略列表，按执行顺序排列
     */
    private List<ChunkingStrategy> strategies;

    /**
     * 文档类型
     */
    private DocumentTypeEnum documentType;

    /**
     * 管道配置参数
     */
    private Map<String, Object> config;

    /**
     * 是否启用并行处理（对于支持的策略）
     */
    private boolean enableParallel;

    /**
     * 执行分块管道
     *
     * @param inputDocuments 输入文档
     * @return 最终分块结果
     */
    public List<ChunkDTO> execute(List<Document> inputDocuments) {
        if (strategies == null || strategies.isEmpty()) {
            throw new IllegalArgumentException("分块管道中必须至少包含一个策略");
        }

        List<Document> currentDocuments = new ArrayList<>(inputDocuments);
        List<ChunkDTO> finalChunks = new ArrayList<>();

        for (ChunkingStrategy strategy : strategies) {
            // 验证策略配置
            if (!strategy.validateConfig(config)) {
                throw new IllegalArgumentException("策略 " + strategy.getName() + " 的配置参数无效");
            }

            // 执行当前策略
            List<ChunkDTO> strategyResult = strategy.chunk(currentDocuments, documentType, config);

            if (strategy.isCombinable() && strategy != strategies.get(strategies.size() - 1)) {
                // 如果策略可组合且不是最后一个策略，将结果转换为Document继续处理
                currentDocuments = convertChunksToDocuments(strategyResult);
            } else {
                // 如果策略不可组合或是最后一个策略，直接添加到最终结果
                finalChunks.addAll(strategyResult);
                break;
            }
        }

        return finalChunks;
    }

    /**
     * 将ChunkDTO列表转换回Document列表，用于管道中的下一个策略
     */
    private List<Document> convertChunksToDocuments(List<ChunkDTO> chunks) {
        List<Document> documents = new ArrayList<>();

        for (ChunkDTO chunk : chunks) {
            // 从元数据JSON字符串解析元数据
            Map<String, Object> metadata = parseMetadata(chunk.getMetadata());
            Document document = Document.from(chunk.getContent(), Metadata.from(metadata));
            documents.add(document);
        }

        return documents;
    }

    /**
     * 解析元数据JSON字符串
     */
    private Map<String, Object> parseMetadata(String metadataJson) {
        return JSONObject.parseObject(metadataJson, new TypeReference<>() {
        });
    }

    /**
     * 添加策略到管道末尾
     */
    public void addStrategy(ChunkingStrategy strategy) {
        if (strategies == null) {
            strategies = new ArrayList<>();
        }
        strategies.add(strategy);
    }

    /**
     * 在指定位置插入策略
     */
    public void insertStrategy(int index, ChunkingStrategy strategy) {
        if (strategies == null) {
            strategies = new ArrayList<>();
        }
        strategies.add(index, strategy);
    }

    /**
     * 移除指定策略
     */
    public void removeStrategy(ChunkingStrategy strategy) {
        if (strategies != null) {
            strategies.remove(strategy);
        }
    }
}