package com.leyue.smartcs.knowledge.executor;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.knowledge.event.DocEmbeddingGenerateEvent;
import com.leyue.smartcs.domain.knowledge.gateway.DocumentGateway;
import com.leyue.smartcs.domain.knowledge.model.Document;
import com.leyue.smartcs.dto.common.SingleClientObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 文档向量生成触发命令执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DocEmbeddingTriggerCmdExe {
    
    private final DocumentGateway documentGateway;
    private final ApplicationEventPublisher eventPublisher;
    
    /**
     * 执行文档向量生成触发命令
     * @param cmd 文档ID
     * @return 处理结果
     */
    public Response execute(SingleClientObject<Long> cmd) {
        log.info("执行文档向量生成触发命令: {}", cmd);
        
        // 参数校验
        if (cmd.getValue() == null) {
            throw new BizException("文档ID不能为空");
        }
        
        Long docId = cmd.getValue();
        
        // 查询文档
        Optional<Document> docOpt = documentGateway.findById(docId);
        if (docOpt.isEmpty()) {
            throw new BizException("文档不存在，ID: " + docId);
        }
        
        Document document = docOpt.get();
        
        // 检查文档是否有OSS URL
        if (!document.canProcess()) {
            return Response.buildFailure("DOC-EMPTY-URL", "文档URL为空，无法处理");
        }
        
        // 发布文档向量生成事件，触发异步处理
        DocEmbeddingGenerateEvent event = DocEmbeddingGenerateEvent.builder()
                .docId(docId)
                .timestamp(System.currentTimeMillis())
                .source("DocEmbeddingTriggerCmdExe")
                .build();
        
        eventPublisher.publishEvent(event);
        log.info("文档向量生成事件已发布: {}", event);
        
        log.info("文档向量生成任务已触发，文档ID: {}", docId);
        return Response.buildSuccess();
    }
} 