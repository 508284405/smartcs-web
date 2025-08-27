package com.leyue.smartcs.knowledge.executor.command;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.knowledge.Content;
import com.leyue.smartcs.domain.knowledge.enums.ContentStatusEnum;
import com.leyue.smartcs.domain.knowledge.gateway.ChunkGateway;
import com.leyue.smartcs.domain.knowledge.gateway.ContentGateway;
import com.leyue.smartcs.utils.OssFileDownloader;
import com.leyue.smartcs.dto.errorcode.ModelErrorCode;
import com.leyue.smartcs.model.ai.DynamicModelManager;
import com.leyue.smartcs.model.service.DefaultModelService;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.MalformedURLException;

/**
 * 内容解析执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ContentParsingCmdExe {

    private final ContentGateway contentGateway;
    private final ChunkGateway chunkGateway;
    private final OssFileDownloader ossFileDownloader;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final DynamicModelManager dynamicModelManager;
    private final DefaultModelService defaultModelService;

    /**
     * 执行内容解析（使用默认嵌入模型）
     */
    @Transactional(rollbackOn = Exception.class)
    public Response execute(Long contentId) {
        // 使用默认嵌入模型服务获取模型ID
        Long defaultModelId = defaultModelService.getDefaultEmbeddingModelId();
        return executeWithModelId(contentId, defaultModelId);
    }

    /**
     * 执行内容解析
     */
    @Transactional(rollbackOn = Exception.class)
    public Response executeWithModelId(Long contentId, Long modelId) {
        log.info("执行内容解析, 内容ID: {}", contentId);

        try {
            // 查询内容
            Content content = contentGateway.findById(contentId);
            if (content == null) {
                log.warn("内容不存在, ID: {}", contentId);
                throw new BizException(ModelErrorCode.CONTENT_NOT_FOUND.getErrCode(), 
                        ModelErrorCode.CONTENT_NOT_FOUND.getErrDesc());
            }

            // 状态校验,只要状态不为空就可以重复解析
            if (content.getStatus() == null) {
                throw new BizException(ModelErrorCode.CONTENT_STATUS_INVALID.getErrCode(), 
                        ModelErrorCode.CONTENT_STATUS_INVALID.getErrDesc());
            }

            // 将OSS URL转换为Resource对象
            Resource resource = convertOssUrlToResource(content.getFileUrl());

            // 创建LangChain4j文档
            String contentText = new String(resource.getInputStream().readAllBytes());
            Document document = Document.from(contentText);

            // 获取嵌入模型
            EmbeddingModel embeddingModel = dynamicModelManager.getEmbeddingModel(modelId);

            // 生成嵌入向量并创建TextSegment
            dev.langchain4j.data.embedding.Embedding embedding = embeddingModel.embed(document.text()).content();
            TextSegment textSegment = TextSegment.from(document.text(), document.metadata());

            // 存储到向量数据库
            embeddingStore.add(embedding, textSegment);

            // 更新内容状态为启用
            content.setStatus(ContentStatusEnum.ENABLED);
            contentGateway.update(content);

            log.info("内容解析完成, 内容ID: {}", contentId);
            return Response.buildSuccess();

        } catch (BizException e) {
            // 重新抛出业务异常
            throw e;
        } catch (Exception e) {
            log.error("内容解析失败, 内容ID: {}, 错误: {}", contentId, e.getMessage(), e);
            throw new BizException(ModelErrorCode.CONTENT_PARSING_FAILED.getErrCode(), 
                    ModelErrorCode.CONTENT_PARSING_FAILED.getErrDesc() + ": " + e.getMessage());
        }
    }

    /**
     * 将OSS URL转换为Resource对象
     */
    private Resource convertOssUrlToResource(String fileUrl) throws MalformedURLException {
        if (fileUrl.startsWith("http")) {
            return new UrlResource(fileUrl);
        } else {
            return new FileSystemResource(new File(fileUrl));
        }
    }

}