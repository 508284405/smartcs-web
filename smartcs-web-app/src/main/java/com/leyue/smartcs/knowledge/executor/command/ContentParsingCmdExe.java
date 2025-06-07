package com.leyue.smartcs.knowledge.executor.command;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.knowledge.Chunk;
import com.leyue.smartcs.domain.knowledge.Content;
import com.leyue.smartcs.domain.knowledge.enums.ContentStatusEnum;
import com.leyue.smartcs.domain.knowledge.gateway.ChunkGateway;
import com.leyue.smartcs.domain.knowledge.gateway.ContentGateway;
import com.leyue.smartcs.domain.utils.OssFileDownloader;

import com.leyue.smartcs.knowledge.parser.DocumentParser;
import com.leyue.smartcs.knowledge.parser.factory.DocumentParserFactory;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;
import java.util.stream.Collectors;

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
    private DocumentParserFactory documentParserFactory;

    @Autowired
    private OssFileDownloader ossFileDownloader;

    @Autowired
    private VectorStore vectorStore;

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

            // 更新解析结果
            content.setStatus(ContentStatusEnum.PARSED);
            contentGateway.updateById(content);

            // 将chunk保存到数据库
            List<Chunk> chunks = documents.stream().map(document -> {
                Chunk chunk = new Chunk();
                chunk.setContentId(contentId);
                chunk.setText(document.getText());
                chunk.setStrategyName(content.getStrategyName());
                chunk.setMetadata(document.getMetadata().toString());
                chunk.setCreatedBy(content.getCreatedBy());
                chunk.setCreatedAt(System.currentTimeMillis());
                chunk.setUpdatedAt(System.currentTimeMillis());
                return chunk;
            }).collect(Collectors.toList());
            chunkGateway.saveBatch(contentId, chunks, content.getStrategyName());

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

    /**
     * 执行实际的解析逻辑
     */
    private String performParsing(Content content) {
        log.info("开始解析内容, 类型: {}, 文件地址: {}", content.getContentType(), content.getFileUrl());

        String fileType = content.getFileType();
        String fileUrl = content.getFileUrl();

        if (!StringUtils.hasText(fileUrl)) {
            throw new IllegalStateException("文件地址为空，无法解析");
        }

        if (!StringUtils.hasText(fileType)) {
            throw new IllegalStateException("文件类型为空，无法解析");
        }

        try {
            // 检查是否支持该文件类型
            if (!documentParserFactory.isSupported(fileType)) {
                log.warn("不支持的文件类型: {}", fileType);
                return "不支持的文件类型: " + fileType;
            }

            // 获取对应的解析器并执行解析
            DocumentParser parser = documentParserFactory.getParser(fileType);
            return parser.parseContent(fileUrl);

        } catch (Exception e) {
            log.error("解析内容失败, 类型: {}, 文件: {}", fileType, fileUrl, e);
            throw new RuntimeException("解析内容失败: " + e.getMessage(), e);
        }
    }
}