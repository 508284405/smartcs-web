package com.leyue.smartcs.knowledge.executor.command;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.alibaba.fastjson2.JSON;
import com.leyue.smartcs.config.ModelBeanManagerService;
import com.leyue.smartcs.domain.knowledge.Chunk;
import com.leyue.smartcs.domain.knowledge.Content;
import com.leyue.smartcs.domain.knowledge.enums.ContentStatusEnum;
import com.leyue.smartcs.domain.knowledge.gateway.ChunkGateway;
import com.leyue.smartcs.domain.knowledge.gateway.ContentGateway;
import com.leyue.smartcs.domain.utils.OssFileDownloader;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

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
    private VectorStore vectorStore;

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

            // 使用TikaDocumentReader解析文档
            TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(resource);
            List<Document> documents = tikaDocumentReader.read();
            // 使用TokenTextSplitter进行分段
            TokenTextSplitter splitter = new TokenTextSplitter(1000, 400, 10, 5000, true);
            documents = splitter.apply(documents);
            // 使用KeywordMetadataEnricher进行关键词提取
            ChatModel chatModel = (ChatModel) modelBeanManagerService.getFirstModelBean();
            if (chatModel != null) {
                KeywordMetadataEnricher enricher = new KeywordMetadataEnricher(chatModel, 5);
                documents = enricher.apply(documents);
            }
            // todo SummaryMetadataEnricher 摘要提取

            // 更新解析结果
            content.setStatus(ContentStatusEnum.PARSED);
            contentGateway.updateById(content);

            // 将chunk保存到数据库
            List<Long> chunkIds = chunkGateway.deleteByContentId(contentId);
            vectorStore.delete(chunkIds.stream().map(String::valueOf).collect(Collectors.toList()));
            List<Chunk> chunks = documents.stream().map(document -> {
                Chunk chunk = new Chunk();
                chunk.setContentId(contentId);
                chunk.setContent(document.getText());
                chunk.setMetadata(JSON.toJSONString(document.getMetadata()));
                chunk.setChunkIndex(document.getId());
                chunk.setCreateTime(System.currentTimeMillis());
                chunk.setUpdateTime(System.currentTimeMillis());
                return chunk;
            }).collect(Collectors.toList());
            chunkGateway.saveBatch(chunks);

            // 向量存储
            vectorStore.add(documents);
            return Response.buildSuccess();

        } catch (Exception e) {
            log.error("内容解析失败", e);
            throw new BizException("内容解析失败: " + e.getMessage());
        }
    }

    /**
     * 将OSS URL转换为Resource对象
     * 
     * @param ossUrl OSS存储地址
     * @return Resource对象
     * @throws Exception 转换异常
     */
    private Resource convertOssUrlToResource(String ossUrl) throws Exception {
        if (!StringUtils.hasText(ossUrl)) {
            throw new IllegalArgumentException("OSS URL不能为空");
        }

        try {
            // 方案1: 直接使用UrlResource (推荐，适用于可直接访问的OSS URL)
            return new UrlResource(ossUrl);

        } catch (MalformedURLException e) {
            log.warn("无法直接创建UrlResource，尝试下载到本地: {}", e.getMessage());

            // 方案2: 下载到本地临时文件再转换为FileSystemResource
            try {
                File tempFile = ossFileDownloader.download(ossUrl);
                return new FileSystemResource(tempFile);
            } catch (Exception downloadException) {
                log.error("下载OSS文件失败: {}", downloadException.getMessage());
                throw new Exception("无法获取OSS文件: " + downloadException.getMessage(), downloadException);
            }
        }
    }
}