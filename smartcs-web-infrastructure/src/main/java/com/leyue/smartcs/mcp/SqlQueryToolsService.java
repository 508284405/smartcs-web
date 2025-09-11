package com.leyue.smartcs.mcp;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SQL查询工具服务 - 使用MCP协议提供SQL查询能力
 * 仅支持SELECT语句，确保数据安全
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SqlQueryToolsService {

    private final JdbcTemplate jdbcTemplate;
    
    // SQL注入防护：只允许SELECT语句
    private static final Pattern SELECT_PATTERN = Pattern.compile(
        "^\\s*SELECT\\s+.*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    // 危险关键词黑名单
    private static final String[] DANGEROUS_KEYWORDS = {
        "DROP", "DELETE", "INSERT", "UPDATE", "ALTER", "CREATE", "TRUNCATE", 
        "EXEC", "EXECUTE", "DECLARE", "UNION", "INFORMATION_SCHEMA"
    };

    @Tool(description = "执行SQL查询语句")
    public String executeSelectQuery(String sqlQuery) {
        try {
            log.info("执行SQL查询: {}", sqlQuery);
            
            // 验证SQL安全性
            if (!isSelectQuery(sqlQuery)) {
                return "错误：仅支持SELECT查询语句";
            }
            
            if (containsDangerousKeywords(sqlQuery)) {
                return "错误：SQL语句包含不安全的关键词";
            }
            
            // 执行查询
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sqlQuery);
            
            if (results.isEmpty()) {
                return "查询结果：无数据";
            }
            
            // 格式化结果
            StringBuilder sb = new StringBuilder();
            sb.append("查询结果（共").append(results.size()).append("条记录）：\n");
            
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
            
            log.info("SQL查询执行成功，返回{}条记录", results.size());
            return sb.toString();
            
        } catch (Exception e) {
            log.error("SQL查询执行失败: {}", e.getMessage(), e);
            return "查询执行失败: " + e.getMessage();
        }
    }

    @Tool(description = "获取数据库表结构信息")
    public String getTableSchema(String tableName) {
        try {
            log.info("查询表结构: {}", tableName);
            
            // 验证表名安全性
            if (!isValidTableName(tableName)) {
                return "错误：表名格式不正确";
            }
            
            String sql = "DESCRIBE " + tableName;
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
            
            if (results.isEmpty()) {
                return "表 " + tableName + " 不存在或无权限访问";
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("表 ").append(tableName).append(" 的结构：\n");
            sb.append("字段名 | 类型 | 是否为空 | 键 | 默认值 | 额外信息\n");
            sb.append("-".repeat(60)).append("\n");
            
            for (Map<String, Object> row : results) {
                sb.append(String.format("%s | %s | %s | %s | %s | %s\n",
                    row.get("Field"),
                    row.get("Type"),
                    row.get("Null"),
                    row.get("Key") != null ? row.get("Key") : "",
                    row.get("Default") != null ? row.get("Default") : "",
                    row.get("Extra") != null ? row.get("Extra") : ""
                ));
            }
            
            return sb.toString();
            
        } catch (Exception e) {
            log.error("查询表结构失败: {}", e.getMessage(), e);
            return "查询表结构失败: " + e.getMessage();
        }
    }

    @Tool(description = "获取数据库中的表列表")
    public String getTableList() {
        try {
            log.info("查询数据库表列表");
            
            String sql = "SHOW TABLES";
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
            
            if (results.isEmpty()) {
                return "当前数据库中没有表";
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("数据库中的表列表（共").append(results.size()).append("个表）：\n");
            
            for (Map<String, Object> row : results) {
                String tableName = row.values().iterator().next().toString();
                sb.append("- ").append(tableName).append("\n");
            }
            
            return sb.toString();
            
        } catch (Exception e) {
            log.error("查询表列表失败: {}", e.getMessage(), e);
            return "查询表列表失败: " + e.getMessage();
        }
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
    
    /**
     * 验证表名格式
     */
    private boolean isValidTableName(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) {
            return false;
        }
        // 只允许字母、数字和下划线
        return tableName.matches("^[a-zA-Z][a-zA-Z0-9_]*$");
    }
}