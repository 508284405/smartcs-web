package com.leyue.smartcs.domain.database.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 数据库表结构实体
 * 用于存储数据库表的元数据信息，支持向量化检索
 * 
 * @author Claude
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseTableSchema {
    
    /**
     * 表名（英文）
     */
    private String tableName;
    
    /**
     * 表的中文描述
     */
    private String tableDescription;
    
    /**
     * 表的业务含义和用途说明
     */
    private String businessDescription;
    
    /**
     * 数据库schema/模式名
     */
    private String schemaName;
    
    /**
     * 字段列表
     */
    private List<ColumnInfo> columns;
    
    /**
     * 索引信息
     */
    private List<IndexInfo> indexes;
    
    /**
     * 外键关系
     */
    private List<ForeignKeyInfo> foreignKeys;
    
    /**
     * 表的统计信息（记录数、大小等）
     */
    private TableStatistics statistics;
    
    /**
     * 扩展属性（如分区信息、存储引擎等）
     */
    private Map<String, Object> extraProperties;
    
    /**
     * 字段信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ColumnInfo {
        /**
         * 字段名（英文）
         */
        private String columnName;
        
        /**
         * 字段的中文描述
         */
        private String columnDescription;
        
        /**
         * 业务含义说明
         */
        private String businessMeaning;
        
        /**
         * 数据类型
         */
        private String dataType;
        
        /**
         * 是否为主键
         */
        private Boolean isPrimaryKey;
        
        /**
         * 是否允许为NULL
         */
        private Boolean isNullable;
        
        /**
         * 默认值
         */
        private String defaultValue;
        
        /**
         * 字段长度/精度
         */
        private Integer maxLength;
        
        /**
         * 数值精度（小数位数）
         */
        private Integer scale;
        
        /**
         * 是否自增
         */
        private Boolean isAutoIncrement;
        
        /**
         * 字段注释
         */
        private String comment;
        
        /**
         * 枚举值（如果是枚举类型）
         */
        private List<String> enumValues;
    }
    
    /**
     * 索引信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IndexInfo {
        /**
         * 索引名称
         */
        private String indexName;
        
        /**
         * 索引类型（PRIMARY, UNIQUE, INDEX等）
         */
        private String indexType;
        
        /**
         * 索引字段列表（按顺序）
         */
        private List<String> columns;
        
        /**
         * 是否唯一索引
         */
        private Boolean isUnique;
        
        /**
         * 索引注释
         */
        private String comment;
    }
    
    /**
     * 外键信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForeignKeyInfo {
        /**
         * 外键名称
         */
        private String constraintName;
        
        /**
         * 当前表字段
         */
        private String columnName;
        
        /**
         * 引用的表名
         */
        private String referencedTableName;
        
        /**
         * 引用的字段名
         */
        private String referencedColumnName;
        
        /**
         * 级联删除策略
         */
        private String onDelete;
        
        /**
         * 级联更新策略
         */
        private String onUpdate;
        
        /**
         * 关系描述（一对一、一对多等）
         */
        private String relationshipType;
        
        /**
         * 业务关系说明
         */
        private String relationshipDescription;
    }
    
    /**
     * 表统计信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TableStatistics {
        /**
         * 记录总数（估算）
         */
        private Long rowCount;
        
        /**
         * 表大小（字节）
         */
        private Long tableSize;
        
        /**
         * 索引大小（字节）
         */
        private Long indexSize;
        
        /**
         * 平均行长度
         */
        private Integer avgRowLength;
        
        /**
         * 最后更新时间
         */
        private Long lastUpdated;
        
        /**
         * 表的创建时间
         */
        private Long createTime;
    }
    
    /**
     * 生成用于向量化的文本描述
     * 将表的结构信息转换为适合向量化的文本格式
     * 
     * @return 结构化的文本描述
     */
    public String generateVectorizationText() {
        StringBuilder text = new StringBuilder();
        
        // 表基本信息
        text.append("表名: ").append(tableName);
        if (tableDescription != null && !tableDescription.isEmpty()) {
            text.append(" (").append(tableDescription).append(")");
        }
        text.append("\n");
        
        if (businessDescription != null && !businessDescription.isEmpty()) {
            text.append("业务用途: ").append(businessDescription).append("\n");
        }
        
        // 字段信息
        text.append("字段列表:\n");
        if (columns != null) {
            for (ColumnInfo column : columns) {
                text.append("- ").append(column.columnName)
                    .append(" (").append(column.dataType).append(")");
                
                if (column.columnDescription != null && !column.columnDescription.isEmpty()) {
                    text.append(": ").append(column.columnDescription);
                }
                
                if (Boolean.TRUE.equals(column.isPrimaryKey)) {
                    text.append(" [主键]");
                }
                
                if (column.businessMeaning != null && !column.businessMeaning.isEmpty()) {
                    text.append(" - ").append(column.businessMeaning);
                }
                
                text.append("\n");
            }
        }
        
        // 关系信息
        if (foreignKeys != null && !foreignKeys.isEmpty()) {
            text.append("关联关系:\n");
            for (ForeignKeyInfo fk : foreignKeys) {
                text.append("- ").append(fk.columnName)
                    .append(" 关联到 ").append(fk.referencedTableName)
                    .append(".").append(fk.referencedColumnName);
                
                if (fk.relationshipDescription != null && !fk.relationshipDescription.isEmpty()) {
                    text.append(" (").append(fk.relationshipDescription).append(")");
                }
                text.append("\n");
            }
        }
        
        return text.toString();
    }
    
    /**
     * 生成简化的表结构描述（用于SQL生成提示）
     * 
     * @return 简化的表结构字符串
     */
    public String generateSqlHint() {
        StringBuilder hint = new StringBuilder();
        
        hint.append("表 ").append(tableName);
        if (tableDescription != null && !tableDescription.isEmpty()) {
            hint.append("(").append(tableDescription).append(")");
        }
        hint.append(":\n");
        
        if (columns != null) {
            for (ColumnInfo column : columns) {
                hint.append("  - ").append(column.columnName)
                    .append(" ").append(column.dataType);
                
                if (column.columnDescription != null && !column.columnDescription.isEmpty()) {
                    hint.append(" // ").append(column.columnDescription);
                }
                
                hint.append("\n");
            }
        }
        
        return hint.toString();
    }
}