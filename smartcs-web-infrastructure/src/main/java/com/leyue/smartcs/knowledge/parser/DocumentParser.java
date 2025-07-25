package com.leyue.smartcs.knowledge.parser;

import dev.langchain4j.data.document.Document;
import com.leyue.smartcs.knowledge.parser.model.ParserExtendParam;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

/**
 * 文档解析器接口
 * 定义了不同类型文档的解析规范
 */
public interface DocumentParser {
    
    /**
     * 解析文档
     * 
     * @param resource 文档资源
     * @param fileName 文件名（用于确定文档类型）
     * @return 解析后的文档列表（某些类型可能生成多个文档，如Excel多个工作表）
     * @throws IOException 解析异常
     */
    List<Document> parse(Resource resource, String fileName, ParserExtendParam parserExtendParam) throws IOException;
    
    /**
     * 获取支持的文档类型
     * 
     * @return 支持的文档类型集合
     */
    String[] getSupportedTypes();
    
    /**
     * 是否支持指定的文件类型
     * 
     * @param extension 文件扩展名
     * @return 是否支持
     */
    boolean supports(String extension);
}