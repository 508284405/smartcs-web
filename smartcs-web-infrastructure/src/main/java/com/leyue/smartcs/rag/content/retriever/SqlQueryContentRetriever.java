package com.leyue.smartcs.rag.content.retriever;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;

import com.leyue.smartcs.rag.database.service.NlpToSqlService;
import com.leyue.smartcs.rag.database.service.NlpToSqlService.SqlGenerationResult;

import dev.langchain4j.data.document.Metadata;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import lombok.extern.slf4j.Slf4j;

/**
 * SQL查询内容检索器
 * 支持直接SQL查询和自然语言到SQL的转换
 * 将SQL查询功能集成到LangChain4j的RAG框架中
 * 
 * @author Claude
 */
@Slf4j
public class SqlQueryContentRetriever implements ContentRetriever {

    private final JdbcTemplate jdbcTemplate;
    private final NlpToSqlService nlpToSqlService;
    private final Long chatModelId;
    private final Long embeddingModelId;
    
    @Value("${smartcs.nlp2sql.enable-nlp-mode:true}")
    private Boolean enableNlpMode;
    
    @Value("${smartcs.nlp2sql.min-confidence:0.6}")
    private Double minConfidence;
    
    /**
     * 构造函数
     * 
     * @param jdbcTemplate JDBC模板
     * @param nlpToSqlService NLP到SQL转换服务
     * @param chatModelId 聊天模型ID
     * @param embeddingModelId 嵌入模型ID
     */
    public SqlQueryContentRetriever(JdbcTemplate jdbcTemplate, 
                                   NlpToSqlService nlpToSqlService,
                                   Long chatModelId,
                                   Long embeddingModelId) {
        this.jdbcTemplate = jdbcTemplate;
        this.nlpToSqlService = nlpToSqlService;
        this.chatModelId = chatModelId;
        this.embeddingModelId = embeddingModelId;
    }
    
