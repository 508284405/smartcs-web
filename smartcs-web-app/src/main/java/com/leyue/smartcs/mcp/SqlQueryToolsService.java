package com.leyue.smartcs.mcp;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SqlQueryToolsService {

    private final JdbcTemplate jdbcTemplate;

    @Tool(description = "执行SQL查询语句，返回多条记录")
    public SingleResponse<List<Map<String, Object>>> executeQuery(String sql, ToolContext toolContext) {
        try {
            log.info("工具名称{}，执行SQL查询，SQL语句: {}", "executeQuery", sql);
            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
            return SingleResponse.of(result);
        } catch (Exception e) {
            log.error("SQL查询执行失败，SQL语句: {}, 错误信息: {}", sql, e.getMessage(), e);
            return SingleResponse.buildFailure("SQL_QUERY_ERROR", "SQL查询执行失败: " + e.getMessage());
        }
    }

    @Tool(description = "执行SQL查询语句，返回单条记录")
    public SingleResponse<Map<String, Object>> queryForSingleRecord(String sql, ToolContext toolContext) {
        try {
            log.info("工具名称{}，执行SQL查询单条记录，SQL语句: {}", "queryForSingleRecord", sql);
            Map<String, Object> result = jdbcTemplate.queryForMap(sql);
            return SingleResponse.of(result);
        } catch (Exception e) {
            log.error("SQL查询单条记录执行失败，SQL语句: {}, 错误信息: {}", sql, e.getMessage(), e);
            return SingleResponse.buildFailure("SQL_QUERY_SINGLE_ERROR", "SQL查询单条记录执行失败: " + e.getMessage());
        }
    }

    @Tool(description = "执行SQL查询语句，返回数量")
    public SingleResponse<Long> queryForCount(String sql, ToolContext toolContext) {
        try {
            log.info("工具名称{}，执行SQL查询数量，SQL语句: {}", "queryForCount", sql);
            Long result = jdbcTemplate.queryForObject(sql, Long.class);
            return SingleResponse.of(result);
        } catch (Exception e) {
            log.error("SQL查询数量执行失败，SQL语句: {}, 错误信息: {}", sql, e.getMessage(), e);
            return SingleResponse.buildFailure("SQL_QUERY_COUNT_ERROR", "SQL查询数量执行失败: " + e.getMessage());
        }
    }
} 