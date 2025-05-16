package com.leyue.smartcs.knowledge.parser.factory;

import com.leyue.smartcs.knowledge.parser.DocumentParser;

/**
 * 文档解析器工厂接口
 */
public interface DocumentParserFactory {

    /**
     * 获取支持特定文件类型的解析器
     * @param fileType 文件类型
     * @return 文档解析器，如果不支持该类型则返回null
     */
    DocumentParser getParser(String fileType);
} 