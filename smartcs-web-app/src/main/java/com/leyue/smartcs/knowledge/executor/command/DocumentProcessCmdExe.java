package com.leyue.smartcs.knowledge.executor.command;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.config.context.UserContext;
import com.leyue.smartcs.config.ModelBeanManagerService;
import com.leyue.smartcs.domain.knowledge.Content;
import com.leyue.smartcs.domain.knowledge.Chunk;
import com.leyue.smartcs.domain.knowledge.enums.ContentStatusEnum;
import com.leyue.smartcs.domain.knowledge.enums.SegmentMode;
import com.leyue.smartcs.domain.knowledge.gateway.ContentGateway;
import com.leyue.smartcs.domain.knowledge.gateway.ChunkGateway;
import com.leyue.smartcs.dto.knowledge.*;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 文档处理命令执行器
 * 负责完整的文档处理流程：创建内容 -> 分块 -> 向量化 -> 存储
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentProcessCmdExe {

    private final KnowledgeGeneralChunkCmdExe knowledgeGeneralChunkCmdExe;
    private final KnowledgeParentChildChunkCmdExe knowledgeParentChildChunkCmdExe;
    private final ContentGateway contentGateway;
    private final ChunkGateway chunkGateway;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final ModelBeanManagerService modelBeanManagerService;

    /**
     * 执行文档处理流程
     * 
     * @param cmd 处理命令
     * @return 处理结果
     */
    @Transactional(rollbackOn = Exception.class)
    public SingleResponse<DocumentProcessResultDTO> execute(@Valid DocumentProcessCmd cmd) {
        long startTime = System.currentTimeMillis();
        
        log.info("开始执行文档处理流程，知识库ID: {}, 文档标题: {}, 分段模式: {}", 
                cmd.getKnowledgeBaseId(), cmd.getTitle(), cmd.getSegmentMode());

        try {
            // 1. 创建文档记录
            Long contentId = createContentRecord(cmd);
            
            // 2. 文档分块处理
            List<ChunkDTO> chunks = processDocumentChunking(cmd);
            
            // 3. 批量保存分块结果到数据库
            List<Long> chunkIds = batchSaveChunks(chunks, contentId);
            
            // 4. 执行向量化处理
            int vectorCount = processVectorization(chunks, chunkIds);
            
            // 5. 计算技术参数并更新文档状态
            updateContentWithTechnicalParameters(contentId, chunks, startTime);
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            // 6. 构建响应结果
            DocumentProcessResultDTO result = new DocumentProcessResultDTO();
            result.setContentId(contentId);
            result.setChunkCount(chunks.size());
            result.setProcessingTime(processingTime);
            result.setTokenCount(calculateTotalTokens(chunks));
            result.setCharCount(calculateTotalChars(chunks));
            result.setEmbeddingCost(vectorCount); // 简化处理，使用向量数量作为成本
            result.setRecallCount(0L);
            result.setStatus("SUCCESS");

            log.info("文档处理流程完成，耗时: {}ms, 内容ID: {}, 分块数: {}, 向量数: {}", 
                    processingTime, contentId, chunks.size(), vectorCount);

            return SingleResponse.of(result);

        } catch (Exception e) {
            log.error("文档处理流程失败: {}", e.getMessage(), e);
            
            // 返回错误结果
            DocumentProcessResultDTO errorResult = new DocumentProcessResultDTO();
            errorResult.setStatus("ERROR");
            errorResult.setErrorMessage(e.getMessage());
            errorResult.setProcessingTime(System.currentTimeMillis() - startTime);
            
            return SingleResponse.of(errorResult);
        }
    }

    /**
     * 创建文档记录
     */
    private Long createContentRecord(DocumentProcessCmd cmd) {
        Long currentUserId = UserContext.getCurrentUser().getId();
        Long currentTime = System.currentTimeMillis();
        
        Content content = Content.builder()
                .knowledgeBaseId(cmd.getKnowledgeBaseId())
                .title(cmd.getTitle())
                .contentType("document")
                .fileType(cmd.getFileType())
                .fileUrl(cmd.getFileUrl())
                .originalFileName(cmd.getOriginalFileName())
                .fileSize(cmd.getFileSize())
                .source(cmd.getSource())
                .metadata(cmd.getMetadata())
                .status(ContentStatusEnum.DISABLED) // 初始状态为禁用，处理完成后启用
                .segmentMode(SegmentMode.fromCode(cmd.getSegmentMode()))
                .charCount(0L)
                .recallCount(0L)
                .processingStatus("processing")
                .createdBy(currentUserId)
                .createdAt(currentTime)
                .updatedAt(currentTime)
                .build();
        
        contentGateway.save(content);
        
        log.info("创建文档记录成功，ID: {}, 标题: {}", content.getId(), cmd.getTitle());
        return content.getId();
    }

    /**
     * 文档分块处理
     */
    private List<ChunkDTO> processDocumentChunking(DocumentProcessCmd cmd) {
        List<ChunkDTO> chunks = new ArrayList<>();
        
        if ("general".equals(cmd.getSegmentMode())) {
            // 通用分块
            KnowledgeGeneralChunkCmd chunkCmd = buildGeneralChunkCmd(cmd);
            var chunkResponse = knowledgeGeneralChunkCmdExe.execute(chunkCmd);
            if (chunkResponse.isSuccess()) {
                chunks.addAll(chunkResponse.getData());
                log.info("通用分块处理完成，分块数: {}", chunkResponse.getData().size());
            } else {
                throw new BizException("通用分块处理失败: " + chunkResponse.getErrMessage());
            }
        } else if ("parent_child".equals(cmd.getSegmentMode())) {
            // 父子分块
            KnowledgeParentChildChunkCmd chunkCmd = buildParentChildChunkCmd(cmd);
            var chunkResponse = knowledgeParentChildChunkCmdExe.execute(chunkCmd);
            if (chunkResponse.isSuccess()) {
                chunks.addAll(chunkResponse.getData());
                log.info("父子分块处理完成，分块数: {}", chunkResponse.getData().size());
            } else {
                throw new BizException("父子分块处理失败: " + chunkResponse.getErrMessage());
            }
        } else {
            throw new BizException("不支持的分段模式: " + cmd.getSegmentMode());
        }
        
        return chunks;
    }

    /**
     * 批量保存分块结果到数据库
     */
    private List<Long> batchSaveChunks(List<ChunkDTO> chunkDTOs, Long contentId) {
        List<Chunk> chunks = new ArrayList<>();
        Long currentTime = System.currentTimeMillis();
        
        for (int i = 0; i < chunkDTOs.size(); i++) {
            ChunkDTO chunkDTO = chunkDTOs.get(i);
            
            Chunk chunk = Chunk.builder()
                    .contentId(contentId)
                    .chunkIndex(String.valueOf(i))
                    .content(chunkDTO.getContent())
                    .tokenSize(calculateTokenCount(chunkDTO.getContent()))
                    .metadata(chunkDTO.getMetadata())
                    .createTime(currentTime)
                    .updateTime(currentTime)
                    .build();
            
            chunks.add(chunk);
        }
        
        // 使用批量保存方法
        List<Long> chunkIds = chunkGateway.saveBatch(chunks);
        
        log.info("批量保存分块到数据库完成，数量: {}", chunkIds.size());
        return chunkIds;
    }

    /**
     * 执行向量化处理
     */
    private int processVectorization(List<ChunkDTO> chunks, List<Long> chunkIds) {
        int vectorCount = 0;
        long embeddingStartTime = System.currentTimeMillis();
        
        // 获取嵌入模型
        EmbeddingModel embeddingModel = (EmbeddingModel) modelBeanManagerService.getFirstModelBean();
        if (embeddingModel == null) {
            log.warn("嵌入模型未找到，跳过向量化处理");
            return 0;
        }
        
        for (int i = 0; i < chunks.size() && i < chunkIds.size(); i++) {
            try {
                ChunkDTO chunkDTO = chunks.get(i);
                Long chunkId = chunkIds.get(i);
                
                // 创建TextSegment
                TextSegment textSegment = TextSegment.from(chunkDTO.getContent());
                
                // 生成嵌入向量
                var embedding = embeddingModel.embed(textSegment.text()).content();
                
                // 存储到向量数据库，并获取向量ID
                String vectorId = embeddingStore.add(embedding, textSegment);
                
                // 更新分块的向量ID
                if (vectorId != null) {
                    Chunk chunk = chunkGateway.findById(chunkId);
                    if (chunk != null) {
                        chunk.setVectorId(Long.valueOf(vectorId.hashCode())); // 简化处理
                        chunk.setUpdateTime(System.currentTimeMillis());
                        chunkGateway.update(chunk);
                    }
                }
                
                vectorCount++;
                
            } catch (Exception e) {
                log.warn("分块向量化失败，跳过该分块: {}", e.getMessage());
            }
        }
        
        long embeddingTime = System.currentTimeMillis() - embeddingStartTime;
        log.info("向量化处理完成，成功处理: {}/{}, 耗时: {}ms", vectorCount, chunks.size(), embeddingTime);
        
        // 更新内容的向量化时间
        updateContentEmbeddingTime(chunks.get(0).getContentId(), embeddingTime, vectorCount);
        
        return vectorCount;
    }

    /**
     * 更新内容的向量化时间和成本
     */
    private void updateContentEmbeddingTime(Long contentId, Long embeddingTime, int vectorCount) {
        Content content = contentGateway.findById(contentId);
        if (content != null) {
            content.setEmbeddingTime(embeddingTime);
            content.setEmbeddingCost((long) vectorCount);
            content.setUpdatedAt(System.currentTimeMillis());
            contentGateway.update(content);
        }
    }

    /**
     * 计算技术参数并更新文档状态
     */
    private void updateContentWithTechnicalParameters(Long contentId, List<ChunkDTO> chunks, long startTime) {
        Content content = contentGateway.findById(contentId);
        if (content != null) {
            // 计算技术参数
            long processingTime = System.currentTimeMillis() - startTime;
            int totalChars = calculateTotalChars(chunks).intValue();
            int averageChunkLength = chunks.isEmpty() ? 0 : totalChars / chunks.size();
            
            // 更新技术参数
            content.setProcessingTime(processingTime);
            content.setChunkCount(chunks.size());
            content.setCharCount((long) totalChars);
            content.setAverageChunkLength(averageChunkLength);
            content.setStatus(ContentStatusEnum.ENABLED);
            content.setProcessingStatus("success");
            content.setUpdatedAt(System.currentTimeMillis());
            
            contentGateway.update(content);
            
            log.info("更新内容技术参数完成，ID: {}, 处理时间: {}ms, 分块数: {}, 平均长度: {}", 
                    contentId, processingTime, chunks.size(), averageChunkLength);
        }
    }

    /**
     * 更新内容状态
     */
    private void updateContentStatus(Long contentId, ContentStatusEnum status) {
        Content content = contentGateway.findById(contentId);
        if (content != null) {
            content.setStatus(status);
            content.setUpdatedAt(System.currentTimeMillis());
            contentGateway.update(content);
            log.info("更新内容状态完成，ID: {}, 状态: {}", contentId, status);
        }
    }

    /**
     * 构建通用分块命令
     */
    private KnowledgeGeneralChunkCmd buildGeneralChunkCmd(DocumentProcessCmd cmd) {
        KnowledgeGeneralChunkCmd chunkCmd = new KnowledgeGeneralChunkCmd();
        chunkCmd.setFileUrl(cmd.getFileUrl());
        
        if (cmd.getSegmentSettings() != null) {
            var settings = cmd.getSegmentSettings();
            chunkCmd.setChunkSize(settings.getMaxLength());
            chunkCmd.setOverlapSize(settings.getOverlapLength());
            chunkCmd.setChunkSeparator(settings.getIdentifier());
            chunkCmd.setMinChunkSize(10);
            chunkCmd.setMaxChunkSize(5000);
            chunkCmd.setKeepSeparator(true);
            chunkCmd.setStripWhitespace(settings.getReplaceConsecutiveSpaces());
            chunkCmd.setRemoveAllUrls(settings.getRemoveAllUrls());
            chunkCmd.setUseQASegmentation(settings.getUseQASegmentation());
            chunkCmd.setQaLanguage(settings.getQaLanguage());
        } else {
            // 默认设置
            chunkCmd.setChunkSize(1000);
            chunkCmd.setOverlapSize(200);
            chunkCmd.setChunkSeparator("\n\n");
            chunkCmd.setMinChunkSize(10);
            chunkCmd.setMaxChunkSize(5000);
            chunkCmd.setKeepSeparator(true);
            chunkCmd.setStripWhitespace(true);
            chunkCmd.setRemoveAllUrls(false);
            chunkCmd.setUseQASegmentation(false);
            chunkCmd.setQaLanguage("Chinese");
        }
        
        return chunkCmd;
    }

    /**
     * 构建父子分块命令
     */
    private KnowledgeParentChildChunkCmd buildParentChildChunkCmd(DocumentProcessCmd cmd) {
        KnowledgeParentChildChunkCmd chunkCmd = new KnowledgeParentChildChunkCmd();
        chunkCmd.setFileUrl(cmd.getFileUrl());
        
        if (cmd.getParentChildSettings() != null) {
            var settings = cmd.getParentChildSettings();
            chunkCmd.setParentChunkSize(settings.getParentMaxLength());
            chunkCmd.setChildChunkSize(settings.getChildMaxLength());
            chunkCmd.setContextParagraphs(3);
            chunkCmd.setParentOverlapSize(50);
            chunkCmd.setChildOverlapSize(30);
            chunkCmd.setChunkSeparator(settings.getParentIdentifier());
            chunkCmd.setMinChunkSize(10);
            chunkCmd.setMaxChunkSize(5000);
            chunkCmd.setKeepSeparator(true);
            chunkCmd.setStripWhitespace(settings.getReplaceConsecutiveSpaces());
            chunkCmd.setRemoveAllUrls(settings.getRemoveAllUrls());
        } else {
            // 默认设置
            chunkCmd.setParentChunkSize(1500);
            chunkCmd.setChildChunkSize(600);   
            chunkCmd.setContextParagraphs(3);
            chunkCmd.setParentOverlapSize(100);
            chunkCmd.setChildOverlapSize(50);
            chunkCmd.setChunkSeparator("\n\n");
            chunkCmd.setMinChunkSize(10);
            chunkCmd.setMaxChunkSize(5000);
            chunkCmd.setKeepSeparator(true);
            chunkCmd.setStripWhitespace(true);
            chunkCmd.setRemoveAllUrls(false);
        }
        
        return chunkCmd;
    }

    /**
     * 计算Token数量（简单估算）
     */
    private Integer calculateTokenCount(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }
        // 简单估算：中文按字符数，英文按单词数的1.3倍
        return (int) Math.ceil(content.length() * 1.3);
    }

    /**
     * 计算总Token数
     */
    private Integer calculateTotalTokens(List<ChunkDTO> chunks) {
        return chunks.stream()
                .mapToInt(chunk -> calculateTokenCount(chunk.getContent()))
                .sum();
    }

    /**
     * 计算总字符数
     */
    private Long calculateTotalChars(List<ChunkDTO> chunks) {
        return chunks.stream()
                .mapToLong(chunk -> chunk.getContent() != null ? chunk.getContent().length() : 0)
                .sum();
    }
}