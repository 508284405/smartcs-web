package com.leyue.smartcs.knowledge.executor.command;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.config.context.UserContext;
import com.leyue.smartcs.domain.knowledge.Content;
import com.leyue.smartcs.domain.knowledge.enums.ContentStatusEnum;
import com.leyue.smartcs.domain.knowledge.gateway.ContentGateway;
import com.leyue.smartcs.dto.knowledge.ContentCreateCmd;
import com.leyue.smartcs.dto.knowledge.ContentDTO;
import com.leyue.smartcs.knowledge.convertor.ContentConvertor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 内容创建命令执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ContentCreateCmdExe {

    private final ContentGateway contentGateway;

    private final ContentConvertor contentConvertor;

    /**
     * 执行内容创建命令
     * @param cmd 创建命令
     * @return 创建的内容
     */
    public SingleResponse<ContentDTO> execute(ContentCreateCmd cmd) {
        log.info("执行内容创建命令: {}", cmd);
        
        // 参数校验
        validateCmd(cmd);
        
        // 构建内容领域对象
        Long currentUserId = UserContext.getCurrentUser().getId();
        Long currentTime = System.currentTimeMillis();
        
        Content content = Content.builder()
                .knowledgeBaseId(cmd.getKnowledgeBaseId())
                .title(cmd.getTitle())
                .contentType(cmd.getContentType())
                .fileType(cmd.getFileType())
                .fileUrl(cmd.getOssUrl())
                .status(ContentStatusEnum.UPLOADED)
                .createdBy(currentUserId)
                .createdAt(currentTime)
                .updatedAt(currentTime)
                .build();
        
        // 保存内容
        contentGateway.save(content);
        
        // 转换为DTO
        ContentDTO contentDTO = contentConvertor.toDTO(content);
        
        log.info("内容创建成功，ID: {}", content.getId());
        return SingleResponse.of(contentDTO);
    }
    
    /**
     * 参数校验
     * @param cmd 创建命令
     */
    private void validateCmd(ContentCreateCmd cmd) {
        if (cmd.getKnowledgeBaseId() == null) {
            throw new BizException("知识库ID不能为空");
        }
        
        if (cmd.getTitle() == null || cmd.getTitle().trim().isEmpty()) {
            throw new BizException("内容标题不能为空");
        }
        
        if (cmd.getTitle().length() > 256) {
            throw new BizException("内容标题长度不能超过256个字符");
        }
        
        if (cmd.getContentType() == null || cmd.getContentType().trim().isEmpty()) {
            throw new BizException("内容类型不能为空");
        }
        
        if (!isValidContentType(cmd.getContentType())) {
            throw new BizException("不支持的内容类型: " + cmd.getContentType());
        }
        
        if (cmd.getOssUrl() == null || cmd.getOssUrl().trim().isEmpty()) {
            throw new BizException("文件地址不能为空");
        }
    }
    
    /**
     * 检查内容类型是否有效
     * @param contentType 内容类型
     * @return 是否有效
     */
    private boolean isValidContentType(String contentType) {
        return "document".equals(contentType) || 
               "audio".equals(contentType) || 
               "video".equals(contentType);
    }
}