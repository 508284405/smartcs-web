package com.leyue.smartcs.rag.processor;

import com.alibaba.fastjson2.JSON;
import com.leyue.smartcs.domain.knowledge.Chunk;
import com.leyue.smartcs.domain.knowledge.gateway.ChunkGateway;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;

/**
 * 段落索引处理器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ParagraphIndexProcessor {

    private final EmbeddingStore<Embedding> embeddingStore;
    private final ChunkGateway chunkGateway;
    private final EmbeddingModel embeddingModel;

    /**
     * 处理段落索引
     */
    public void processParagraphIndex(List<Chunk> chunks) {
        log.info("开始处理段落索引，共 {} 个段落", chunks.size());

        try {
            // 转换为文档
            List<Document> docs = new ArrayList<>();
            for (Chunk chunk : chunks) {
                Map<String, Object> metadataMap = JSON.parseObject(chunk.getMetadata(), Map.class);
                metadataMap.put("chunkId", chunk.getId());
                metadataMap.put("contentId", chunk.getContentId());
                Metadata metadata = Metadata.from(metadataMap);
                Document doc = Document.from(chunk.getContent(), metadata);
                docs.add(doc);
            }

            // 生成嵌入向量并存储
            for (Document doc : docs) {
                Embedding embedding = embeddingModel.embed(doc.text()).content();
                embeddingStore.add(embedding);
            }

            log.info("段落索引处理完成");

        } catch (Exception e) {
            log.error("段落索引处理失败", e);
            throw new RuntimeException("段落索引处理失败", e);
        }
    }

    /**
     * 搜索相关段落
     */
    public List<Document> searchRelevantParagraphs(String query, int topK) {
        try {
            // 生成查询向量
            Embedding queryEmbedding = embeddingModel.embed(query).content();

            // TODO: 实现向量搜索
            // 由于LangChain4j API的差异，这里需要根据具体的EmbeddingStore实现来调整
            log.warn("向量搜索功能需要根据具体的EmbeddingStore实现来调整");
            
            return List.of();

        } catch (Exception e) {
            log.error("搜索相关段落失败", e);
            return List.of();
        }
    }
} 