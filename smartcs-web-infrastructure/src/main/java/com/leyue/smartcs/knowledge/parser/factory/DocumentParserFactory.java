package com.leyue.smartcs.knowledge.parser.factory;

import com.leyue.smartcs.knowledge.parser.DocumentParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 文档解析器工厂
 */
@Component
@RequiredArgsConstructor
public class DocumentParserFactory {

    private final List<DocumentParser> parsers;

    /**
     * 根据文件类型获取对应的解析器
     *
     * @param fileType 文件类型
     * @return 文档解析器
     * @throws IllegalArgumentException 如果不支持该文件类型
     */
    public DocumentParser getParser(String fileType) {
        return parsers.stream()
                .filter(parser -> parser.supports(fileType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("不支持的文件类型: " + fileType));
    }

    /**
     * 判断是否支持该文件类型
     *
     * @param fileType 文件类型
     * @return 是否支持
     */
    public boolean isSupported(String fileType) {
        return parsers.stream().anyMatch(parser -> parser.supports(fileType));
    }
} 