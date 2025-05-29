package com.leyue.smartcs.knowledge.executor.command;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.knowledge.Content;
import com.leyue.smartcs.domain.knowledge.enums.ContentStatusEnum;
import com.leyue.smartcs.domain.knowledge.enums.StrategyNameEnum;
import com.leyue.smartcs.domain.knowledge.gateway.ContentGateway;

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
     * TODO: 根据不同的内容类型实现具体的解析逻辑
     */
    private String performParsing(Content content) {
        log.info("开始解析内容, 类型: {}, 文件地址: {}", content.getContentType(), content.getFileUrl());

        String fileType = content.getFileType();
        String fileUrl = content.getFileUrl();

        if (!StringUtils.hasText(fileUrl)) {
            throw new IllegalStateException("文件地址为空，无法解析");
        }

        try {
            // 根据内容类型执行不同的解析逻辑
            switch (fileType.toLowerCase()) {
                case "pdf":
                    return parsePdfContent(fileUrl);
                case "doc":
                case "docx":
                    return parseDocContent(fileUrl);
                case "txt":
                    return parseTxtContent(fileUrl);
                case "md":
                    return parseMarkdownContent(fileUrl);
                default:
                    log.warn("不支持的内容类型: {}", fileType);
                    return "不支持的内容类型，无法解析文本";
            }
        } catch (Exception e) {
            log.error("解析内容失败, 类型: {}, 文件: {}", fileType, fileUrl, e);
            throw new RuntimeException("解析内容失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析PDF内容
     */
    private String parsePdfContent(String fileUrl) {
        // TODO: 实现PDF解析逻辑
        log.info("解析PDF文件: {}", fileUrl);
        return "PDF文件解析后的文本内容"; // 临时返回
    }

    /**
     * 解析DOC/DOCX内容
     */
    private String parseDocContent(String fileUrl) {
        // TODO: 实现DOC/DOCX解析逻辑
        log.info("解析DOC文件: {}", fileUrl);
        return "DOC文件解析后的文本内容"; // 临时返回
    }

    /**
     * 解析TXT内容
     */
    private String parseTxtContent(String fileUrl) {
        // TODO: 实现TXT解析逻辑
        log.info("解析TXT文件: {}", fileUrl);
        return "TXT文件解析后的文本内容"; // 临时返回
    }

    /**
     * 解析Markdown内容
     */
    private String parseMarkdownContent(String fileUrl) {
        // TODO: 实现Markdown解析逻辑
        log.info("解析Markdown文件: {}", fileUrl);
        return "Markdown文件解析后的文本内容"; // 临时返回
    }
}