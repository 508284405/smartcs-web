package com.leyue.smartcs.knowledge.listener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.config.context.UserContext;
import com.leyue.smartcs.domain.bot.gateway.LLMGateway;
import com.leyue.smartcs.domain.knowledge.Chunk;
import com.leyue.smartcs.domain.knowledge.Content;
import com.leyue.smartcs.domain.knowledge.event.DocEmbeddingGenerateEvent;
import com.leyue.smartcs.domain.knowledge.gateway.ChunkGateway;
import com.leyue.smartcs.domain.knowledge.gateway.ContentGateway;
import com.leyue.smartcs.domain.utils.OssFileDownloader;
import com.leyue.smartcs.dto.knowledge.enums.StrategyNameEnum;
import com.leyue.smartcs.knowledge.parser.DocumentParser;
import com.leyue.smartcs.knowledge.parser.SegmentStrategy;
import com.leyue.smartcs.knowledge.parser.factory.DocumentParserFactory;
import com.leyue.smartcs.knowledge.parser.factory.SegmentStrategyFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 文档向量生成事件监听器
 * 负责处理文档的内容解析、分段和向量生成
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocEmbeddingGenerateListener {

    private final LLMGateway llmGateway;
    private final DocumentParserFactory parserFactory;
    private final SegmentStrategyFactory segmentStrategyFactory;
    private final OssFileDownloader ossFileDownloader;
    private final ContentGateway contentGateway;
    private final ChunkGateway chunkGateway;

    /**
     * 异步处理文档向量生成事件
     *
     * @param event 文档向量生成事件
     */
    @Async("commonThreadPoolExecutor")
    @EventListener
    public void handleDocEmbeddingGenerate(DocEmbeddingGenerateEvent event) {
        log.info("收到文档向量生成事件: {}", event);

        try {
            // 1. 获取文档信息
            Long contentId = event.getContentId();
            Content content = contentGateway.findById(contentId);
            if (content == null) {
                log.error("文档不存在，无法生成向量，文档ID: {}", contentId);
                return;
            }

            log.info("开始处理文档: {}, URL: {}", content.getTitle(), content.getFileUrl());

            // 2. 下载并解析文档内容
            StrategyNameEnum strategyName = event.getStrategyName();
            List<String> contentChunks = parseDocumentContent(content, strategyName);
            if (contentChunks.isEmpty()) {
                log.warn("文档内容为空或解析失败，文档ID: {}", contentId);
                return;
            }

            log.info("文档解析完成，共分为 {} 个段落", contentChunks.size());

            // 3. 清除文档现有的向量数据
            chunkGateway.deleteByContentId(contentId,strategyName);

            // 5. 保存向量到数据库
            if (!contentChunks.isEmpty()) {
                List<Chunk> chunks = generateEmbeddings(contentId, contentChunks,strategyName);
                chunkGateway.saveBatch(contentId, chunks, strategyName);
            }
        } catch (Exception e) {
            log.error("文档向量生成失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 解析文档内容，并分段
     *
     * @param strategyName 解析策略名称
     * @return 分段后的内容列表
     */
    private List<String> parseDocumentContent(Content content, StrategyNameEnum strategyName) {
        log.info("开始解析文档内容: {}", content.getTitle());

        List<String> contentChunks = new ArrayList<>();
        File localFile = null;

        try {
            // 获取文件类型
            String fileType = content.getContentType();
            if (fileType == null || fileType.isEmpty()) {
                log.error("文档类型为空，无法解析: {}", content.getId());
                return contentChunks;
            }

            // 检查文档URL
            String ossUrl = content.getFileUrl();
            if (ossUrl == null || ossUrl.isEmpty()) {
                log.error("文档URL为空，无法下载: {}", content.getId());
                return contentChunks;
            }

            // 获取对应的解析器
            DocumentParser parser = parserFactory.getParser(fileType);
            if (parser == null) {
                log.error("不支持的文档类型: {}", fileType);
                return contentChunks;
            }

            // 下载文档
            localFile = ossFileDownloader.download(ossUrl);

            // 解析文档内容
            String fullText = parser.parseContent(content, localFile);

            // 分段
            if (fullText != null && !fullText.isEmpty()) {
                // 使用传入的策略名称，如果为空则使用默认策略
                SegmentStrategy strategy = segmentStrategyFactory.getStrategy(strategyName);
                contentChunks = strategy.segment(fullText);
                log.info("文档内容分段完成，共 {} 段，策略: {}",
                        contentChunks.size(), strategy.getStrategyName());
            }
        } catch (Exception e) {
            log.error("文档解析或分段过程出错: {}", e.getMessage(), e);
        }

        return contentChunks;
    }

    /**
     * 为文本段落生成向量
     *
     * @param contentChunks 文本段落列表
     * @param strategyName  解析策略名称
     * @return 向量对象列表
     */
    private List<Chunk> generateEmbeddings(Long contentId, List<String> contentChunks,StrategyNameEnum strategyName) {
        List<Chunk> chunks = new ArrayList<>();

        try {
            // 调用LLM服务生成向量
            List<float[]> vectors = llmGateway.generateEmbeddings(contentChunks);

            // 构建Embedding对象列表
            for (int i = 0; i < contentChunks.size(); i++) {
                Chunk chunk = Chunk.builder()
                        .contentId(contentId)
                        .chunkIndex(i)
                        .text(contentChunks.get(i))
                        .vectorId(String.valueOf(i))
                        .vector(vectors.get(i))
                        .strategyName(strategyName)
                        .createdBy(UserContext.getCurrentUser().getId())
                        .createdAt(System.currentTimeMillis())
                        .updatedAt(System.currentTimeMillis())
                        .build();

                chunks.add(chunk);
            }
        } catch (Exception e) {
            log.error("向量生成失败: {}", e.getMessage(), e);
            throw new BizException("向量生成失败: " + e.getMessage(), e);
        }

        return chunks;
    }
} 