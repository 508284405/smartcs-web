package com.leyue.smartcs.knowledge.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.knowledge.gateway.DocumentGateway;
import com.leyue.smartcs.domain.knowledge.model.Document;
import com.leyue.smartcs.dto.knowledge.DocAddCmd;
import com.leyue.smartcs.dto.knowledge.DocDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 文档创建命令执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DocAddCmdExe {
    
    private final DocumentGateway documentGateway;
    
    /**
     * 执行文档创建命令
     * @param cmd 文档创建命令
     * @return 创建后的文档
     */
    public SingleResponse<DocDTO> execute(DocAddCmd cmd) {
        log.info("执行文档创建命令: {}", cmd);
        
        // 参数校验
        if (cmd.getTitle() == null || cmd.getTitle().trim().isEmpty()) {
            throw new BizException("文档标题不能为空");
        }
        
        // 至少需要一个来源：文件名或OSS地址
        if ((cmd.getFileName() == null || cmd.getFileName().trim().isEmpty()) && 
            (cmd.getOssUrl() == null || cmd.getOssUrl().trim().isEmpty())) {
            throw new BizException("文件名或OSS地址至少需要一个");
        }
        
        // 创建文档实体
        Document document = Document.builder()
                .title(cmd.getTitle())
                .ossUrl(cmd.getOssUrl())
                .fileType(cmd.getFileType())
                .version(cmd.getVersion())
                .build();
        
        // 保存并转换结果
        Document savedDoc = documentGateway.save(document);
        DocDTO docDTO = convertToDTO(savedDoc);
        
        log.info("文档创建成功，ID: {}", savedDoc.getId());
        return SingleResponse.of(docDTO);
    }
    
    /**
     * 转换为DTO
     * @param document 文档实体
     * @return 文档DTO
     */
    private DocDTO convertToDTO(Document document) {
        if (document == null) {
            return null;
        }
        
        DocDTO dto = new DocDTO();
        dto.setId(document.getId());
        dto.setTitle(document.getTitle());
        dto.setOssUrl(document.getOssUrl());
        dto.setFileType(document.getFileType());
        dto.setVersion(document.getVersion());
        dto.setCreatedBy(document.getCreatedBy());
        dto.setCreatedAt(document.getCreatedAt());
        dto.setUpdatedAt(document.getUpdatedAt());
        
        return dto;
    }
} 