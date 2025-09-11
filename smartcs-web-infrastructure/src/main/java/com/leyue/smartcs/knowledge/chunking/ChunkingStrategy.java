package com.leyue.smartcs.knowledge.chunking;

import com.leyue.smartcs.dto.knowledge.ChunkDTO;
import com.leyue.smartcs.knowledge.enums.DocumentTypeEnum;
import dev.langchain4j.data.document.Document;

import java.util.List;
import java.util.Map;

/**
 * 分块策略接口
 * 支持单一策略和组合策略的统一处理
 */
public interface ChunkingStrategy {
    
    /**
     * 策略名称
     */
    String getName();
    
    /**
     * 策略描述
     */
    String getDescription();
    
    /**
     * 支持的文档类型
     */
    List<DocumentTypeEnum> getSupportedDocumentTypes();
    
    /**
     * 是否可以与其他策略组合使用
     */
    boolean isCombinable();
    
    /**
     * 策略优先级，数值越小优先级越高
     */
    int getPriority();
    
    /**
     * 执行分块处理
     * 
     * @param documents 输入文档列表
     * @param documentType 文档类型
     * @param config 分块配置参数
     * @return 分块结果
     */
    List<ChunkDTO> chunk(List<Document> documents, DocumentTypeEnum documentType, Map<String, Object> config);
    
    /**
     * 验证配置参数是否有效
     */
    boolean validateConfig(Map<String, Object> config);
}