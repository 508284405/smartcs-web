package com.leyue.smartcs.knowledge.listener;

import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.bot.gateway.LLMGateway;
import com.leyue.smartcs.domain.knowledge.event.DocEmbeddingGenerateEvent;
import com.leyue.smartcs.domain.knowledge.gateway.DocumentGateway;
import com.leyue.smartcs.domain.knowledge.gateway.EmbeddingGateway;
import com.leyue.smartcs.domain.knowledge.model.Document;
import com.leyue.smartcs.domain.knowledge.model.Embedding;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

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
    
    // 默认使用的向量模型类型
    private static final String DEFAULT_MODEL_TYPE = "text-embedding-ada-002";
    // 默认的内容分段长度（字符数）
    private static final int DEFAULT_CHUNK_SIZE = 1000;
    
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
     * 注意：实际实现需要根据不同文件类型进行解析
     * @param document 文档对象
     * @return 分段后的内容列表
     */
    private List<String> parseDocumentContent(Document document) {
        // TODO: 这里需要根据不同文件类型（PDF、Word、TXT等）调用不同的解析器
        // 以下为简化版实现，假设已经有了文本内容
        
        String fileType = document.getFileType();
        String ossUrl = document.getOssUrl();
        
        // 模拟解析出的全文
        String fullText = "这是文档" + document.getTitle() + "的内容。这里应该是从OSS下载并解析文件得到的文本内容。";
        
        // 将文本分段，这里简化处理，实际应该根据语义或段落进行更智能的分段
        return splitTextIntoChunks(fullText, DEFAULT_CHUNK_SIZE);
    }
    
    /**
     * 将长文本分割成多个段落
     * @param text 完整文本
     * @param chunkSize 每段长度限制
     * @return 分段列表
     */
    private List<String> splitTextIntoChunks(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        
        if (text == null || text.isEmpty()) {
            return chunks;
        }
        
        // 简单按字符长度分段，实际应该按句子或段落分隔符进行更智能的分段
        for (int i = 0; i < text.length(); i += chunkSize) {
            int end = Math.min(i + chunkSize, text.length());
            chunks.add(text.substring(i, end));
        }
        
        return chunks;
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
            List<String> vectors = llmGateway.generateEmbeddings(contentChunks);
            
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