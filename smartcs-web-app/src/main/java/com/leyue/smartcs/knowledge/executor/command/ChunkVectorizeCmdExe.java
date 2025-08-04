package com.leyue.smartcs.knowledge.executor.command;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.alibaba.fastjson2.JSON;
import com.leyue.smartcs.domain.knowledge.Chunk;
import com.leyue.smartcs.domain.knowledge.gateway.ChunkGateway;
import com.leyue.smartcs.model.ai.DynamicModelManager;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 切片向量化命令执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChunkVectorizeCmdExe {

    private final ChunkGateway chunkGateway;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final DynamicModelManager dynamicModelManager;

    /**
     * 执行切片向量化命令
     * 
     * @param id 切片ID
     * @param modelId 模型ID
     * @return 操作结果
     */
    public Response execute(Long id, Long modelId) {
        log.info("开始对切片进行向量化处理，切片ID: {}", id);

        try {
            // 查询切片信息
            Chunk chunk = chunkGateway.findById(id);
            if (chunk == null) {
                throw new BizException("CHUNK_NOT_FOUND", "切片不存在: " + id);
            }

            // 检查切片内容
            if (!StringUtils.hasText(chunk.getContent())) {
                throw new BizException("CHUNK_CONTENT_EMPTY", "切片内容为空，无法进行向量化");
            }

            // 构建文档对象
            Map<String, Object> metadataMap = JSON.parseObject(chunk.getMetadata(), Map.class);
            Metadata metadata = Metadata.from(metadataMap);
            Document document = Document.from(chunk.getContent(), metadata);

            // 获取嵌入模型
            EmbeddingModel embeddingModel = dynamicModelManager.getEmbeddingModel(modelId);

            // 生成嵌入向量并创建TextSegment
            dev.langchain4j.data.embedding.Embedding embedding = embeddingModel.embed(document.text()).content();
            TextSegment textSegment = TextSegment.from(document.text(), document.metadata());

            // 存储到向量数据库
            embeddingStore.add(embedding, textSegment);

            log.info("切片向量化处理成功，切片ID: {}", id);
            return Response.buildSuccess();

        } catch (Exception e) {
            log.error("切片向量化处理失败，切片ID: {}, 错误: {}", id, e.getMessage(), e);
            throw new BizException("CHUNK_VECTORIZE_FAILED", "切片向量化处理失败: " + e.getMessage());
        }
    }
}