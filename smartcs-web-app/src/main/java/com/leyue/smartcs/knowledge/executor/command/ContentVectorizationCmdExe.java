package com.leyue.smartcs.knowledge.executor.command;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.config.context.UserContext;
import com.leyue.smartcs.domain.bot.gateway.LLMGateway;
import com.leyue.smartcs.domain.common.Constants;
import com.leyue.smartcs.domain.common.EmbeddingStructure;
import com.leyue.smartcs.domain.knowledge.Chunk;
import com.leyue.smartcs.domain.knowledge.Content;
import com.leyue.smartcs.domain.knowledge.Vector;
import com.leyue.smartcs.domain.knowledge.enums.ContentStatusEnum;
import com.leyue.smartcs.domain.knowledge.enums.StrategyNameEnum;
import com.leyue.smartcs.domain.knowledge.gateway.ContentGateway;
import com.leyue.smartcs.domain.knowledge.gateway.VectorGateway;
import com.leyue.smartcs.domain.knowledge.gateway.SearchGateway;
import com.leyue.smartcs.domain.utils.RedisearchUtils;
import com.leyue.smartcs.domain.knowledge.gateway.ChunkGateway;
import com.leyue.smartcs.knowledge.parser.SegmentStrategy;
import com.leyue.smartcs.knowledge.parser.factory.SegmentStrategyFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 内容向量化执行器
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ContentVectorizationCmdExe {

    private final ContentGateway contentGateway;
    private final ChunkGateway chunkGateway;
    private final SegmentStrategyFactory segmentStrategyFactory;
    private final LLMGateway llmGateway;
    private final VectorGateway vectorGateway;
    private final SearchGateway searchGateway;

    /**
     * 执行内容向量化
     */
    @Transactional(rollbackFor = Exception.class)
    public Response execute(Long contentId, StrategyNameEnum strategyName) {
        log.info("执行内容向量化, 内容ID: {}", contentId);

        try {

            // 查询内容
            Content content = contentGateway.findById(contentId);
            if (content == null) {
                throw new BizException("内容不存在");
            }

            // 如果是已上传，则报错提示
            if (ContentStatusEnum.UPLOADED.equals(content.getStatus())) {
                throw new BizException("请先解析内容");
            }

            // 检查是否有提取的文本
            if (!StringUtils.hasText(content.getTextExtracted())) {
                throw new BizException("请先解析内容");
            }

            // 执行向量化逻辑
            performVectorization(content, strategyName);

            // 更新状态为向量化完成
            content.setStatus(ContentStatusEnum.VECTORIZED);
            contentGateway.update(content);

            log.info("内容向量化完成, ID: {}", contentId);

            return Response.buildSuccess();

        } catch (Exception e) {
            log.error("内容向量化失败", e);
            throw new BizException("内容向量化失败: " + e.getMessage());
        }
    }

    /**
     * 执行实际的向量化逻辑
     */
    private void performVectorization(Content content, StrategyNameEnum strategyName) {
        log.info("开始向量化内容, ID: {}, 文本长度: {}", content.getId(), content.getTextExtracted().length());

        try {
            // 1. 文本分段
            List<String> textSegments = splitTextIntoSegments(content.getTextExtracted(), strategyName);
            log.info("文本分段完成, 共 {} 段", textSegments.size());
            // 2. 生成向量
            List<float[]> vectors = llmGateway.generateEmbeddings(textSegments);
            // 3. 存储向量到向量数据库
            storeVector(content.getKnowledgeBaseId(), content.getId(), vectors, textSegments, strategyName);
            log.info("内容向量化处理完成, 内容ID: {}, 总段数: {}", content.getId(), textSegments.size());
        } catch (Exception e) {
            log.error("向量化处理失败, 内容ID: {}", content.getId(), e);
            throw new RuntimeException("向量化处理失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将文本分段
     */
    private List<String> splitTextIntoSegments(String text, StrategyNameEnum strategyName) {
        log.debug("开始分段文本, 原始长度: {}", text.length());
        SegmentStrategy segmentStrategy = segmentStrategyFactory.getStrategy(strategyName);
        List<String> segments = segmentStrategy.segment(text);
        return segments;
    }

    /**
     * 存储向量到向量数据库
     */
    private void storeVector(Long kbId, Long contentId, List<float[]> vectors, List<String> textSegments,
            StrategyNameEnum strategyName) {
        // 1. 分段数据存到chunk表
        // 1.1 先清除chunk表中contentId为contentId的数据
        chunkGateway.deleteByContentId(contentId);
        // 1.2 再保存新的分段数据
        List<Chunk> chunks = new ArrayList<>(textSegments.size());
        for (int i = 0; i < textSegments.size(); i++) {
            Chunk chunk = new Chunk();
            chunk.setContentId(contentId);
            chunk.setChunkIndex(i);
            chunk.setText(textSegments.get(i));
            chunk.setCreatedBy(UserContext.getCurrentUser().getId());
            chunk.setStrategyName(strategyName);
            chunk.setCreatedAt(System.currentTimeMillis());
            chunk.setUpdatedAt(System.currentTimeMillis());
            chunks.add(chunk);
        }
        List<Chunk> chunkDOs = chunkGateway.saveBatch(contentId, chunks, strategyName);

        // 2. 向量数据存到向量数据库vector表
        // 2.1 先清除vector表中chunk的数据
        vectorGateway.deleteByChunkIds(chunkDOs.stream().map(Chunk::getId).collect(Collectors.toList()));
        List<Vector> vectorList = new ArrayList<>(chunkDOs.size());
        for (int i = 0; i < vectors.size(); i++) {
            Vector vector = new Vector();
            vector.setChunkId(chunkDOs.get(i).getId());
            vector.setEmbedding(RedisearchUtils.floatArrayToByteArray(vectors.get(i)));
            vector.setDim(vectors.get(i).length);
            vector.setProvider("openai");
            vector.setCreatedBy(UserContext.getCurrentUser().getId());
            vector.setCreatedAt(System.currentTimeMillis());
            vector.setUpdatedAt(System.currentTimeMillis());
            vectorList.add(vector);
        }
        List<Vector> savedVectors = vectorGateway.saveBatch(vectorList);
        // 获取向量ID和切片ID的映射关系
        Map<Long, Long> vectorIdChunkIdMap = savedVectors.stream()
                .collect(Collectors.toMap(Vector::getChunkId, Vector::getId));
        List<Chunk> chunkForUpdate = chunkDOs.stream().map(chunkDO -> {
            Chunk chunk = new Chunk();
            Long chunkId = chunkDO.getId();
            chunk.setId(chunkId);
            chunk.setVectorId(vectorIdChunkIdMap.get(chunkId));
            return chunk;
        }).collect(Collectors.toList());
        // 更新切片表的向量ID
        chunkGateway.updateBatchVectorId(chunkForUpdate);
        // 3. 更新redisearch索引
        for (int i = 0; i < savedVectors.size(); i++) {
            Vector savedVector = savedVectors.get(i);
            EmbeddingStructure embeddingStructure = new EmbeddingStructure();
            embeddingStructure.setKbId(kbId);
            embeddingStructure.setContentId(contentId);
            embeddingStructure.setChunkId(savedVector.getChunkId());
            embeddingStructure.setEmbedding(savedVector.getEmbedding());
            searchGateway.indexDocument(Constants.EMBEDDING_INDEX_REDISEARCH, savedVector.getId(), embeddingStructure);
        }
    }
}