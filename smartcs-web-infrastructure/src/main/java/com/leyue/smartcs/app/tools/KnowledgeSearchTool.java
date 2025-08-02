package com.leyue.smartcs.app.tools;

import com.leyue.smartcs.app.rag.KnowledgeContentRetriever;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.query.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 知识库搜索工具
 * 为AI提供知识库搜索能力
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KnowledgeSearchTool {

    private final KnowledgeContentRetriever knowledgeContentRetriever;

    /**
     * 搜索知识库内容
     * 
     * @param query 搜索查询
     * @param maxResults 最大结果数，默认5
     * @return 搜索结果摘要
     */
    @Tool("搜索知识库中与查询相关的内容")
    public String searchKnowledge(String query, int maxResults) {
        try {
            log.debug("工具调用 - 知识库搜索: query={}, maxResults={}", query, maxResults);
            
            // 限制最大结果数，避免返回过多内容
            int limitedMaxResults = Math.min(maxResults, 10);
            
            Query searchQuery = Query.from(query);
            List<Content> contents = knowledgeContentRetriever.retrieve(searchQuery, limitedMaxResults, 0.6);
            
            if (contents.isEmpty()) {
                log.debug("知识库搜索无结果: query={}", query);
                return "未找到相关的知识内容。";
            }
            
            // 格式化搜索结果
            StringBuilder result = new StringBuilder();
            result.append("找到 ").append(contents.size()).append(" 条相关知识内容：\n\n");
            
            for (int i = 0; i < contents.size(); i++) {
                Content content = contents.get(i);
                String text = content.textSegment().text();
                
                // 限制每条内容的长度
                String excerpt = text.length() > 200 ? text.substring(0, 200) + "..." : text;
                
                result.append("【知识 ").append(i + 1).append("】\n");
                result.append(excerpt);
                
                if (i < contents.size() - 1) {
                    result.append("\n\n");
                }
            }
            
            log.info("知识库搜索完成: query={}, foundCount={}", query, contents.size());
            return result.toString();
            
        } catch (Exception e) {
            log.error("知识库搜索工具出错: query={}, error={}", query, e.getMessage(), e);
            return "知识库搜索失败：" + e.getMessage();
        }
    }

    /**
     * 搜索指定知识库的内容
     * 
     * @param query 搜索查询
     * @param knowledgeBaseId 知识库ID
     * @param maxResults 最大结果数，默认5
     * @return 搜索结果摘要
     */
    @Tool("在指定知识库中搜索与查询相关的内容")
    public String searchKnowledgeBase(String query, Long knowledgeBaseId, int maxResults) {
        try {
            log.debug("工具调用 - 指定知识库搜索: query={}, knowledgeBaseId={}, maxResults={}", 
                     query, knowledgeBaseId, maxResults);
            
            // 限制最大结果数
            int limitedMaxResults = Math.min(maxResults, 10);
            
            Query searchQuery = Query.from(query);
            List<Content> contents = knowledgeContentRetriever.retrieveByKnowledgeBase(
                searchQuery, knowledgeBaseId, limitedMaxResults, 0.6);
            
            if (contents.isEmpty()) {
                log.debug("指定知识库搜索无结果: query={}, knowledgeBaseId={}", query, knowledgeBaseId);
                return String.format("在知识库 %d 中未找到相关内容。", knowledgeBaseId);
            }
            
            // 格式化搜索结果
            StringBuilder result = new StringBuilder();
            result.append("在知识库 ").append(knowledgeBaseId).append(" 中找到 ")
                  .append(contents.size()).append(" 条相关内容：\n\n");
            
            for (int i = 0; i < contents.size(); i++) {
                Content content = contents.get(i);
                String text = content.textSegment().text();
                
                // 限制每条内容的长度
                String excerpt = text.length() > 200 ? text.substring(0, 200) + "..." : text;
                
                result.append("【内容 ").append(i + 1).append("】\n");
                result.append(excerpt);
                
                if (i < contents.size() - 1) {
                    result.append("\n\n");
                }
            }
            
            log.info("指定知识库搜索完成: query={}, knowledgeBaseId={}, foundCount={}", 
                    query, knowledgeBaseId, contents.size());
            return result.toString();
            
        } catch (Exception e) {
            log.error("指定知识库搜索工具出错: query={}, knowledgeBaseId={}, error={}", 
                     query, knowledgeBaseId, e.getMessage(), e);
            return String.format("知识库 %d 搜索失败：%s", knowledgeBaseId, e.getMessage());
        }
    }

    /**
     * 获取知识库摘要信息
     * 
     * @param knowledgeBaseId 知识库ID
     * @return 知识库摘要
     */
    @Tool("获取指定知识库的摘要信息")
    public String getKnowledgeBaseSummary(Long knowledgeBaseId) {
        try {
            log.debug("工具调用 - 获取知识库摘要: knowledgeBaseId={}", knowledgeBaseId);
            
            // 这里可以调用知识库相关的服务获取摘要信息
            // 暂时返回简单的信息
            return String.format("知识库 %d 包含了相关领域的专业知识内容，可以为您的问题提供权威参考。", knowledgeBaseId);
            
        } catch (Exception e) {
            log.error("获取知识库摘要工具出错: knowledgeBaseId={}, error={}", knowledgeBaseId, e.getMessage(), e);
            return String.format("获取知识库 %d 摘要失败：%s", knowledgeBaseId, e.getMessage());
        }
    }

    /**
     * 搜索并获取详细内容
     * 
     * @param query 搜索查询
     * @param includeMetadata 是否包含元数据
     * @return 详细搜索结果
     */
    @Tool("搜索知识库并返回详细内容，包括相关元数据")
    public String searchKnowledgeDetailed(String query, boolean includeMetadata) {
        try {
            log.debug("工具调用 - 详细知识库搜索: query={}, includeMetadata={}", query, includeMetadata);
            
            Query searchQuery = Query.from(query);
            List<Content> contents = knowledgeContentRetriever.retrieve(searchQuery, 5, 0.6);
            
            if (contents.isEmpty()) {
                return "未找到相关的知识内容。";
            }
            
            StringBuilder result = new StringBuilder();
            result.append("详细搜索结果（共 ").append(contents.size()).append(" 条）：\n\n");
            
            for (int i = 0; i < contents.size(); i++) {
                Content content = contents.get(i);
                String text = content.textSegment().text();
                
                result.append("=== 内容 ").append(i + 1).append(" ===\n");
                result.append(text);
                
                if (includeMetadata && content.textSegment().metadata() != null) {
                    result.append("\n【元数据】\n");
                    content.textSegment().metadata().toMap().forEach((key, value) -> 
                        result.append(key).append(": ").append(value).append("\n"));
                }
                
                if (i < contents.size() - 1) {
                    result.append("\n\n");
                }
            }
            
            log.info("详细知识库搜索完成: query={}, foundCount={}", query, contents.size());
            return result.toString();
            
        } catch (Exception e) {
            log.error("详细知识库搜索工具出错: query={}, error={}", query, e.getMessage(), e);
            return "详细知识库搜索失败：" + e.getMessage();
        }
    }
}