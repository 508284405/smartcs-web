package com.leyue.smartcs.rag.content.retriever;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * SQL查询内容检索器
 * 将SQL查询功能集成到LangChain4j的RAG框架中
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SqlQueryContentRetriever implements ContentRetriever {

    private final JdbcTemplate jdbcTemplate;
    
    // SQL注入防护：只允许SELECT语句
    private static final Pattern SELECT_PATTERN = Pattern.compile(
        "^\\s*SELECT\\s+.*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    // 危险关键词黑名单
    private static final String[] DANGEROUS_KEYWORDS = {
        "DROP", "DELETE", "INSERT", "UPDATE", "ALTER", "CREATE", "TRUNCATE", 
        "EXEC", "EXECUTE", "DECLARE", "UNION", "INFORMATION_SCHEMA"
    };

    @Override
    public List<Content> retrieve(Query query) {
        try {
            String sqlQuery = query.text();
            log.info("SQL查询ContentRetriever接收到查询: {}", sqlQuery);
            
            // 验证SQL安全性
            if (!isSelectQuery(sqlQuery)) {
                log.warn("非SELECT查询被拒绝: {}", sqlQuery);
                return List.of();
            }
            
            if (containsDangerousKeywords(sqlQuery)) {
                log.warn("包含危险关键词的查询被拒绝: {}", sqlQuery);
                return List.of();
            }
            
            // 执行查询
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sqlQuery);
            
            if (results.isEmpty()) {
                log.info("SQL查询无结果: {}", sqlQuery);
                return List.of(Content.from(TextSegment.from("查询结果：无数据", Metadata.from(Map.of(
                "source", "sql_query",
                "query", sqlQuery
            )))));
            }
            
            // 格式化结果并转换为Content
            String formattedResult = formatQueryResults(results);
            
            log.info("SQL查询执行成功，返回{}条记录", results.size());
            
            return List.of(Content.from(TextSegment.from(formattedResult, Metadata.from(Map.of(
                "source", "sql_query",
                "query", sqlQuery,
                "result_count", String.valueOf(results.size())
            )))));
            
        } catch (Exception e) {
            log.error("SQL查询执行失败: {}", e.getMessage(), e);
            return List.of(Content.from(TextSegment.from("查询执行失败: " + e.getMessage(), Metadata.from(Map.of(
                "source", "sql_query",
                "query", query.text(),
                "error", e.getMessage()
            )))));
        }
    }

    /**
     * 格式化查询结果
     */
    private String formatQueryResults(List<Map<String, Object>> results) {
        StringBuilder sb = new StringBuilder();
        sb.append("SQL查询结果（共").append(results.size()).append("条记录）：\n");
        
        // 添加表头
        if (!results.isEmpty()) {
            Map<String, Object> firstRow = results.get(0);
            sb.append(String.join(" | ", firstRow.keySet())).append("\n");
            sb.append("-".repeat(50)).append("\n");
            
            // 添加数据行（限制显示前10行）
            int limit = Math.min(results.size(), 10);
            for (int i = 0; i < limit; i++) {
                Map<String, Object> row = results.get(i);
                sb.append(String.join(" | ", 
                    row.values().stream()
                        .map(v -> v == null ? "NULL" : v.toString())
                        .toArray(String[]::new)
                )).append("\n");
            }
            
            if (results.size() > 10) {
                sb.append("... 还有 ").append(results.size() - 10).append(" 条记录\n");
            }
        }
        
        return sb.toString();
    }
    
    /**
     * 验证是否为SELECT查询
     */
    private boolean isSelectQuery(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return false;
        }
        return SELECT_PATTERN.matcher(sql.trim()).matches();
    }
    
    /**
     * 检查是否包含危险关键词
     */
    private boolean containsDangerousKeywords(String sql) {
        String upperSql = sql.toUpperCase();
        for (String keyword : DANGEROUS_KEYWORDS) {
            if (upperSql.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
