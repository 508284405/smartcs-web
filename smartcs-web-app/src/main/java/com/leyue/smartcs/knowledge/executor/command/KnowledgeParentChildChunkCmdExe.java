package com.leyue.smartcs.knowledge.executor.command;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.dto.knowledge.ChunkDTO;
import com.leyue.smartcs.dto.knowledge.KnowledgeParentChildChunkCmd;
import com.leyue.smartcs.knowledge.util.TextPreprocessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
        log.info("开始执行父子文档分块，内容长度: {}", cmd.getFileUrl());

        try {
            // 文本预处理 - 移除URL和邮箱（如果需要）
            Resource resource = new UrlResource(cmd.getFileUrl());
            TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(resource);
            List<Document> documents = tikaDocumentReader.read();

            // 第一步：创建父块分割器
            TokenTextSplitter parentSplitter = new TokenTextSplitter(
                    cmd.getParentChunkSize(),
                    cmd.getParentOverlapSize(),
                    cmd.getMinChunkSize(),
                    cmd.getMaxChunkSize(),
                    cmd.getKeepSeparator()
            );

            // 第二步：创建子块分割器
            TokenTextSplitter childSplitter = new TokenTextSplitter(
                    cmd.getChildChunkSize(),
                    cmd.getChildOverlapSize(),
                    cmd.getMinChunkSize(),
                    cmd.getMaxChunkSize(),
                    cmd.getKeepSeparator()
            );

            // 执行父块分割
            List<Document> parentChunks = parentSplitter.apply(documents);

            // 为每个父块执行子块分割，并保持父子关系
            List<ChunkDTO> allChunks = new ArrayList<>();
            
            for (int parentIndex = 0; parentIndex < parentChunks.size(); parentIndex++) {
                Document parentChunk = parentChunks.get(parentIndex);
                
                // 获取上下文段落
                String contextText = getContextText(parentChunks, parentIndex, cmd.getContextParagraphs());
                
                // 执行子块分割
                List<Document> childChunks = childSplitter.apply(List.of(parentChunk));
                
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
     * 获取上下文文本（父块用作上下文的段落）
     */
    private String getContextText(List<Document> parentChunks, int currentIndex, int contextParagraphs) {
        StringBuilder contextBuilder = new StringBuilder();
        
        // 获取前面的段落作为上下文
        int startIndex = Math.max(0, currentIndex - contextParagraphs);
        int endIndex = Math.min(parentChunks.size(), currentIndex + contextParagraphs + 1);
        
        for (int i = startIndex; i < endIndex; i++) {
            if (i != currentIndex) {
                contextBuilder.append(parentChunks.get(i).getText()).append("\n\n");
            }
        }
        
        return contextBuilder.toString().trim();
    }

    /**
     * 将Spring AI的Document转换为ChunkDTO，包含父子关系和上下文
     */
    private ChunkDTO convertToChunkDTO(Document document, int parentIndex, int childIndex, String contextText) {
        String chunkIndex = String.format("parent_%d_child_%d", parentIndex, childIndex);
        
        // 构建元数据，包含父子关系和上下文
        String metadata = String.format(
                "{\"parentIndex\":%d,\"childIndex\":%d,\"context\":\"%s\"}",
                parentIndex, childIndex, contextText.replace("\"", "\\\"")
        );
        
        return ChunkDTO.builder()
                .chunkIndex(chunkIndex)
                .content(document.getText())
                .tokenSize(estimateTokenSize(document.getText()))
                .metadata(metadata)
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis())
                .build();
    }

    /**
     * 估算token数量（简单估算：中文字符 * 1.5）
     */
    private Integer estimateTokenSize(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }
        return (int) (content.length() * 1.5);
    }
} 