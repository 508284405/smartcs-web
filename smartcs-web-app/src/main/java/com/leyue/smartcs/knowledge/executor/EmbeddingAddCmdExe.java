package com.leyue.smartcs.knowledge.executor;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.knowledge.gateway.DocumentGateway;
import com.leyue.smartcs.domain.knowledge.gateway.EmbeddingGateway;
import com.leyue.smartcs.domain.knowledge.model.Document;
import com.leyue.smartcs.domain.knowledge.model.Embedding;
import com.leyue.smartcs.dto.knowledge.EmbeddingAddCmd;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

/**
 * 向量批量添加命令执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmbeddingAddCmdExe {
    
    private final EmbeddingGateway embeddingGateway;
    private final DocumentGateway documentGateway;
    
    /**
     * 执行向量批量添加命令
     * @param cmd 向量批量添加命令
     * @return 操作结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Response execute(EmbeddingAddCmd cmd) {
        log.info("执行向量批量添加命令: {}", cmd);
        
        // 参数校验
        if (cmd.getDocId() == null) {
            throw new BizException("文档ID不能为空");
        }
        
        if (cmd.getItems() == null || cmd.getItems().isEmpty()) {
            throw new BizException("向量数据不能为空");
        }
        
        Long docId = cmd.getDocId();
        
        // 检查文档是否存在
        Optional<Document> docOpt = documentGateway.findById(docId);
        if (docOpt.isEmpty()) {
            throw new BizException("文档不存在，ID: " + docId);
        }
        
        // 先删除文档已有的向量
        embeddingGateway.deleteByDocId(docId);
        
        // 转换并批量保存向量
        List<Embedding> embeddings = convertToEmbeddings(cmd);
        List<Embedding> savedEmbeddings = embeddingGateway.saveBatch(embeddings);
        
        log.info("向量批量添加成功，共 {} 条记录", savedEmbeddings.size());
        return Response.buildSuccess();
    }
    
    /**
     * 转换为向量实体列表
     * @param cmd 向量批量添加命令
     * @return 向量实体列表
     */
    private List<Embedding> convertToEmbeddings(EmbeddingAddCmd cmd) {
        List<Embedding> embeddings = new ArrayList<>(cmd.getItems().size());
        
        for (EmbeddingAddCmd.EmbeddingItem item : cmd.getItems()) {
            byte[] vectorBytes = null;
            if (item.getVector() != null && !item.getVector().isEmpty()) {
                try {
                    vectorBytes = Base64.getDecoder().decode(item.getVector());
                } catch (IllegalArgumentException e) {
                    log.warn("向量解码失败: {}", e.getMessage());
                    throw new BizException("向量Base64解码失败: " + e.getMessage());
                }
            }
            
            Embedding embedding = Embedding.builder()
                    .docId(cmd.getDocId())
                    .sectionIdx(item.getSectionIdx())
                    .contentSnip(item.getContentSnip())
                    .vector(vectorBytes)
                    .modelType(cmd.getModelType())
                    .build();
            
            embeddings.add(embedding);
        }
        
        return embeddings;
    }
} 