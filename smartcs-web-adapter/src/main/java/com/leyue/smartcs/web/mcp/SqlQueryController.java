package com.leyue.smartcs.web.mcp;

import com.leyue.smartcs.mcp.SqlQueryToolsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * SQL查询MCP控制器
 * 提供SQL查询的REST API接口，用于测试MCP功能
 */
@Slf4j
@RestController
@RequestMapping("/api/mcp/sql")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SqlQueryController {

    private final SqlQueryToolsService sqlQueryToolsService;

    /**
     * 执行SQL查询
     */
    @PostMapping("/query")
    public String executeQuery(@RequestBody SqlQueryRequest request) {
        log.info("接收到SQL查询请求: {}", request.getSql());
        return sqlQueryToolsService.executeSelectQuery(request.getSql());
    }

    /**
     * 获取表结构
     */
    @GetMapping("/schema/{tableName}")
    public String getTableSchema(@PathVariable String tableName) {
        log.info("查询表结构: {}", tableName);
        return sqlQueryToolsService.getTableSchema(tableName);
    }

    /**
     * 获取表列表
     */
    @GetMapping("/tables")
    public String getTableList() {
        log.info("查询表列表");
        return sqlQueryToolsService.getTableList();
    }

    /**
     * SQL查询请求DTO
     */
    public static class SqlQueryRequest {
        private String sql;
        
        public String getSql() {
            return sql;
        }
        
        public void setSql(String sql) {
            this.sql = sql;
        }
    }
}