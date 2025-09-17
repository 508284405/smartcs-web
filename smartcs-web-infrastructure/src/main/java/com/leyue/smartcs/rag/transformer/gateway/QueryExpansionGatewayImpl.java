package com.leyue.smartcs.rag.transformer.gateway;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.leyue.smartcs.domain.rag.transformer.gateway.QueryExpansionGateway;
import com.leyue.smartcs.model.ai.DynamicModelManager;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 查询扩展Gateway实现
 * 基于LangChain4j实现查询扩展的基础设施服务
 * 
 * @author Claude
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QueryExpansionGatewayImpl implements QueryExpansionGateway {
    
    private final DynamicModelManager dynamicModelManager;
    
    @Override
    @SentinelResource(value = "rag:query-expansion:generate",
            blockHandler = "generateExpansionBlockHandler",
            fallback = "generateExpansionFallback")
    public String generateExpansion(String prompt, Long modelId) {
        try {
            log.debug("开始生成查询扩展: modelId={}, promptLength={}", modelId, prompt.length());
            
            ChatModel chatModel = dynamicModelManager.getChatModel(modelId);
            String result = chatModel.chat(UserMessage.from(prompt))
                    .aiMessage().text();
            
            log.debug("查询扩展生成完成: modelId={}, resultLength={}", modelId, result.length());
            return result;
            
        } catch (Exception e) {
            log.error("查询扩展生成失败: modelId={}", modelId, e);
            throw new RuntimeException("查询扩展生成失败: " + e.getMessage(), e);
        }
    }

    public String generateExpansionFallback(String prompt, Long modelId, Throwable e) {
        log.warn("查询扩展降级: modelId={}, promptLength={}, error={}", modelId,
                prompt != null ? prompt.length() : 0, e.getMessage());
        return prompt;
    }

    public String generateExpansionBlockHandler(String prompt, Long modelId, BlockException ex) {
        log.warn("查询扩展触发限流: modelId={}, rule={}", modelId, ex.getRule());
        return prompt;
    }

    @Override
    @SentinelResource(value = "rag:query-expansion:parse",
            blockHandler = "parseExpandedQueriesBlockHandler",
            fallback = "parseExpandedQueriesFallback")
    public List<String> parseExpandedQueries(String expandedText, int maxQueries) {
        List<String> queries = new ArrayList<>();
        
        if (expandedText == null || expandedText.trim().isEmpty()) {
            log.warn("扩展文本为空，返回空查询列表");
            return queries;
        }
        
        try {
            log.debug("开始解析扩展查询: textLength={}, maxQueries={}", expandedText.length(), maxQueries);
            
            String[] lines = expandedText.trim().split("\n");
            for (String line : lines) {
                String cleanedQuery = cleanQueryLine(line);
                
                if (!cleanedQuery.isEmpty() && queries.size() < maxQueries) {
                    queries.add(cleanedQuery);
                    log.debug("解析到查询: {}", cleanedQuery);
                }
                
                if (queries.size() >= maxQueries) {
                    break;
                }
            }
            
            log.debug("查询解析完成: parsedCount={}, maxQueries={}", queries.size(), maxQueries);
            return queries;
            
        } catch (Exception e) {
            log.error("查询解析失败: textLength={}", expandedText.length(), e);
            return queries; // 返回已解析的部分结果
        }
    }

    public List<String> parseExpandedQueriesFallback(String expandedText, int maxQueries, Throwable e) {
        log.warn("解析扩展查询降级: maxQueries={}, error={}", maxQueries, e.getMessage());
        return new ArrayList<>();
    }

    public List<String> parseExpandedQueriesBlockHandler(String expandedText, int maxQueries, BlockException ex) {
        log.warn("解析扩展查询触发限流: rule={}", ex.getRule());
        return new ArrayList<>();
    }
    
    /**
     * 清理查询行
     * 移除编号、特殊字符等
     */
    private String cleanQueryLine(String line) {
        if (line == null) {
            return "";
        }
        
        String cleaned = line.trim();
        
        // 移除可能的编号前缀 (如 "1. ", "1) ", "1： "等)
        cleaned = cleaned.replaceAll("^\\d+[.)：:．\\s]+", "").trim();
        
        // 移除可能的破折号前缀 (如 "- ", "* "等)
        cleaned = cleaned.replaceAll("^[-*•]\\s+", "").trim();
        
        // 移除可能的引号
        if (cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1).trim();
        }
        
        if (cleaned.startsWith("'") && cleaned.endsWith("'")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1).trim();
        }
        
        // 移除多余的空白字符
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        
        return cleaned;
    }
}