    // SQL注入防护：只允许SELECT语句
    private static final Pattern SELECT_PATTERN = Pattern.compile(
        "^\\s*SELECT\\s+.*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    // 危险关键词黑名单
    private static final String[] DANGEROUS_KEYWORDS = {
        "DROP", "DELETE", "INSERT", "UPDATE", "ALTER", "CREATE", "TRUNCATE", 
        "EXEC", "EXECUTE", "DECLARE", "UNION", "INFORMATION_SCHEMA"
    };

    @Override
    @SentinelResource(value = "rag:sql-retriever:retrieve",
            blockHandler = "retrieveBlockHandler",
            fallback = "retrieveFallback")
    public List<Content> retrieve(Query query) {
        String queryText = query.text();
        log.info("SQL查询ContentRetriever接收到查询: {}", queryText);
        
        try {
            return handleNaturalLanguageQuery(queryText);
        } catch (Exception e) {
            log.error("查询处理失败: {}", e.getMessage(), e);
            return Collections.singletonList(Content.from(TextSegment.from(
                "查询处理失败: " + e.getMessage(), 
                Metadata.from(Map.of(
                    "source", "sql_query_error",
                    "query", queryText,
                    "error", e.getMessage()
                ))
            )));
        }
    }

    public List<Content> retrieveFallback(Query query, Throwable throwable) {
        log.warn("SQL查询检索降级: queryLength={}, error={}",
                query != null && query.text() != null ? query.text().length() : 0, throwable.getMessage());
        return Collections.singletonList(Content.from(TextSegment.from(
                "查询系统繁忙，请稍后重试。",
                Metadata.from(Map.of(
                        "source", "sql_query_fallback",
                        "error", Optional.ofNullable(throwable.getMessage()).orElse("fallback")
                ))
        )));
    }

    public List<Content> retrieveBlockHandler(Query query, BlockException ex) {
        log.warn("SQL查询检索触发限流: rule={}", ex.getRule());
        return Collections.singletonList(Content.from(TextSegment.from(
                "查询请求过多，请稍后重试。",
                Metadata.from(Map.of(
                        "source", "sql_query_block",
                        "rule", Optional.ofNullable(ex.getRule()).map(Object::toString).orElse("block")
                ))
        )));
    }
    
    /**
     * 处理自然语言查询
     * 
     * @param nlpQuery 自然语言查询
     * @return 查询结果
     */
    private List<Content> handleNaturalLanguageQuery(String nlpQuery) {
        log.info("处理自然语言查询: {}", nlpQuery);
        
        try {
            // 1. NLP转SQL
            SqlGenerationResult sqlResult = nlpToSqlService.generateSql(nlpQuery, chatModelId, embeddingModelId);
            if (!sqlResult.getSuccess()) {
                log.warn("NLP到SQL转换失败: {}", sqlResult.getErrorMessage());
                return Collections.singletonList(Content.from(TextSegment.from(
                    "无法理解您的查询: " + sqlResult.getErrorMessage(), 
                    Metadata.from(Map.of(
                        "source", "nlp_to_sql_failed",
                        "original_query", nlpQuery,
                        "error", sqlResult.getErrorMessage()
                    ))
                )));
            }
            
            // 2. 检查置信度
            if (sqlResult.getConfidence() < minConfidence) {
                log.warn("SQL生成置信度过低: confidence={}, threshold={}", 
                        sqlResult.getConfidence(), minConfidence);
                return Collections.singletonList(Content.from(TextSegment.from(
                    String.format("查询理解置信度较低(%.2f)，生成的SQL可能不准确。建议使用更具体的描述或直接使用SQL查询。", 
                                 sqlResult.getConfidence()), 
                    Metadata.from(Map.of(
                        "source", "low_confidence_sql",
                        "original_query", nlpQuery,
                        "generated_sql", sqlResult.getGeneratedSql(),
                        "confidence", String.valueOf(sqlResult.getConfidence())
                    ))
                )));
            }
            
            // 3. 执行生成的SQL
            String generatedSql = sqlResult.getGeneratedSql();
            log.info("执行生成的SQL: {} (置信度: {})", generatedSql, sqlResult.getConfidence());
            
            List<Content> results = executeSqlQuery(generatedSql);
            
            // 4. 增强结果元数据
            return results.stream()
                    .map(content -> enhanceNlpResultMetadata(content, sqlResult))
                    .toList();
            
        } catch (Exception e) {
            log.error("自然语言查询处理失败: {}", nlpQuery, e);
            return Collections.singletonList(Content.from(TextSegment.from(
                "自然语言查询处理失败: " + e.getMessage(), 
                Metadata.from(Map.of(
                    "source", "nlp_query_error",
                    "original_query", nlpQuery,
                    "error", e.getMessage()
                ))
            )));
        }
    }
    
    /**
     * 执行SQL查询
     * 
     * @param sqlQuery SQL查询语句
     * @return 查询结果
     */
    private List<Content> executeSqlQuery(String sqlQuery) {
        try {
            // 执行查询
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sqlQuery);
            
            if (results.isEmpty()) {
                log.info("SQL查询无结果: {}", sqlQuery);
                return Collections.singletonList(Content.from(TextSegment.from(
                    "查询结果：无数据", 
                    Metadata.from(Map.of(
                        "source", "sql_query_empty",
                        "query", sqlQuery,
                        "result_count", "0"
                    ))
                )));
            }
            
            // 格式化结果并转换为Content
            String formattedResult = formatQueryResults(results);
            
            log.info("SQL查询执行成功，返回{}条记录", results.size());
            
            return Collections.singletonList(Content.from(TextSegment.from(
                formattedResult, 
                Metadata.from(Map.of(
                    "source", "sql_query_success",
                    "query", sqlQuery,
                    "result_count", String.valueOf(results.size())
                ))
            )));
            
        } catch (Exception e) {
            log.error("SQL查询执行失败: query={}", sqlQuery, e);
            return Collections.singletonList(Content.from(TextSegment.from(
                "查询执行失败: " + e.getMessage(), 
                Metadata.from(Map.of(
                    "source", "sql_execution_error",
                    "query", sqlQuery,
                    "error", e.getMessage()
                ))
            )));
        }
    }
    
    /**
     * 增强NLP结果的元数据
     * 
     * @param content 原始内容
     * @param sqlResult SQL生成结果
     * @return 增强后的内容
     */
    private Content enhanceNlpResultMetadata(Content content, SqlGenerationResult sqlResult) {
        Map<String, Object> originalMetadata = content.textSegment().metadata().toMap();
        Map<String, String> enhancedMetadata = new HashMap<>();
        
        // 复制原有元数据
        originalMetadata.forEach((key, value) -> 
            enhancedMetadata.put(key, value != null ? value.toString() : null));
        
        // 添加NLP相关元数据
        enhancedMetadata.put("nlp_mode", "true");
        enhancedMetadata.put("original_query", sqlResult.getOriginalQuery());
        enhancedMetadata.put("generated_sql", sqlResult.getGeneratedSql());
        enhancedMetadata.put("confidence", String.valueOf(sqlResult.getConfidence()));
        enhancedMetadata.put("used_tables", String.join(",", sqlResult.getUsedTables()));
        
        if (sqlResult.getExplanation() != null && !sqlResult.getExplanation().isEmpty()) {
            enhancedMetadata.put("sql_explanation", sqlResult.getExplanation());
        }
        
        return Content.from(TextSegment.from(
            content.textSegment().text(), 
            Metadata.from(enhancedMetadata)
        ));
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
