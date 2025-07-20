package com.leyue.smartcs.knowledge.executor.command;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.config.ModelBeanManagerService;
import com.leyue.smartcs.dto.knowledge.ChunkDTO;
import com.leyue.smartcs.dto.knowledge.KnowledgeGeneralChunkCmd;
import com.leyue.smartcs.knowledge.util.TextPreprocessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用文档分块命令执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KnowledgeGeneralChunkCmdExe {

    private final TextPreprocessor textPreprocessor;
    private final ModelBeanManagerService modelBeanManagerService;

    /**
     * 执行通用文档分块
     * 
     * @param cmd 分块命令
     * @return 分块结果
     */
    public MultiResponse<ChunkDTO> execute(KnowledgeGeneralChunkCmd cmd) {
        try {
            // 将OSS URL转换为Resource对象
            Resource resource = new UrlResource(cmd.getFileUrl());

            // 创建LangChain4j文档 - 简化处理，直接读取文本内容
            String content = new String(resource.getInputStream().readAllBytes());
            Document document = Document.from(content);
            
            // 获取聊天模型
            ChatLanguageModel chatModel = (ChatLanguageModel) modelBeanManagerService.getFirstModelBean();

            // 文本预处理
            List<Document> preprocessedTexts = textPreprocessor.preprocessText(
                    List.of(document),
                    cmd.getRemoveAllUrls(),
                    cmd.getUseQASegmentation(),
                    cmd.getQaLanguage(),
                    chatModel);

            List<ChunkDTO> allChunks = new ArrayList<>();

            // 使用LangChain4j的文档分割器进行分块
            var splitter = DocumentSplitters.recursive(cmd.getChunkSize(), cmd.getOverlapSize());
            List<TextSegment> segments = splitter.splitAll(preprocessedTexts);
            
            // 转换为ChunkDTO
            for (int j = 0; j < segments.size(); j++) {
                TextSegment segment = segments.get(j);
                ChunkDTO chunkDTO = convertToChunkDTO(segment, j);
                allChunks.add(chunkDTO);
            }

            return MultiResponse.of(allChunks);

        } catch (Exception e) {
            log.error("通用文档分块失败: {}", e.getMessage(), e);
            throw new BizException("通用文档分块失败: " + e.getMessage());
        }
    }

    /**
     * 转换为ChunkDTO
     */
    private ChunkDTO convertToChunkDTO(TextSegment segment, int index) {
        ChunkDTO chunkDTO = new ChunkDTO();
        chunkDTO.setChunkIndex(String.valueOf(index));
        chunkDTO.setContent(segment.text());
        chunkDTO.setMetadata(segment.metadata() != null ? segment.metadata().toString() : "{}");
        return chunkDTO;
    }
}