package com.leyue.smartcs.knowledge.parser;

import com.leyue.smartcs.domain.knowledge.model.Document;

import java.io.File;
import java.util.List;

/**
 * 文档解析器接口
 * 负责解析不同类型的文档内容
 */
public interface DocumentParser {
    
    /**
     * 解析文档内容
     * @param document 文档领域对象
     * @param localFile 已下载到本地的文件
     * @return 解析后的文本内容
     * @throws Exception 解析过程中可能出现的异常
     */
    String parseContent(Document document, File localFile) throws Exception;
    
    /**
     * 获取当前解析器支持的文件类型
     * @return 文件类型（如"pdf", "docx", "txt"等）
     */
    String getSupportedFileType();
} 