package com.leyue.smartcs.knowledge.executor.command;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.alibaba.fastjson2.JSON;
import com.leyue.smartcs.config.ModelBeanManagerService;
import com.leyue.smartcs.domain.knowledge.Chunk;
import com.leyue.smartcs.domain.knowledge.gateway.ChunkGateway;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 切片向量化命令执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChunkVectorizeCmdExe {

    private final ChunkGateway chunkGateway;
    private final VectorStore vectorStore;
    private final ModelBeanManagerService modelBeanManagerService;

    /**
     * 执行切片向量化命令
     * 
     * @param id 切片ID
     * @return 操作结果
     */
    public Response execute(Long id) {
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
            Document document = Document.builder()
                    .id(chunk.getChunkIndex().toString())
                    .text(chunk.getContent())
                    .metadata(JSON.parseObject(chunk.getMetadata(), Map.class))
                    .build();

            // 使用TokenTextSplitter进行分段
            TokenTextSplitter splitter = new TokenTextSplitter(1000, 400, 10, 5000, true);
            List<Document> documents = splitter.apply(List.of(document));
            // 使用KeywordMetadataEnricher进行关键词提取
            ChatModel chatModel = (ChatModel) modelBeanManagerService.getFirstModelBean();
            if (chatModel != null) {
                KeywordMetadataEnricher enricher = new KeywordMetadataEnricher(chatModel, 5);
                documents = enricher.apply(documents);
            }

            // 存储到向量数据库
            vectorStore.delete(chunk.getChunkIndex().toString());
            vectorStore.add(documents);

            log.info("切片向量化处理成功，切片ID: {}", id);
            return Response.buildSuccess();

        } catch (Exception e) {
            log.error("切片向量化处理失败", e);
            throw new BizException("CHUNK_VECTORIZE_FAILED", "切片向量化处理失败: " + e.getMessage());
        }
    }
}