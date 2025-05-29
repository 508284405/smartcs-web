package com.leyue.smartcs.knowledge.executor.command;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.knowledge.Content;
import com.leyue.smartcs.domain.knowledge.enums.ContentStatusEnum;
import com.leyue.smartcs.domain.knowledge.gateway.ContentGateway;

import com.leyue.smartcs.knowledge.parser.DocumentParser;
import com.leyue.smartcs.knowledge.parser.factory.DocumentParserFactory;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 内容解析执行器
 */
@Component
@Slf4j
public class ContentParsingCmdExe {

    @Autowired
    private ContentGateway contentGateway;

    @Autowired
    private DocumentParserFactory documentParserFactory;

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

            // 执行解析逻辑
            String extractedText = performParsing(content);

            // 更新解析结果
            content.setTextExtracted(extractedText);
            content.setStatus(ContentStatusEnum.PARSED);
            contentGateway.save(content);

            log.info("内容解析完成, ID: {}, 提取文本长度: {}", contentId,
                    extractedText != null ? extractedText.length() : 0);

            return Response.buildSuccess();

        } catch (Exception e) {
            log.error("内容解析失败", e);
            throw new BizException("内容解析失败: " + e.getMessage());
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