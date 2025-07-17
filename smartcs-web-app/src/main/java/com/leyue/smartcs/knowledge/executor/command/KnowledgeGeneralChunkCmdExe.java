package com.leyue.smartcs.knowledge.executor.command;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.config.ModelBeanManagerService;
import com.leyue.smartcs.dto.knowledge.ChunkDTO;
import com.leyue.smartcs.dto.knowledge.KnowledgeGeneralChunkCmd;
import com.leyue.smartcs.knowledge.util.TextPreprocessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
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
            // 使用TikaDocumentReader读取文档
            // 将OSS URL转换为Resource对象
            Resource resource = new UrlResource(cmd.getFileUrl());

            // 使用TikaDocumentReader解析文档
            TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(resource);
            // 只会有一个文档
            List<Document> documents = tikaDocumentReader.read();
            // 获取聊天模型
            ChatModel chatModel = (ChatModel) modelBeanManagerService.getFirstModelBean();

            // 文本预处理
            List<Document> preprocessedTexts = textPreprocessor.preprocessText(
                    documents,
                    cmd.getRemoveAllUrls(),
                    cmd.getUseQASegmentation(),
                    cmd.getQaLanguage(),
                    chatModel);

            List<ChunkDTO> allChunks = new ArrayList<>();

            // 使用TokenTextSplitter进行分块
            TokenTextSplitter splitter = new TokenTextSplitter(
                    cmd.getChunkSize(),
                    cmd.getOverlapSize(),
                    cmd.getMinChunkSize(),
                    cmd.getMaxChunkSize(),
                    cmd.getKeepSeparator());
            List<Document> chunks = splitter.apply(preprocessedTexts);
            // 转换为ChunkDTO
            for (int j = 0; j < chunks.size(); j++) {
                Document doc = chunks.get(j);
                ChunkDTO chunkDTO = convertToChunkDTO(doc, j);
                allChunks.add(chunkDTO);
            }

            log.info("通用文档分块完成，共生成 {} 个分块", allChunks.size());

            return MultiResponse.of(allChunks);

        } catch (Exception e) {
            log.error("通用文档分块失败", e);
            throw new BizException("CHUNK_GENERAL_FAILED", "通用文档分块失败: " + e.getMessage());
        }
    }

    /**
     * 将Spring AI的Document转换为ChunkDTO
     */
    private ChunkDTO convertToChunkDTO(Document document, int chunkIndex) {
        String chunkId = String.format("text_%d_chunk_%d", chunkIndex);
        String metadata = String.format("{\"chunkIndex\":%d}", chunkIndex);

        return ChunkDTO.builder()
                .chunkIndex(chunkId)
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