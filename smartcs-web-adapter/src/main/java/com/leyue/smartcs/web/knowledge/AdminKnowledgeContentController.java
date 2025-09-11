package com.leyue.smartcs.web.knowledge;

import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.api.ContentService;
import com.leyue.smartcs.dto.knowledge.DocumentProcessCmd;
import com.leyue.smartcs.dto.knowledge.DocumentProcessResultDTO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 知识库内容管理Controller
 * 专门处理知识库相关的内容操作
 */
@RestController
@RequestMapping("/api/admin/knowledge/content")
@Slf4j
public class AdminKnowledgeContentController {
    
    @Autowired
    private ContentService contentService;
    
    /**
     * 处理文档 - 完整的文档分块和向量化流程
     * 包括文档解析、分块处理、向量化和存储
     */
    @PostMapping("/process")
    public SingleResponse<DocumentProcessResultDTO> processDocument(@RequestBody @Valid DocumentProcessCmd cmd) {
        log.info("收到文档处理请求，知识库ID: {}, 文档标题: {}", cmd.getKnowledgeBaseId(), cmd.getTitle());
        return contentService.processDocument(cmd);
    }
}