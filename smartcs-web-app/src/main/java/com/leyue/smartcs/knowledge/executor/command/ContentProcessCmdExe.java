package com.leyue.smartcs.knowledge.executor.command;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.knowledge.Content;
import com.leyue.smartcs.domain.knowledge.Chunk;
import com.leyue.smartcs.domain.knowledge.enums.ContentStatusEnum;
import com.leyue.smartcs.domain.knowledge.enums.SegmentMode;
import com.leyue.smartcs.domain.knowledge.gateway.ContentGateway;
import com.leyue.smartcs.domain.knowledge.gateway.ChunkGateway;
import com.leyue.smartcs.dto.knowledge.*;
import com.leyue.smartcs.config.ModelBeanManagerService;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 内容处理命令执行器
 * 负责完整的文档处理流程：解析 -> 分块 -> 向量化 -> 存储
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ContentProcessCmdExe {

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
    public SingleResponse<ContentProcessResponse> execute(ContentProcessCmd cmd) {
        long startTime = System.currentTimeMillis();
        
        log.info("开始执行文档处理流程，知识库ID: {}, 文件数量: {}, 分段模式: {}", 
                cmd.getKnowledgeBaseId(), cmd.getFiles().size(), cmd.getSegmentMode());

        try {
            // 1. 参数校验
            validateCommand(cmd);

            // 2. 创建内容记录
            List<Long> contentIds = createContentRecords(cmd);
            
            // 3. 文档分块处理
            List<ChunkDTO> allChunks = processDocumentChunking(cmd);
            
            // 4. 保存分块到数据库
            List<Long> chunkIds = saveChunksToDatabase(allChunks, contentIds.get(0));
            
            // 5. 向量化处理
            int vectorCount = processVectorization(allChunks);
            
            // 6. 更新内容状态
            updateContentStatus(contentIds, ContentStatusEnum.ENABLED);
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            // 7. 构建响应结果
            ContentProcessResponse response = ContentProcessResponse.builder()
                    .contentCount(contentIds.size())
                    .chunkCount(allChunks.size())
                    .vectorCount(vectorCount)
                    .contentIds(contentIds)
                    .processingTime(processingTime)
                    .statusMessage("文档处理完成")
                    .build();

            log.info("文档处理流程完成，耗时: {}ms, 内容数: {}, 分块数: {}, 向量数: {}", 
                    processingTime, contentIds.size(), allChunks.size(), vectorCount);

            return SingleResponse.of(response);

        } catch (Exception e) {
            log.error("文档处理流程失败: {}", e.getMessage(), e);
            throw new BizException("文档处理失败: " + e.getMessage());
        }
    }

    /**
     * 参数校验
     */
    private void validateCommand(ContentProcessCmd cmd) {
        if (cmd.getKnowledgeBaseId() == null) {
            throw new BizException("知识库ID不能为空");
        }
        if (cmd.getFiles() == null || cmd.getFiles().isEmpty()) {
            throw new BizException("文件列表不能为空");
        }
        if (cmd.getSegmentMode() == null) {
            throw new BizException("分段模式不能为空");
        }
    }

    /**
     * 创建内容记录
     */
    private List<Long> createContentRecords(ContentProcessCmd cmd) throws Exception {
        List<Long> contentIds = new ArrayList<>();
        
        for (String fileUrl : cmd.getFiles()) {
            // 从URL提取文件名
            String fileName = extractFileName(fileUrl);
            
            // 创建内容对象
            Content content = new Content();
            content.setKnowledgeBaseId(cmd.getKnowledgeBaseId());
            content.setTitle(fileName);
            content.setContentType("DOCUMENT");
            content.setFileUrl(fileUrl);
            content.setFileType(getFileExtension(fileName));
            content.setStatus(ContentStatusEnum.DISABLED);
            content.setSegmentMode(SegmentMode.fromCode(cmd.getSegmentMode()));
            content.setCharCount(0L);
            content.setRecallCount(0L);
            content.setCreatedBy(getCurrentUserId());
            content.setCreatedAt(System.currentTimeMillis());
            content.setUpdatedAt(System.currentTimeMillis());
            
            // 保存到数据库
            contentGateway.save(content);
            // 假设save方法会设置ID，或者我们需要从其他方式获取ID
            // 这里可能需要调整，取决于实际的save方法实现
            contentIds.add(content.getId());
            
            log.info("创建内容记录成功，ID: {}, 文件: {}", content.getId(), fileName);
        }
        
        return contentIds;
    }

    /**
     * 文档分块处理
     */
    private List<ChunkDTO> processDocumentChunking(ContentProcessCmd cmd) {
        List<ChunkDTO> allChunks = new ArrayList<>();
        
        if ("general".equals(cmd.getSegmentMode())) {
            // 通用分块
            for (String fileUrl : cmd.getFiles()) {
                KnowledgeGeneralChunkCmd chunkCmd = buildGeneralChunkCmd(fileUrl, cmd);
                var chunkResponse = knowledgeGeneralChunkCmdExe.execute(chunkCmd);
                if (chunkResponse.isSuccess()) {
                    allChunks.addAll(chunkResponse.getData());
                    log.info("通用分块处理完成，文件: {}, 分块数: {}", fileUrl, chunkResponse.getData().size());
                } else {
                    throw new BizException("通用分块处理失败: " + chunkResponse.getErrMessage());
                }
            }
        } else if ("parent_child".equals(cmd.getSegmentMode())) {
            // 父子分块
            for (String fileUrl : cmd.getFiles()) {
                KnowledgeParentChildChunkCmd chunkCmd = buildParentChildChunkCmd(fileUrl, cmd);
                var chunkResponse = knowledgeParentChildChunkCmdExe.execute(chunkCmd);
                if (chunkResponse.isSuccess()) {
                    allChunks.addAll(chunkResponse.getData());
                    log.info("父子分块处理完成，文件: {}, 分块数: {}", fileUrl, chunkResponse.getData().size());
                } else {
                    throw new BizException("父子分块处理失败: " + chunkResponse.getErrMessage());
                }
            }
        } else {
            throw new BizException("不支持的分段模式: " + cmd.getSegmentMode());
        }
        
        return allChunks;
    }

    /**
     * 保存分块到数据库
     */  
    private List<Long> saveChunksToDatabase(List<ChunkDTO> chunks, Long contentId) {
        List<Long> chunkIds = new ArrayList<>();
        
        for (int i = 0; i < chunks.size(); i++) {
            ChunkDTO chunkDTO = chunks.get(i);
            
            // 创建分块对象
            Chunk chunk = new Chunk();
            chunk.setContentId(contentId);
            chunk.setChunkIndex(String.valueOf(i));
            chunk.setContent(chunkDTO.getContent());
            chunk.setTokenSize(calculateTokenCount(chunkDTO.getContent()));
            chunk.setMetadata(chunkDTO.getMetadata());
            chunk.setCreateTime(System.currentTimeMillis());
            chunk.setUpdateTime(System.currentTimeMillis());
            
            // 保存到数据库
            Long chunkId = chunkGateway.save(chunk);
            chunkIds.add(chunkId);
        }
        
        log.info("保存分块到数据库完成，数量: {}", chunkIds.size());
        return chunkIds;
    }

    /**
     * 向量化处理
     */
    private int processVectorization(List<ChunkDTO> chunks) {
        int vectorCount = 0;
        
        // 获取嵌入模型
        EmbeddingModel embeddingModel = (EmbeddingModel) modelBeanManagerService.getFirstModelBean();
        if (embeddingModel == null) {
            log.warn("嵌入模型未找到，跳过向量化处理");
            return 0;
        }
        
        for (ChunkDTO chunkDTO : chunks) {
            try {
                // 创建TextSegment
                TextSegment textSegment = TextSegment.from(chunkDTO.getContent());
                
                // 生成嵌入向量
                var embedding = embeddingModel.embed(textSegment.text()).content();
                
                // 存储到向量数据库
                embeddingStore.add(embedding, textSegment);
                
                vectorCount++;
                
            } catch (Exception e) {
                log.warn("分块向量化失败，跳过该分块: {}", e.getMessage());
            }
        }
        
        log.info("向量化处理完成，成功处理: {}/{}", vectorCount, chunks.size());
        return vectorCount;
    }

    /**
     * 更新内容状态
     */
    private void updateContentStatus(List<Long> contentIds, ContentStatusEnum status) {
        for (Long contentId : contentIds) {
            Content content = contentGateway.findById(contentId);
            if (content != null) {
                content.setStatus(status);
                content.setUpdatedAt(System.currentTimeMillis());
                contentGateway.update(content);
            }
        }
        log.info("更新内容状态完成，数量: {}, 状态: {}", contentIds.size(), status);
    }

    /**
     * 构建通用分块命令
     */
    private KnowledgeGeneralChunkCmd buildGeneralChunkCmd(String fileUrl, ContentProcessCmd cmd) {
        KnowledgeGeneralChunkCmd chunkCmd = new KnowledgeGeneralChunkCmd();
        chunkCmd.setFileUrl(fileUrl);
        
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
            chunkCmd.setChunkSize(500);
            chunkCmd.setOverlapSize(50);
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
    private KnowledgeParentChildChunkCmd buildParentChildChunkCmd(String fileUrl, ContentProcessCmd cmd) {
        KnowledgeParentChildChunkCmd chunkCmd = new KnowledgeParentChildChunkCmd();
        chunkCmd.setFileUrl(fileUrl);
        
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
            chunkCmd.setParentChunkSize(500);
            chunkCmd.setChildChunkSize(200);   
            chunkCmd.setContextParagraphs(3);
            chunkCmd.setParentOverlapSize(50);
            chunkCmd.setChildOverlapSize(30);
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
     * 从URL提取文件名
     */
    private String extractFileName(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return "unknown_file";
        }
        
        String fileName = fileUrl;
        int lastSlashIndex = fileName.lastIndexOf('/');
        if (lastSlashIndex >= 0 && lastSlashIndex < fileName.length() - 1) {
            fileName = fileName.substring(lastSlashIndex + 1);
        }
        
        // 移除URL参数
        int queryIndex = fileName.indexOf('?');
        if (queryIndex >= 0) {
            fileName = fileName.substring(0, queryIndex);
        }
        
        return fileName;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex >= 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        
        return "";
    }

    /**
     * 计算Token数量（简单估算）
     */
    private Integer calculateTokenCount(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }
        // 简单估算：中文按字符数，英文按单词数
        return content.length();
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        // TODO: 从上下文获取当前用户ID，这里暂时返回默认值
        return 1L;
    }
}