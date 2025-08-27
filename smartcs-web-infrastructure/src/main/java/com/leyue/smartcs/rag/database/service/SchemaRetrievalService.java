package com.leyue.smartcs.rag.database.service;

import com.leyue.smartcs.domain.database.entity.DatabaseTableSchema;
import com.leyue.smartcs.model.gateway.ModelProvider;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 表结构检索服务
 * 根据自然语言查询检索相关的数据库表结构信息
 * 
 * @author Claude
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SchemaRetrievalService {
    
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final ModelProvider modelProvider;
    private final TableSchemaVectorizationService vectorizationService;
    
    @Value("${smartcs.nlp2sql.schema-search-max-results:10}")
    private Integer maxSearchResults;
    
    @Value("${smartcs.nlp2sql.schema-similarity-threshold:0.5}")
    private Double similarityThreshold;
    
    /**
     * 根据自然语言查询检索相关的表结构
     * 
     * @param nlpQuery 自然语言查询
     * @param embeddingModelId 嵌入模型ID
     * @return 检索结果，包含相关的表结构和相似度分数
     */
    public SchemaRetrievalResult retrieveRelevantSchemas(String nlpQuery, Long embeddingModelId) {
        return retrieveRelevantSchemas(nlpQuery, embeddingModelId, maxSearchResults, similarityThreshold);
    }
    
    /**
     * 根据自然语言查询检索相关的表结构（完整参数控制）
     * 
     * @param nlpQuery 自然语言查询
     * @param embeddingModelId 嵌入模型ID
     * @param maxResults 最大结果数
     * @param minSimilarity 最小相似度阈值
     * @return 检索结果
     */
    public SchemaRetrievalResult retrieveRelevantSchemas(String nlpQuery, Long embeddingModelId, int maxResults, double minSimilarity) {
        try {
            log.debug("检索相关表结构: query={}, maxResults={}, minSimilarity={}", 
                    nlpQuery, maxResults, minSimilarity);
            
            // 生成查询向量
            EmbeddingModel embeddingModel = modelProvider.getEmbeddingModel(embeddingModelId);
            Embedding queryEmbedding = embeddingModel.embed(nlpQuery).content();
            
            // 执行向量搜索
            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(maxResults)
                    .minScore(minSimilarity)
                    .build();
            
            EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);
            
            // 处理搜索结果
            List<SchemaMatch> schemaMatches = processSearchResults(searchResult.matches());
            
            // 过滤并排序结果
            List<SchemaMatch> filteredMatches = schemaMatches.stream()
                    .filter(match -> match.getScore() >= minSimilarity)
                    .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                    .collect(Collectors.toList());
            
            SchemaRetrievalResult result = SchemaRetrievalResult.builder()
                    .query(nlpQuery)
                    .matches(filteredMatches)
                    .totalResults(filteredMatches.size())
                    .searchTime(System.currentTimeMillis())
                    .build();
            
            log.info("表结构检索完成: query={}, results={}", nlpQuery, filteredMatches.size());
            return result;
            
        } catch (Exception e) {
            log.error("表结构检索失败: query={}", nlpQuery, e);
            return SchemaRetrievalResult.builder()
                    .query(nlpQuery)
                    .matches(Collections.emptyList())
                    .totalResults(0)
                    .searchTime(System.currentTimeMillis())
                    .error("检索失败: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * 获取指定表的结构信息
     * 
     * @param tableNames 表名列表
     * @return 表结构列表
     */
    public List<DatabaseTableSchema> getTableSchemas(List<String> tableNames) {
        return tableNames.stream()
                .map(tableName -> vectorizationService.getCachedTableSchema(tableName))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取所有可用的表名
     * 
     * @return 表名列表
     */
    public Set<String> getAllAvailableTableNames() {
        return vectorizationService.getAllCachedTableNames();
    }
    
    /**
     * 根据表名模糊匹配相关表
     * 
     * @param tableNamePattern 表名模式（支持通配符）
     * @return 匹配的表名列表
     */
    public List<String> findTablesByNamePattern(String tableNamePattern) {
        Set<String> allTables = getAllAvailableTableNames();
        String pattern = tableNamePattern.toLowerCase().replace("*", ".*");
        
        return allTables.stream()
                .filter(tableName -> tableName.toLowerCase().matches(pattern))
                .sorted()
                .collect(Collectors.toList());
    }
    
    /**
     * 获取表之间的关联关系
     * 
     * @param tableNames 表名列表
     * @return 关联关系映射
     */
    public Map<String, List<String>> getTableRelationships(List<String> tableNames) {
        Map<String, List<String>> relationships = new HashMap<>();
        
        List<DatabaseTableSchema> schemas = getTableSchemas(tableNames);
        
        for (DatabaseTableSchema schema : schemas) {
            List<String> relatedTables = new ArrayList<>();
            
            if (schema.getForeignKeys() != null) {
                for (DatabaseTableSchema.ForeignKeyInfo fk : schema.getForeignKeys()) {
                    String referencedTable = fk.getReferencedTableName();
                    if (tableNames.contains(referencedTable)) {
                        relatedTables.add(referencedTable);
                    }
                }
            }
            
            relationships.put(schema.getTableName(), relatedTables);
        }
        
        return relationships;
    }
    
    /**
     * 处理搜索结果，提取表结构信息
     * 
     * @param matches 向量搜索匹配结果
     * @return 表结构匹配列表
     */
    private List<SchemaMatch> processSearchResults(List<EmbeddingMatch<TextSegment>> matches) {
        List<SchemaMatch> schemaMatches = new ArrayList<>();
        
        for (EmbeddingMatch<TextSegment> match : matches) {
            try {
                TextSegment segment = match.embedded();
                Map<String, Object> rawMetadata = segment.metadata().toMap();
                Map<String, String> metadata = new HashMap<>();
                rawMetadata.forEach((key, value) -> metadata.put(key, value != null ? value.toString() : null));
                
                String tableName = metadata.get("table_name");
                if (tableName == null) {
                    log.warn("搜索结果中缺少表名信息");
                    continue;
                }
                
                // 从缓存获取完整的表结构
                DatabaseTableSchema schema = vectorizationService.getCachedTableSchema(tableName);
                if (schema == null) {
                    log.warn("缓存中未找到表结构: {}", tableName);
                    continue;
                }
                
                SchemaMatch schemaMatch = SchemaMatch.builder()
                        .schema(schema)
                        .score(match.score())
                        .matchedText(segment.text())
                        .matchReason(generateMatchReason(metadata, match.score()))
                        .build();
                
                schemaMatches.add(schemaMatch);
                
            } catch (Exception e) {
                log.warn("处理搜索结果失败", e);
            }
        }
        
        return schemaMatches;
    }
    
    /**
     * 生成匹配原因说明
     * 
     * @param metadata 元数据
     * @param score 相似度分数
     * @return 匹配原因
     */
    private String generateMatchReason(Map<String, String> metadata, double score) {
        StringBuilder reason = new StringBuilder();
        reason.append("相似度: ").append(String.format("%.3f", score));
        
        String tableDesc = metadata.get("table_description");
        if (tableDesc != null && !tableDesc.isEmpty()) {
            reason.append(", 表描述匹配");
        }
        
        String businessDesc = metadata.get("business_description");
        if (businessDesc != null && !businessDesc.isEmpty()) {
            reason.append(", 业务用途匹配");
        }
        
        return reason.toString();
    }
    
    /**
     * 表结构检索结果
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SchemaRetrievalResult {
        /**
         * 原始查询
         */
        private String query;
        
        /**
         * 匹配结果
         */
        private List<SchemaMatch> matches;
        
        /**
         * 结果总数
         */
        private Integer totalResults;
        
        /**
         * 搜索耗时
         */
        private Long searchTime;
        
        /**
         * 错误信息（如果有）
         */
        private String error;
        
        /**
         * 获取最相关的表结构列表
         * 
         * @return 表结构列表
         */
        public List<DatabaseTableSchema> getTopSchemas() {
            return matches.stream()
                    .map(SchemaMatch::getSchema)
                    .collect(Collectors.toList());
        }
        
        /**
         * 获取最相关的表名列表
         * 
         * @return 表名列表
         */
        public List<String> getTopTableNames() {
            return matches.stream()
                    .map(match -> match.getSchema().getTableName())
                    .collect(Collectors.toList());
        }
        
        /**
         * 检查是否有有效结果
         * 
         * @return true如果有结果
         */
        public boolean hasResults() {
            return matches != null && !matches.isEmpty();
        }
        
        /**
         * 检查是否出错
         * 
         * @return true如果出错
         */
        public boolean hasError() {
            return error != null && !error.isEmpty();
        }
    }
    
    /**
     * 表结构匹配结果
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SchemaMatch {
        /**
         * 匹配的表结构
         */
        private DatabaseTableSchema schema;
        
        /**
         * 相似度分数
         */
        private Double score;
        
        /**
         * 匹配的文本内容
         */
        private String matchedText;
        
        /**
         * 匹配原因说明
         */
        private String matchReason;
        
        /**
         * 获取表名
         * 
         * @return 表名
         */
        public String getTableName() {
            return schema != null ? schema.getTableName() : null;
        }
        
        /**
         * 获取表描述
         * 
         * @return 表描述
         */
        public String getTableDescription() {
            return schema != null ? schema.getTableDescription() : null;
        }
    }
}