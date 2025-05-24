package com.leyue.smartcs.knowledge.listener;

import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.bot.gateway.LLMGateway;
import com.leyue.smartcs.domain.knowledge.event.DocEmbeddingGenerateEvent;
import com.leyue.smartcs.domain.knowledge.gateway.DocumentGateway;
import com.leyue.smartcs.domain.knowledge.gateway.EmbeddingGateway;
import com.leyue.smartcs.domain.knowledge.model.Document;
import com.leyue.smartcs.domain.knowledge.model.Embedding;
import com.leyue.smartcs.knowledge.factory.SegmentStrategyFactory;
import com.leyue.smartcs.knowledge.parser.DocumentParser;
import com.leyue.smartcs.knowledge.parser.SegmentStrategy;
import com.leyue.smartcs.knowledge.parser.factory.DocumentParserFactory;
import com.leyue.smartcs.knowledge.util.OssFileDownloader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 文档向量生成事件监听器
 * 负责处理文档的内容解析、分段和向量生成
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocEmbeddingGenerateListener {

    private final DocumentGateway documentGateway;
    private final EmbeddingGateway embeddingGateway;
    private final LLMGateway llmGateway;
    private final DocumentParserFactory parserFactory;
    private final SegmentStrategyFactory segmentStrategyFactory;
    private final OssFileDownloader ossFileDownloader;
    
    // 默认使用的向量模型类型
    private static final String DEFAULT_MODEL_TYPE = "text-embedding-ada-002";
    
    /**
     * 异步处理文档向量生成事件
     * @param event 文档向量生成事件
     */
    @Async("commonThreadPoolExecutor")
    @EventListener
    public void handleDocEmbeddingGenerate(DocEmbeddingGenerateEvent event) {
        log.info("收到文档向量生成事件: {}", event);
        
        try {
            // 1. 获取文档信息
            Long docId = event.getDocId();
            Optional<Document> docOpt = documentGateway.findById(docId);
            if (docOpt.isEmpty()) {
                log.error("文档不存在，无法生成向量，文档ID: {}", docId);
                return;
            }
            
            Document document = docOpt.get();
            log.info("开始处理文档: {}, URL: {}", document.getTitle(), document.getOssUrl());
            
            // 2. 下载并解析文档内容
            List<String> contentChunks = parseDocumentContent(document);
            if (contentChunks.isEmpty()) {
                log.warn("文档内容为空或解析失败，文档ID: {}", docId);
                return;
            }
            
            log.info("文档解析完成，共分为 {} 个段落", contentChunks.size());
            
            // 3. 清除文档现有的向量数据
            embeddingGateway.deleteByDocId(docId);
            
            // 4. 为每个段落生成向量
            List<Embedding> embeddings = generateEmbeddings(docId, contentChunks);
            
            // 5. 保存向量到数据库
            if (!embeddings.isEmpty()) {
                embeddingGateway.saveBatch(embeddings);
                log.info("文档向量生成完成，共 {} 个向量，文档ID: {}", embeddings.size(), docId);
            }
        } catch (Exception e) {
            log.error("文档向量生成失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 解析文档内容，并分段
     * @param document 文档对象
     * @return 分段后的内容列表
     */
    private List<String> parseDocumentContent(Document document) {
        log.info("开始解析文档内容: {}", document.getTitle());
        
        List<String> contentChunks = new ArrayList<>();
        File localFile = null;
        
        try {
            // 获取文件类型
            String fileType = document.getFileType();
            if (fileType == null || fileType.isEmpty()) {
                log.error("文档类型为空，无法解析: {}", document.getId());
                return contentChunks;
            }
            
            // 检查文档URL
            String ossUrl = document.getOssUrl();
            if (ossUrl == null || ossUrl.isEmpty()) {
                log.error("文档URL为空，无法下载: {}", document.getId());
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
            String fullText = parser.parseContent(document, localFile);
            
            // 分段
            if (fullText != null && !fullText.isEmpty()) {
                String strategyName = "Paragraph"; // 临时使用默认策略名
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
     * @param docId 文档ID
     * @param contentChunks 文本段落列表
     * @return 向量对象列表
     */
    private List<Embedding> generateEmbeddings(Long docId, List<String> contentChunks) {
        List<Embedding> embeddings = new ArrayList<>();
        
        try {
            // 调用LLM服务生成向量
            List<float[]> vectors = llmGateway.generateEmbeddings(contentChunks);
            
            // 构建Embedding对象列表
            for (int i = 0; i < contentChunks.size(); i++) {
                Embedding embedding = Embedding.builder()
                        .docId(docId)
                        .sectionIdx(i)
                        .contentSnip(contentChunks.get(i))
                        .vector(vectors.get(i))
                        .modelType(DEFAULT_MODEL_TYPE)
                        .createdAt(System.currentTimeMillis())
                        .updatedAt(System.currentTimeMillis())
                        .build();
                
                embeddings.add(embedding);
            }
        } catch (Exception e) {
            log.error("向量生成失败: {}", e.getMessage(), e);
            throw new BizException("向量生成失败: " + e.getMessage(), e);
        }
        
        return embeddings;
    }
} 