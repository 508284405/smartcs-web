package com.leyue.smartcs.knowledge.executor.command;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.config.ModelBeanManagerService;
import com.leyue.smartcs.domain.knowledge.Content;
import com.leyue.smartcs.domain.knowledge.enums.ContentStatusEnum;
import com.leyue.smartcs.domain.knowledge.gateway.ChunkGateway;
import com.leyue.smartcs.domain.knowledge.gateway.ContentGateway;
import com.leyue.smartcs.domain.utils.OssFileDownloader;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@Slf4j
public class ContentParsingCmdExe {

    @Autowired
    private ContentGateway contentGateway;

    @Autowired
    private ChunkGateway chunkGateway;

    @Autowired
    private OssFileDownloader ossFileDownloader;

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    @Autowired
    private ModelBeanManagerService modelBeanManagerService;

    /**
     * 执行内容解析
     */
    @Transactional(rollbackOn = Exception.class)
    public Response execute(Long contentId) {
        log.info("执行内容解析, 内容ID: {}", contentId);

        try {
            // 查询内容
            Content content = contentGateway.findById(contentId);
            if (content == null) {
                log.warn("内容不存在, ID: {}", contentId);
                throw new BizException("内容不存在");
            }

            // 状态校验,只要状态不为空就可以重复解析
            if (content.getStatus() == null) {
                throw new BizException("内容状态为空,不能重复解析");
            }

            // 将OSS URL转换为Resource对象
            Resource resource = convertOssUrlToResource(content.getFileUrl());

            // 创建LangChain4j文档
            String contentText = new String(resource.getInputStream().readAllBytes());
            Document document = Document.from(contentText);

            // 获取嵌入模型
            EmbeddingModel embeddingModel = (EmbeddingModel) modelBeanManagerService.getFirstModelBean();
            if (embeddingModel == null) {
                throw new BizException("嵌入模型未找到");
            }

            // 生成嵌入向量并创建TextSegment
            dev.langchain4j.data.embedding.Embedding embedding = embeddingModel.embed(document.text()).content();
            TextSegment textSegment = TextSegment.from(document.text(), document.metadata());

            // 存储到向量数据库
            embeddingStore.add(embedding, textSegment);

            // 更新内容状态为已解析
            content.setStatus(ContentStatusEnum.PARSED);
            contentGateway.update(content);

            log.info("内容解析完成, 内容ID: {}", contentId);
            return Response.buildSuccess();

        } catch (Exception e) {
            log.error("内容解析失败, 内容ID: {}, 错误: {}", contentId, e.getMessage(), e);
            throw new BizException("内容解析失败: " + e.getMessage());
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