package com.leyue.smartcs.rag.knowledge.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 知识查询结果
 * 封装知识库查询的结果项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeResult {

    /**
     * 内容ID
     */
    private String contentId;

    /**
     * 知识库ID
     */
    private Long knowledgeBaseId;

    /**
     * 知识库名称
     */
    private String knowledgeBaseName;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 内容摘要
     */
    private String summary;

    /**
     * 相关性分数
     */
    private Double relevanceScore;

    /**
     * 内容类型
     */
    private ContentType contentType;

    /**
     * 来源信息
     */
    private SourceInfo source;

    /**
     * 匹配的关键词
     */
    private List<String> matchedKeywords;

    /**
     * 高亮片段
     */
    private List<String> highlights;

    /**
     * 元数据
     */
    private Map<String, Object> metadata;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 访问次数
     */
    private Integer accessCount;

    /**
     * 评分
     */
    private Double rating;

    /**
     * 标签
     */
    private List<String> tags;

    /**
     * 层级信息（用于文档结构）
     */
    private HierarchyInfo hierarchy;

    /**
     * 向量距离（用于向量搜索）
     */
    private Double vectorDistance;

    /**
     * 内容类型枚举
     */
    public enum ContentType {
        DOCUMENT,       // 文档
        CHUNK,          // 文档片段
        FAQ,            // 常见问题
        CODE,           // 代码
        IMAGE,          // 图片
        TABLE,          // 表格
        STRUCTURED      // 结构化数据
    }

    /**
     * 来源信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceInfo {
        private String sourceId;
        private String sourceName;
        private String sourceType;
        private String sourceUrl;
        private String author;
        private LocalDateTime publishedAt;
        private String version;
    }

    /**
     * 层级信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HierarchyInfo {
        private String parentId;
        private List<String> childIds;
        private Integer level;
        private String path;
        private Integer position;
    }
}