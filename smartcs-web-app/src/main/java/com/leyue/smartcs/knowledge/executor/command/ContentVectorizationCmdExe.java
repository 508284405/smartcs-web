package com.leyue.smartcs.knowledge.executor.command;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.config.context.UserContext;
import com.leyue.smartcs.domain.knowledge.Chunk;
import com.leyue.smartcs.domain.knowledge.Content;
import com.leyue.smartcs.domain.knowledge.enums.ContentStatusEnum;
import com.leyue.smartcs.domain.knowledge.enums.StrategyNameEnum;
import com.leyue.smartcs.domain.knowledge.gateway.ChunkGateway;
import com.leyue.smartcs.domain.knowledge.gateway.ContentGateway;
import com.leyue.smartcs.knowledge.parser.SegmentStrategy;
import com.leyue.smartcs.knowledge.parser.factory.SegmentStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private final VectorStore vectorStore;

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

            // 2. 将块文件存储到数据库
            // 2.1 先清除chunk表中contentId为contentId的数据
            chunkGateway.deleteByContentId(content.getId());
            // 2.2 再保存新的分段数据
            List<Chunk> chunks = new ArrayList<>(textSegments.size());
            for (int i = 0; i < textSegments.size(); i++) {
                Chunk chunk = new Chunk();
                chunk.setContentId(content.getId());
                chunk.setChunkIndex(i);
                chunk.setText(textSegments.get(i));
                chunk.setCreatedBy(UserContext.getCurrentUser().getId());
                chunk.setStrategyName(strategyName);
                chunk.setCreatedAt(System.currentTimeMillis());
                chunk.setUpdatedAt(System.currentTimeMillis());
                chunks.add(chunk);
            }
            List<Chunk> chunkDOs = chunkGateway.saveBatch(content.getId(), chunks, strategyName);

            List<Document> documents = chunkDOs.stream().map(chunk -> {
                Document document = new Document(chunk.getId().toString(), chunk.getText(), new HashMap<String, Object>());
                return document;
            }).collect(Collectors.toList());
            vectorStore.add(documents);
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
}