package com.leyue.smartcs.knowledge.executor.command;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.dto.knowledge.ChunkDTO;
import com.leyue.smartcs.dto.knowledge.KnowledgeParentChildChunkCmd;
import com.leyue.smartcs.knowledge.util.TextPreprocessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.segment.TextSegment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 父子文档分块命令执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KnowledgeParentChildChunkCmdExe {

    private final TextPreprocessor textPreprocessor;

    /**
     * 执行父子文档分块
     * 
     * @param cmd 分块命令
     * @return 分块结果
     */
    public MultiResponse<ChunkDTO> execute(KnowledgeParentChildChunkCmd cmd) {
        if (cmd == null || cmd.getFileUrl() == null) {
            throw new BizException("CHUNK_PARENT_CHILD_FAILED", "文件地址不能为空");
        }
        log.info("开始执行父子文档分块，文件: {}", cmd.getFileUrl());

        try {
            // 读取文档
            Resource resource = new UrlResource(cmd.getFileUrl());
            String content = readResourceContent(resource);
            if (Boolean.TRUE.equals(cmd.getRemoveAllUrls())) {
                content = textPreprocessor.removeUrlsAndEmails(content);
            }
            Document document = Document.from(content);

            // 第一步：创建父块分割器
            DocumentSplitter parentSplitter = DocumentSplitters.recursive(
                    cmd.getParentChunkSize(),
                    cmd.getParentOverlapSize()
            );

            // 第二步：创建子块分割器
            DocumentSplitter childSplitter = DocumentSplitters.recursive(
                    cmd.getChildChunkSize(),
                    cmd.getChildOverlapSize()
            );

            // 执行父块分割
            List<TextSegment> parentSegments = parentSplitter.split(document);
            List<Document> parentChunks = parentSegments.stream()
                    .map(segment -> Document.from(segment.text(), segment.metadata()))
                    .collect(Collectors.toList());
            log.info("父块数量: {}", parentChunks.size());

            // 为每个父块执行子块分割，并保持父子关系
            List<ChunkDTO> allChunks = new ArrayList<>();
            
            for (int parentIndex = 0; parentIndex < parentChunks.size(); parentIndex++) {
                Document parentChunk = parentChunks.get(parentIndex);
                
                // 获取上下文段落
                int ctxPara = cmd.getContextParagraphs() == null ? 0 : cmd.getContextParagraphs();
                String contextText = getContextText(parentChunks, parentIndex, ctxPara);
                
                // 执行子块分割
                List<TextSegment> childSegments = childSplitter.split(parentChunk);
                List<Document> childChunks = childSegments.stream()
                        .map(segment -> Document.from(segment.text(), segment.metadata()))
                        .collect(Collectors.toList());
                log.debug("父块 {} 生成子块 {} 个", parentIndex, childChunks.size());
                
                // 转换子块为ChunkDTO，并添加父块上下文
                for (int childIndex = 0; childIndex < childChunks.size(); childIndex++) {
                    Document childChunk = childChunks.get(childIndex);
                    ChunkDTO chunkDTO = convertToChunkDTO(childChunk, parentIndex, childIndex, contextText);
                    allChunks.add(chunkDTO);
                }
            }

            log.info("父子文档分块完成，共生成 {} 个分块", allChunks.size());

            return MultiResponse.of(allChunks);

        } catch (Exception e) {
            log.error("父子文档分块失败", e);
            throw new BizException("CHUNK_PARENT_CHILD_FAILED", "父子文档分块失败: " + e.getMessage());
        }
    }

    /**
     * 读取资源内容
     */
    private String readResourceContent(Resource resource) throws IOException {
        try (var in = resource.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * 获取上下文文本（父块用作上下文的段落）
     */
    private String getContextText(List<Document> parentChunks, int currentIndex, int contextParagraphs) {
        StringBuilder contextBuilder = new StringBuilder();
        if (parentChunks == null || parentChunks.isEmpty()) {
            return "";
        }

        int paragraphs = contextParagraphs <= 0 ? 0 : contextParagraphs;

        // 获取前面的段落作为上下文
        int startIndex = Math.max(0, currentIndex - paragraphs);
        int endIndex = Math.min(parentChunks.size(), currentIndex + paragraphs + 1);
        
        for (int i = startIndex; i < endIndex; i++) {
            if (i != currentIndex) {
                contextBuilder.append(parentChunks.get(i).text()).append("\n\n");
            }
        }
        
        return contextBuilder.toString().trim();
    }

    /**
     * 将LangChain4j的Document转换为ChunkDTO，包含父子关系和上下文
     */
    private ChunkDTO convertToChunkDTO(Document document, int parentIndex, int childIndex, String contextText) {
        String chunkIndex = String.format("parent_%d_child_%d", parentIndex, childIndex);
        
        // 构建元数据，包含父子关系和上下文
        String metadata = String.format(
                "{\"parentIndex\":%d,\"childIndex\":%d,\"context\":\"%s\"}",
                parentIndex, childIndex, contextText.replace("\"", "\\\"")
        );
        
        ChunkDTO chunkDTO = new ChunkDTO();
        chunkDTO.setChunkIndex(chunkIndex);
        chunkDTO.setContent(document.text());
        chunkDTO.setMetadata(metadata);
        return chunkDTO;
    }
    
    /**
     * 从URL中提取文件名
     */
    private String extractFileName(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return "unknown_file";
        }
        
        // 从URL中提取文件名
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

} 