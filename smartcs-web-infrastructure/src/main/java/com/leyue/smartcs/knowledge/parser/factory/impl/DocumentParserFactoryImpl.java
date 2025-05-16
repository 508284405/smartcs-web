package com.leyue.smartcs.knowledge.parser.factory.impl;

import com.leyue.smartcs.knowledge.parser.DocumentParser;
import com.leyue.smartcs.knowledge.parser.factory.DocumentParserFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 文档解析器工厂实现类
 * 负责根据文件类型获取对应的文档解析器
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DocumentParserFactoryImpl implements DocumentParserFactory {
    
    /**
     * 所有注册的解析器
     */
    private final List<DocumentParser> parsers;
    
    /**
     * 解析器缓存，避免重复查找
     */
    private final Map<String, DocumentParser> parserCache = new ConcurrentHashMap<>();
    
    /**
     * 获取支持特定文件类型的解析器
     * @param fileType 文件类型
     * @return 文档解析器，如果不支持该类型则返回null
     */
    @Override
    public DocumentParser getParser(String fileType) {
        if (fileType == null || fileType.isEmpty()) {
            log.warn("文件类型为空，无法获取解析器");
            return null;
        }
        
        String normalizedType = fileType.toLowerCase().trim();
        
        // 先查找缓存
        if (parserCache.containsKey(normalizedType)) {
            return parserCache.get(normalizedType);
        }
        
        // 查找支持该类型的解析器
        for (DocumentParser parser : parsers) {
            if (normalizedType.equals(parser.getSupportedFileType())) {
                // 添加到缓存
                parserCache.put(normalizedType, parser);
                log.info("找到文件类型 [{}] 的解析器: {}", normalizedType, parser.getClass().getSimpleName());
                return parser;
            }
        }
        
        log.warn("不支持的文件类型: {}, 支持的类型: {}", 
                normalizedType, 
                parsers.stream().map(DocumentParser::getSupportedFileType).collect(Collectors.joining(", ")));
        
        return null;
    }
} 