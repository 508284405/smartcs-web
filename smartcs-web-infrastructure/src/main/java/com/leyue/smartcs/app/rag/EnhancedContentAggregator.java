package com.leyue.smartcs.app.rag;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.aggregator.ContentAggregator;
import dev.langchain4j.rag.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 内容聚合器
 * 负责合并和排序检索到的内容
 */
@Component
public class EnhancedContentAggregator implements ContentAggregator {

    private static final Logger log = LoggerFactory.getLogger(EnhancedContentAggregator.class);

    // 默认最大内容长度（字符数）
    private static final int DEFAULT_MAX_CONTENT_LENGTH = 4000;
    private static final String CONTENT_SEPARATOR = "\n\n---\n\n";

    @Override
    public List<Content> aggregate(Map<Query, Collection<List<Content>>> queryToContents) {
        log.debug("开始聚合多查询内容: queryCount={}", queryToContents.size());
        
        List<Content> allContents = new java.util.ArrayList<>();
        for (Map.Entry<Query, Collection<List<Content>>> entry : queryToContents.entrySet()) {
            for (List<Content> contentList : entry.getValue()) {
                allContents.addAll(aggregateContentList(entry.getKey(), contentList));
            }
        }
        
        return allContents.stream()
            .distinct()
            .limit(10)
            .collect(Collectors.toList());
    }
    
    /**
     * 聚合单个内容列表
     */
    public List<Content> aggregateContentList(Query query, List<Content> contents) {
        if (contents == null || contents.isEmpty()) {
            log.debug("没有内容需要聚合: query={}", query.text());
            return List.of();
        }

        log.debug("开始聚合内容: query={}, contentCount={}", query.text(), contents.size());

        // 对内容进行排序和去重
        List<Content> processedContents = contents.stream()
            .distinct() // 去重
            .limit(10) // 限制数量，避免内容过多
            .collect(Collectors.toList());

        log.info("内容聚合完成: query={}, originalCount={}, aggregatedCount={}", 
                query.text(), contents.size(), processedContents.size());

        return processedContents;
    }

    /**
     * 合并内容为单个文本块
     * 
     * @param query 查询对象
     * @param contents 内容列表
     * @return 合并后的内容
     */
    public Content mergeContents(Query query, List<Content> contents) {
        if (contents == null || contents.isEmpty()) {
            log.debug("没有内容需要合并: query={}", query.text());
            return Content.from(TextSegment.from(""));
        }

        log.debug("开始合并内容: query={}, contentCount={}", query.text(), contents.size());

        StringBuilder mergedText = new StringBuilder();
        int totalLength = 0;

        for (int i = 0; i < contents.size(); i++) {
            Content content = contents.get(i);
            String text = content.textSegment().text();
            
            // 检查是否会超过最大长度限制
            if (totalLength + text.length() + CONTENT_SEPARATOR.length() > DEFAULT_MAX_CONTENT_LENGTH) {
                log.debug("内容长度超过限制，停止合并: totalLength={}, maxLength={}", 
                         totalLength, DEFAULT_MAX_CONTENT_LENGTH);
                break;
            }

            if (i > 0) {
                mergedText.append(CONTENT_SEPARATOR);
                totalLength += CONTENT_SEPARATOR.length();
            }

            mergedText.append(text);
            totalLength += text.length();
        }

        String finalText = mergedText.toString();
        log.info("内容合并完成: query={}, originalCount={}, mergedLength={}", 
                query.text(), contents.size(), finalText.length());

        return Content.from(TextSegment.from(finalText));
    }

    /**
     * 带权重的内容聚合
     * 根据内容的相关性和质量进行加权排序
     * 
     * @param query 查询对象
     * @param contents 内容列表
     * @param weights 权重列表
     * @return 聚合后的内容列表
     */
    public List<Content> aggregateWithWeights(Query query, List<Content> contents, List<Double> weights) {
        if (contents == null || contents.isEmpty()) {
            log.debug("没有内容需要加权聚合: query={}", query.text());
            return List.of();
        }

        if (weights == null || weights.size() != contents.size()) {
            log.warn("权重列表长度与内容列表不匹配，使用默认聚合: query={}", query.text());
            return aggregateContentList(query, contents);
        }

        log.debug("开始加权聚合内容: query={}, contentCount={}", query.text(), contents.size());

        // 创建带权重的内容对
        List<WeightedContent> weightedContents = new java.util.ArrayList<>();
        for (int i = 0; i < contents.size(); i++) {
            weightedContents.add(new WeightedContent(contents.get(i), weights.get(i)));
        }

        // 按权重排序
        List<Content> sortedContents = weightedContents.stream()
            .sorted((a, b) -> Double.compare(b.weight, a.weight)) // 降序排列
            .map(wc -> wc.content)
            .distinct()
            .limit(10)
            .collect(Collectors.toList());

        log.info("加权内容聚合完成: query={}, originalCount={}, aggregatedCount={}", 
                query.text(), contents.size(), sortedContents.size());

        return sortedContents;
    }

    /**
     * 带权重的内容对象
     */
    private static class WeightedContent {
        final Content content;
        final double weight;

        WeightedContent(Content content, double weight) {
            this.content = content;
            this.weight = weight;
        }
    }

    /**
     * 智能内容摘要
     * 提取内容的关键信息进行摘要
     * 
     * @param query 查询对象
     * @param contents 内容列表
     * @param maxSummaryLength 最大摘要长度
     * @return 摘要内容
     */
    public Content summarizeContents(Query query, List<Content> contents, int maxSummaryLength) {
        if (contents == null || contents.isEmpty()) {
            log.debug("没有内容需要摘要: query={}", query.text());
            return Content.from(TextSegment.from(""));
        }

        log.debug("开始智能摘要内容: query={}, contentCount={}, maxLength={}", 
                 query.text(), contents.size(), maxSummaryLength);

        StringBuilder summary = new StringBuilder();
        int currentLength = 0;

        for (Content content : contents) {
            String text = content.textSegment().text();
            
            // 简单的摘要策略：取每段内容的前面部分
            String excerpt = extractExcerpt(text, Math.min(text.length(), 
                                          maxSummaryLength - currentLength));
            
            if (currentLength + excerpt.length() > maxSummaryLength) {
                break;
            }

            if (summary.length() > 0) {
                summary.append("\n");
                currentLength += 1;
            }

            summary.append(excerpt);
            currentLength += excerpt.length();
        }

        String summaryText = summary.toString();
        log.info("内容摘要完成: query={}, originalCount={}, summaryLength={}", 
                query.text(), contents.size(), summaryText.length());

        return Content.from(TextSegment.from(summaryText));
    }

    /**
     * 提取文本摘要
     */
    private String extractExcerpt(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }

        // 尝试在句号处截断
        String truncated = text.substring(0, maxLength);
        int lastPeriod = truncated.lastIndexOf('。');
        if (lastPeriod > maxLength / 2) {
            return truncated.substring(0, lastPeriod + 1);
        }

        // 否则直接截断并加省略号
        return truncated + "...";
    }
}