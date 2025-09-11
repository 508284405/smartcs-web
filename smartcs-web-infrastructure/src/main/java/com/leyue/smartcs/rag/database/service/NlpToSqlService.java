package com.leyue.smartcs.rag.database.service;

import com.leyue.smartcs.domain.database.entity.DatabaseTableSchema;
import com.leyue.smartcs.model.gateway.ModelProvider;
import com.leyue.smartcs.rag.database.service.SchemaRetrievalService.SchemaRetrievalResult;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 自然语言转SQL服务
 * 将自然语言查询转换为SQL语句，基于向量检索的表结构信息
 * 
 * @author Claude
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NlpToSqlService {
    
    private final SchemaRetrievalService schemaRetrievalService;
    private final ModelProvider modelProvider;
    
    @Value("${smartcs.nlp2sql.max-tables-for-sql:5}")
    private Integer maxTablesForSql;
    
    @Value("${smartcs.nlp2sql.enable-complex-queries:true}")
    private Boolean enableComplexQueries;
    
    @Value("${smartcs.nlp2sql.similarity-threshold:0.6}")
    private Double similarityThreshold;
    
    /**
     * SQL语法验证模式
     */
    private static final Pattern SQL_SELECT_PATTERN = Pattern.compile(
        "^\\s*SELECT\\s+.*?\\s+FROM\\s+.*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    /**
     * 危险关键词检查
     */
    private static final String[] DANGEROUS_KEYWORDS = {
        "DROP", "DELETE", "INSERT", "UPDATE", "ALTER", "CREATE", "TRUNCATE",
        "EXEC", "EXECUTE", "DECLARE", "INFORMATION_SCHEMA"
    };
    
    /**
     * 将自然语言查询转换为SQL语句
     * 
     * @param nlpQuery 自然语言查询
     * @param chatModelId 聊天模型ID
     * @param embeddingModelId 嵌入模型ID
     * @return SQL转换结果
     */
    public SqlGenerationResult generateSql(String nlpQuery, Long chatModelId, Long embeddingModelId) {
        return generateSql(nlpQuery, chatModelId, embeddingModelId, maxTablesForSql, similarityThreshold);
    }
    
    /**
     * 将自然语言查询转换为SQL语句（完整参数控制）
     * 
     * @param nlpQuery 自然语言查询
     * @param chatModelId 聊天模型ID
     * @param embeddingModelId 嵌入模型ID
     * @param maxTables 最大表数量
     * @param minSimilarity 最小相似度
     * @return SQL转换结果
     */
    public SqlGenerationResult generateSql(String nlpQuery, Long chatModelId, Long embeddingModelId, int maxTables, double minSimilarity) {
        try {
            log.info("开始NLP到SQL转换: query={}", nlpQuery);
            
            // 1. 检索相关表结构
            SchemaRetrievalResult schemaResult = schemaRetrievalService
                    .retrieveRelevantSchemas(nlpQuery, embeddingModelId, maxTables, minSimilarity);
            
            if (!schemaResult.hasResults()) {
                return SqlGenerationResult.builder()
                        .originalQuery(nlpQuery)
                        .success(false)
                        .errorMessage("未找到相关的表结构信息")
                        .confidence(0.0)
                        .build();
            }
            
            // 2. 构建SQL生成提示
            String sqlPrompt = buildSqlGenerationPrompt(nlpQuery, schemaResult);
            
            // 3. 调用LLM生成SQL
            ChatModel chatModel = modelProvider.getChatModel(chatModelId);
            String llmResponse = chatModel.chat(UserMessage.from(sqlPrompt))
                    .aiMessage().text();
            
            // 4. 解析和验证生成的SQL
            SqlParseResult parseResult = parseSqlFromResponse(llmResponse);
            
            if (!Boolean.TRUE.equals(parseResult.getValid())) {
                return SqlGenerationResult.builder()
                        .originalQuery(nlpQuery)
                        .success(false)
                        .errorMessage("生成的SQL语句无效: " + parseResult.getErrorMessage())
                        .rawLlmResponse(llmResponse)
                        .build();
            }
            
            // 5. 安全验证
            SecurityValidationResult securityResult = validateSqlSecurity(parseResult.getSql());
            if (!Boolean.TRUE.equals(securityResult.getSafe())) {
                return SqlGenerationResult.builder()
                        .originalQuery(nlpQuery)
                        .success(false)
                        .errorMessage("SQL安全验证失败: " + securityResult.getReason())
                        .generatedSql(parseResult.getSql())
                        .rawLlmResponse(llmResponse)
                        .build();
            }
            
            // 6. 计算置信度
            double confidence = calculateConfidence(schemaResult, parseResult);
            
            SqlGenerationResult result = SqlGenerationResult.builder()
                    .originalQuery(nlpQuery)
                    .generatedSql(parseResult.getSql())
                    .success(true)
                    .confidence(confidence)
                    .usedTables(schemaResult.getTopTableNames())
                    .rawLlmResponse(llmResponse)
                    .explanation(parseResult.getExplanation())
                    .generationTime(System.currentTimeMillis())
                    .build();
            
            log.info("NLP到SQL转换成功: query={}, confidence={}, tables={}", 
                    nlpQuery, confidence, result.getUsedTables());
            
            return result;
            
        } catch (Exception e) {
            log.error("NLP到SQL转换失败: query={}", nlpQuery, e);
            return SqlGenerationResult.builder()
                    .originalQuery(nlpQuery)
                    .success(false)
                    .errorMessage("转换过程发生异常: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * 检测输入是否为自然语言（而非SQL）
     * 
     * @param input 输入文本
     * @return true如果是自然语言
     */
    public boolean isNaturalLanguage(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = input.trim().toUpperCase();
        
        // 如果以SQL关键词开头，认为是SQL语句
        String[] sqlKeywords = {"SELECT", "WITH", "SHOW", "DESCRIBE", "EXPLAIN"};
        for (String keyword : sqlKeywords) {
            if (trimmed.startsWith(keyword)) {
                return false;
            }
        }
        
        // 如果包含典型的SQL模式，认为是SQL
        if (SQL_SELECT_PATTERN.matcher(input).matches()) {
            return false;
        }
        
        // 其他情况认为是自然语言
        return true;
    }
    
    /**
     * 构建SQL生成提示
     * 
     * @param nlpQuery 自然语言查询
     * @param schemaResult 表结构检索结果
     * @return SQL生成提示
     */
    private String buildSqlGenerationPrompt(String nlpQuery, SchemaRetrievalResult schemaResult) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("你是一个专业的SQL生成助手。请根据用户的自然语言查询和提供的数据库表结构，生成准确的SQL查询语句。\\n\\n");
        
        // 用户查询
        prompt.append("用户查询: ").append(nlpQuery).append("\\n\\n");
        
        // 相关表结构
        prompt.append("相关数据库表结构:\\n");
        List<DatabaseTableSchema> schemas = schemaResult.getTopSchemas();
        for (DatabaseTableSchema schema : schemas) {
            prompt.append(schema.generateSqlHint()).append("\\n");
        }
        
        // 表关联关系
        if (schemas.size() > 1) {
            Map<String, List<String>> relationships = schemaRetrievalService
                    .getTableRelationships(schemaResult.getTopTableNames());
            if (!relationships.isEmpty()) {
                prompt.append("表关联关系:\\n");
                relationships.forEach((table, related) -> {
                    if (!related.isEmpty()) {
                        prompt.append("- ").append(table).append(" 关联到: ")
                              .append(String.join(", ", related)).append("\\n");
                    }
                });
                prompt.append("\\n");
            }
        }
        
        // 生成要求
        prompt.append("请按以下要求生成SQL:\\n");
        prompt.append("1. 只生成SELECT查询语句\\n");
        prompt.append("2. 使用准确的表名和字段名\\n");
        prompt.append("3. 如需多表查询，使用适当的JOIN\\n");
        prompt.append("4. 添加必要的WHERE条件\\n");
        prompt.append("5. 考虑查询性能，避免不必要的复杂操作\\n");
        
        if (!enableComplexQueries) {
            prompt.append("6. 避免使用子查询和复杂的聚合函数\\n");
        }
        
        prompt.append("\\n请按以下格式返回结果:\\n");
        prompt.append("```sql\\n");
        prompt.append("[生成的SQL语句]\\n");
        prompt.append("```\\n\\n");
        prompt.append("解释: [简要说明SQL的逻辑和字段选择原因]");
        
        return prompt.toString();
    }
    
    /**
     * 从LLM响应中解析SQL语句
     * 
     * @param llmResponse LLM响应
     * @return SQL解析结果
     */
    private SqlParseResult parseSqlFromResponse(String llmResponse) {
        if (llmResponse == null || llmResponse.trim().isEmpty()) {
            return SqlParseResult.builder()
                    .valid(false)
                    .errorMessage("LLM返回空响应")
                    .build();
        }
        
        // 提取SQL代码块
        String sql = extractSqlFromCodeBlock(llmResponse);
        if (sql.isEmpty()) {
            // 如果没有代码块，尝试提取第一行可能的SQL
            sql = extractSqlFromPlainText(llmResponse);
        }
        
        if (sql.isEmpty()) {
            return SqlParseResult.builder()
                    .valid(false)
                    .errorMessage("无法从响应中提取SQL语句")
                    .build();
        }
        
        // 基础语法验证
        if (!SQL_SELECT_PATTERN.matcher(sql).matches()) {
            return SqlParseResult.builder()
                    .valid(false)
                    .errorMessage("生成的不是有效的SELECT语句")
                    .sql(sql)
                    .build();
        }
        
        // 提取解释
        String explanation = extractExplanationFromResponse(llmResponse);
        
        return SqlParseResult.builder()
                .valid(true)
                .sql(sql.trim())
                .explanation(explanation)
                .build();
    }
    
    /**
     * 从代码块中提取SQL
     * 
     * @param response 响应文本
     * @return SQL语句
     */
    private String extractSqlFromCodeBlock(String response) {
        Pattern codeBlockPattern = Pattern.compile("```(?:sql)?\\s*([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
        var matcher = codeBlockPattern.matcher(response);
        
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        return "";
    }
    
    /**
     * 从纯文本中提取SQL
     * 
     * @param response 响应文本
     * @return SQL语句
     */
    private String extractSqlFromPlainText(String response) {
        String[] lines = response.split("\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.toUpperCase().startsWith("SELECT")) {
                return trimmed;
            }
        }
        return "";
    }
    
    /**
     * 从响应中提取解释
     * 
     * @param response 响应文本
     * @return 解释文本
     */
    private String extractExplanationFromResponse(String response) {
        Pattern explanationPattern = Pattern.compile("解释[：:]\\s*([\\s\\S]*?)(?:\\n\\n|$)", Pattern.CASE_INSENSITIVE);
        var matcher = explanationPattern.matcher(response);
        
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        // 如果没有明确的解释标记，返回代码块后的文本
        String afterCodeBlock = response.replaceAll("```[\\s\\S]*?```", "").trim();
        if (afterCodeBlock.length() > 0 && afterCodeBlock.length() < 500) {
            return afterCodeBlock;
        }
        
        return "";
    }
    
    /**
     * SQL安全验证
     * 
     * @param sql SQL语句
     * @return 安全验证结果
     */
    private SecurityValidationResult validateSqlSecurity(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return SecurityValidationResult.builder()
                    .safe(false)
                    .reason("SQL语句为空")
                    .build();
        }
        
        String upperSql = sql.toUpperCase();
        
        // 检查危险关键词
        for (String keyword : DANGEROUS_KEYWORDS) {
            if (upperSql.contains(keyword)) {
                return SecurityValidationResult.builder()
                        .safe(false)
                        .reason("包含危险关键词: " + keyword)
                        .build();
            }
        }
        
        // 检查是否为SELECT语句
        if (!upperSql.trim().startsWith("SELECT")) {
            return SecurityValidationResult.builder()
                    .safe(false)
                    .reason("只允许SELECT查询")
                    .build();
        }
        
        // 检查嵌套查询深度（如果禁用复杂查询）
        if (!enableComplexQueries) {
            long selectCount = upperSql.chars()
                    .mapToObj(c -> String.valueOf((char) c))
                    .collect(Collectors.joining())
                    .split("SELECT").length - 1;
            
            if (selectCount > 2) {
                return SecurityValidationResult.builder()
                        .safe(false)
                        .reason("不允许复杂的嵌套查询")
                        .build();
            }
        }
        
        return SecurityValidationResult.builder()
                .safe(true)
                .build();
    }
    
    /**
     * 计算生成SQL的置信度
     * 
     * @param schemaResult 表结构检索结果
     * @param parseResult SQL解析结果
     * @return 置信度分数 (0.0 - 1.0)
     */
    private double calculateConfidence(SchemaRetrievalResult schemaResult, SqlParseResult parseResult) {
        double confidence = 0.0;
        
        // 基于表结构匹配的置信度 (40%)
        if (schemaResult.hasResults()) {
            double avgSimilarity = schemaResult.getMatches().stream()
                    .mapToDouble(match -> match.getScore())
                    .average().orElse(0.0);
            confidence += avgSimilarity * 0.4;
        }
        
        // 基于SQL语法正确性 (30%)
        if (Boolean.TRUE.equals(parseResult.getValid())) {
            confidence += 0.3;
        }
        
        // 基于表数量的合理性 (15%)
        int tableCount = schemaResult.getTotalResults();
        if (tableCount >= 1 && tableCount <= 3) {
            confidence += 0.15;
        } else if (tableCount > 3) {
            confidence += 0.1; // 表太多可能过于复杂
        }
        
        // 基于响应质量 (15%)
        if (parseResult.getExplanation() != null && !parseResult.getExplanation().isEmpty()) {
            confidence += 0.15;
        }
        
        return Math.min(confidence, 1.0);
    }
    
    /**
     * SQL解析结果
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SqlParseResult {
        private Boolean valid;
        private String sql;
        private String explanation;
        private String errorMessage;
    }
    
    /**
     * 安全验证结果
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SecurityValidationResult {
        private Boolean safe;
        private String reason;
    }
    
    /**
     * SQL生成结果
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SqlGenerationResult {
        /**
         * 原始自然语言查询
         */
        private String originalQuery;
        
        /**
         * 生成的SQL语句
         */
        private String generatedSql;
        
        /**
         * 是否生成成功
         */
        private Boolean success;
        
        /**
         * 置信度分数 (0.0 - 1.0)
         */
        private Double confidence;
        
        /**
         * 使用的表列表
         */
        private List<String> usedTables;
        
        /**
         * LLM原始响应
         */
        private String rawLlmResponse;
        
        /**
         * SQL逻辑解释
         */
        private String explanation;
        
        /**
         * 错误信息（如果失败）
         */
        private String errorMessage;
        
        /**
         * 生成时间戳
         */
        private Long generationTime;
        
        /**
         * 是否高置信度结果
         * 
         * @return true如果置信度 >= 0.7
         */
        public boolean isHighConfidence() {
            return confidence != null && confidence >= 0.7;
        }
        
        /**
         * 获取简要的结果描述
         * 
         * @return 结果描述
         */
        public String getResultSummary() {
            if (success) {
                return String.format("成功生成SQL，置信度: %.2f，使用表: %s", 
                        confidence, String.join(", ", usedTables));
            } else {
                return "生成失败: " + errorMessage;
            }
        }
    }
}