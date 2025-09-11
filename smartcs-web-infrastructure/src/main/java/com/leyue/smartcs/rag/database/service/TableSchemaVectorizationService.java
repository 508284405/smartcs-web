package com.leyue.smartcs.rag.database.service;

import com.leyue.smartcs.domain.database.entity.DatabaseTableSchema;
import com.leyue.smartcs.model.ai.DynamicModelManager;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import com.leyue.smartcs.service.TracingSupport;

/**
 * 表结构向量化服务
 * 负责将数据库表结构信息向量化并存储到向量数据库中
 * 
 * @author Claude
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TableSchemaVectorizationService {
    
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final DynamicModelManager dynamicModelManager;
    private final JdbcTemplate jdbcTemplate;
    
    @Value("${smartcs.nlp2sql.schema-index-prefix:table_schema:}")
    private String schemaIndexPrefix;
    
    /**
     * 表结构缓存
     */
    private final Map<String, DatabaseTableSchema> tableSchemaCache = new ConcurrentHashMap<>();
    
    /**
     * 向量化所有数据库表结构
     * 
     * @param embeddingModelId 嵌入模型ID
     * @return 向量化的表数量
     */
    public CompletableFuture<Integer> vectorizeAllTableSchemas(Long embeddingModelId) {
        return TracingSupport.supplyAsync(() -> {
            try {
                log.info("开始向量化数据库表结构");
                
                // 获取所有表结构
                List<DatabaseTableSchema> schemas = extractAllTableSchemas();
                
                if (schemas.isEmpty()) {
                    log.warn("未找到任何表结构信息");
                    return 0;
                }
                
                // 批量向量化
                int successCount = 0;
                for (DatabaseTableSchema schema : schemas) {
                    try {
                        vectorizeTableSchema(schema, embeddingModelId);
                        successCount++;
                        log.debug("表结构向量化成功: {}", schema.getTableName());
                    } catch (Exception e) {
                        log.error("表结构向量化失败: {}", schema.getTableName(), e);
                    }
                }
                
                log.info("数据库表结构向量化完成，成功: {}/{}", successCount, schemas.size());
                return successCount;
                
            } catch (Exception e) {
                log.error("向量化表结构过程失败", e);
                throw new RuntimeException("向量化表结构失败", e);
            }
        });
    }
    
    /**
     * 向量化单个表结构
     * 
     * @param schema 表结构信息
     * @param embeddingModelId 嵌入模型ID
     */
    public void vectorizeTableSchema(DatabaseTableSchema schema, Long embeddingModelId) {
        try {
            // 生成用于向量化的文本
            String vectorizationText = schema.generateVectorizationText();
            
            // 获取嵌入模型
            EmbeddingModel embeddingModel = dynamicModelManager.getEmbeddingModel(embeddingModelId);
            
            // 生成向量
            Embedding embedding = embeddingModel.embed(vectorizationText).content();
            
            // 创建文本段，包含元数据
            Map<String, String> metadata = new HashMap<>();
            metadata.put("type", "table_schema");
            metadata.put("table_name", schema.getTableName());
            metadata.put("schema_name", schema.getSchemaName());
            if (schema.getTableDescription() != null) {
                metadata.put("table_description", schema.getTableDescription());
            }
            if (schema.getBusinessDescription() != null) {
                metadata.put("business_description", schema.getBusinessDescription());
            }
            metadata.put("column_count", String.valueOf(schema.getColumns() != null ? schema.getColumns().size() : 0));
            
            TextSegment textSegment = TextSegment.from(vectorizationText, Metadata.from(metadata));
            
            // 存储到向量数据库
            String embeddingId = schemaIndexPrefix + schema.getTableName();
            embeddingStore.add(embedding, textSegment);
            
            // 缓存表结构
            tableSchemaCache.put(schema.getTableName(), schema);
            
            log.debug("表结构向量化并存储成功: {} -> {}", schema.getTableName(), embeddingId);
            
        } catch (Exception e) {
            log.error("向量化表结构失败: {}", schema.getTableName(), e);
            throw new RuntimeException("向量化表结构失败: " + schema.getTableName(), e);
        }
    }
    
    /**
     * 更新单个表的结构向量
     * 
     * @param tableName 表名
     * @param embeddingModelId 嵌入模型ID
     * @return 是否更新成功
     */
    public boolean updateTableSchema(String tableName, Long embeddingModelId) {
        try {
            log.info("更新表结构向量: {}", tableName);
            
            // 提取表结构
            DatabaseTableSchema schema = extractTableSchema(tableName);
            if (schema == null) {
                log.warn("表不存在或无法访问: {}", tableName);
                return false;
            }
            
            // 重新向量化
            vectorizeTableSchema(schema, embeddingModelId);
            
            log.info("表结构向量更新成功: {}", tableName);
            return true;
            
        } catch (Exception e) {
            log.error("更新表结构向量失败: {}", tableName, e);
            return false;
        }
    }
    
    /**
     * 获取缓存的表结构
     * 
     * @param tableName 表名
     * @return 表结构，如果不存在则返回null
     */
    public DatabaseTableSchema getCachedTableSchema(String tableName) {
        return tableSchemaCache.get(tableName);
    }
    
    /**
     * 获取所有缓存的表名
     * 
     * @return 表名列表
     */
    public Set<String> getAllCachedTableNames() {
        return new HashSet<>(tableSchemaCache.keySet());
    }
    
    /**
     * 清除指定表的缓存
     * 
     * @param tableName 表名
     */
    public void clearTableCache(String tableName) {
        tableSchemaCache.remove(tableName);
        log.debug("清除表结构缓存: {}", tableName);
    }
    
    /**
     * 清除所有缓存
     */
    public void clearAllCache() {
        tableSchemaCache.clear();
        log.info("清除所有表结构缓存");
    }
    
    /**
     * 提取所有表结构信息
     * 
     * @return 表结构列表
     */
    private List<DatabaseTableSchema> extractAllTableSchemas() {
        try {
            List<String> tableNames = getAvailableTableNames();
            
            return tableNames.stream()
                    .map(this::extractTableSchema)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("提取表结构信息失败", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 提取单个表的结构信息
     * 
     * @param tableName 表名
     * @return 表结构信息
     */
    private DatabaseTableSchema extractTableSchema(String tableName) {
        try {
            // 获取表的基本信息
            DatabaseTableSchema.DatabaseTableSchemaBuilder builder = DatabaseTableSchema.builder()
                    .tableName(tableName)
                    .schemaName(getCurrentSchemaName());
            
            // 获取表注释和描述
            String tableComment = getTableComment(tableName);
            if (tableComment != null && !tableComment.isEmpty()) {
                builder.tableDescription(tableComment);
            }
            
            // 获取字段信息
            List<DatabaseTableSchema.ColumnInfo> columns = extractColumnInfo(tableName);
            builder.columns(columns);
            
            // 获取索引信息
            List<DatabaseTableSchema.IndexInfo> indexes = extractIndexInfo(tableName);
            builder.indexes(indexes);
            
            // 获取外键信息
            List<DatabaseTableSchema.ForeignKeyInfo> foreignKeys = extractForeignKeyInfo(tableName);
            builder.foreignKeys(foreignKeys);
            
            // 获取表统计信息
            DatabaseTableSchema.TableStatistics statistics = extractTableStatistics(tableName);
            builder.statistics(statistics);
            
            return builder.build();
            
        } catch (Exception e) {
            log.error("提取表结构失败: {}", tableName, e);
            return null;
        }
    }
    
    /**
     * 获取数据库中所有可用的表名
     * 
     * @return 表名列表
     */
    private List<String> getAvailableTableNames() {
        String sql = "SELECT table_name FROM information_schema.tables " +
                    "WHERE table_schema = DATABASE() " +
                    "AND table_type = 'BASE TABLE' " +
                    "ORDER BY table_name";
        
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("table_name"));
    }
    
    /**
     * 获取当前数据库schema名
     * 
     * @return schema名称
     */
    private String getCurrentSchemaName() {
        try {
            return jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        } catch (Exception e) {
            log.warn("获取当前schema名失败", e);
            return "unknown";
        }
    }
    
    /**
     * 获取表注释
     * 
     * @param tableName 表名
     * @return 表注释
     */
    private String getTableComment(String tableName) {
        try {
            String sql = "SELECT table_comment FROM information_schema.tables " +
                        "WHERE table_schema = DATABASE() AND table_name = ?";
            return jdbcTemplate.queryForObject(sql, String.class, tableName);
        } catch (Exception e) {
            log.debug("获取表注释失败: {}", tableName);
            return null;
        }
    }
    
    /**
     * 提取字段信息
     * 
     * @param tableName 表名
     * @return 字段信息列表
     */
    private List<DatabaseTableSchema.ColumnInfo> extractColumnInfo(String tableName) {
        String sql = """
            SELECT 
                column_name,
                data_type,
                is_nullable,
                column_default,
                column_type,
                column_key,
                extra,
                column_comment,
                character_maximum_length,
                numeric_precision,
                numeric_scale
            FROM information_schema.columns 
            WHERE table_schema = DATABASE() AND table_name = ?
            ORDER BY ordinal_position
            """;
        
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            return DatabaseTableSchema.ColumnInfo.builder()
                    .columnName(rs.getString("column_name"))
                    .dataType(rs.getString("data_type"))
                    .isNullable("YES".equals(rs.getString("is_nullable")))
                    .defaultValue(rs.getString("column_default"))
                    .isPrimaryKey("PRI".equals(rs.getString("column_key")))
                    .isAutoIncrement(rs.getString("extra").contains("auto_increment"))
                    .comment(rs.getString("column_comment"))
                    .columnDescription(rs.getString("column_comment")) // 使用注释作为描述
                    .maxLength(rs.getObject("character_maximum_length", Integer.class))
                    .scale(rs.getObject("numeric_scale", Integer.class))
                    .build();
        }, tableName);
    }
    
    /**
     * 提取索引信息
     * 
     * @param tableName 表名
     * @return 索引信息列表
     */
    private List<DatabaseTableSchema.IndexInfo> extractIndexInfo(String tableName) {
        String sql = """
            SELECT 
                index_name,
                column_name,
                non_unique,
                index_comment
            FROM information_schema.statistics 
            WHERE table_schema = DATABASE() AND table_name = ?
            ORDER BY index_name, seq_in_index
            """;
        
        Map<String, DatabaseTableSchema.IndexInfo.IndexInfoBuilder> indexBuilders = new LinkedHashMap<>();
        
        jdbcTemplate.query(sql, rs -> {
            String indexName = rs.getString("index_name");
            String columnName = rs.getString("column_name");
            boolean isUnique = rs.getInt("non_unique") == 0;
            String comment = rs.getString("index_comment");
            
            DatabaseTableSchema.IndexInfo.IndexInfoBuilder builder = indexBuilders.computeIfAbsent(indexName, k -> 
                DatabaseTableSchema.IndexInfo.builder()
                    .indexName(indexName)
                    .isUnique(isUnique)
                    .comment(comment)
                    .columns(new ArrayList<>())
            );
            
            // 获取或创建列名列表
            List<String> columns = (List<String>) indexBuilders.get(indexName).build().getColumns();
            if (columns == null) {
                columns = new ArrayList<>();
                builder.columns(columns);
            }
            columns.add(columnName);
        }, tableName);
        
        return indexBuilders.values().stream()
                .map(builder -> builder.build())
                .collect(Collectors.toList());
    }
    
    /**
     * 提取外键信息
     * 
     * @param tableName 表名
     * @return 外键信息列表
     */
    private List<DatabaseTableSchema.ForeignKeyInfo> extractForeignKeyInfo(String tableName) {
        String sql = """
            SELECT 
                constraint_name,
                column_name,
                referenced_table_name,
                referenced_column_name,
                delete_rule,
                update_rule
            FROM information_schema.key_column_usage kcu
            JOIN information_schema.referential_constraints rc 
                ON kcu.constraint_name = rc.constraint_name 
                AND kcu.table_schema = rc.constraint_schema
            WHERE kcu.table_schema = DATABASE() 
                AND kcu.table_name = ?
                AND kcu.referenced_table_name IS NOT NULL
            """;
        
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            return DatabaseTableSchema.ForeignKeyInfo.builder()
                    .constraintName(rs.getString("constraint_name"))
                    .columnName(rs.getString("column_name"))
                    .referencedTableName(rs.getString("referenced_table_name"))
                    .referencedColumnName(rs.getString("referenced_column_name"))
                    .onDelete(rs.getString("delete_rule"))
                    .onUpdate(rs.getString("update_rule"))
                    .build();
        }, tableName);
    }
    
    /**
     * 提取表统计信息
     * 
     * @param tableName 表名
     * @return 统计信息
     */
    private DatabaseTableSchema.TableStatistics extractTableStatistics(String tableName) {
        try {
            String sql = """
                SELECT 
                    table_rows,
                    data_length,
                    index_length,
                    avg_row_length,
                    create_time,
                    update_time
                FROM information_schema.tables 
                WHERE table_schema = DATABASE() AND table_name = ?
                """;
            
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                return DatabaseTableSchema.TableStatistics.builder()
                        .rowCount(rs.getLong("table_rows"))
                        .tableSize(rs.getLong("data_length"))
                        .indexSize(rs.getLong("index_length"))
                        .avgRowLength(rs.getInt("avg_row_length"))
                        .createTime(rs.getTimestamp("create_time") != null ? 
                                   rs.getTimestamp("create_time").getTime() : null)
                        .lastUpdated(rs.getTimestamp("update_time") != null ? 
                                    rs.getTimestamp("update_time").getTime() : System.currentTimeMillis())
                        .build();
            }, tableName);
            
        } catch (Exception e) {
            log.debug("获取表统计信息失败: {}", tableName);
            return DatabaseTableSchema.TableStatistics.builder()
                    .lastUpdated(System.currentTimeMillis())
                    .build();
        }
    }
}
