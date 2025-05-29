package com.leyue.smartcs.knowledge.parser;

/**
 * 文档解析器接口
 */
public interface DocumentParser {

    /**
     * 解析文档内容
     *
     * @param fileUrl 文件URL
     * @return 解析后的文本内容
     * @throws Exception 解析异常
     */
    String parseContent(String fileUrl) throws Exception;

    /**
     * 判断是否支持该文件类型
     *
     * @param fileType 文件类型
     * @return 是否支持
     */
    boolean supports(String fileType);
} 