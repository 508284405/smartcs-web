package com.leyue.smartcs.mcp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyue.smartcs.mcp.SqlQueryToolsService;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.WebMvcSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.McpToolUtils;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * SQL查询MCP服务器配置
 * 使用Spring AI MCP框架实现SQL查询服务器
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SqlMcpServerConfiguration {

    private final SqlQueryToolsService sqlQueryToolsService;

    /**
     * SQL查询MCP传输配置
     */
    @Bean("sqlQueryTransport")
    public WebMvcSseServerTransportProvider sqlQueryTransport(ObjectMapper mapper) {
        log.info("初始化SQL查询MCP传输配置");
        return new WebMvcSseServerTransportProvider(
                mapper, "/mcp/sql/message", "/mcp/sql/sse");
    }

    /**
     * SQL查询MCP路由配置
     */
    @Bean
    public RouterFunction<ServerResponse> sqlQueryRouter(
            @Qualifier("sqlQueryTransport") WebMvcSseServerTransportProvider transport) {
        log.info("初始化SQL查询MCP路由配置");
        return transport.getRouterFunction();
    }

    /**
     * SQL查询工具回调提供者
     */
    @Bean("sqlQueryTools")
    public ToolCallbackProvider sqlQueryTools() {
        log.info("初始化SQL查询工具回调提供者");
        return MethodToolCallbackProvider.builder()
                .toolObjects(sqlQueryToolsService)
                .build();
    }

    /**
     * SQL查询MCP服务器
     */
    @Bean("sqlQueryMcpServer")
    public McpSyncServer sqlQueryMcpServer(
            @Qualifier("sqlQueryTransport") WebMvcSseServerTransportProvider transport,
            @Qualifier("sqlQueryTools") ToolCallbackProvider tools) {
        
        log.info("初始化SQL查询MCP服务器");
        
        return McpServer.sync(transport)
                .serverInfo("SQL-Query-MCP", "1.0")
                .capabilities(McpSchema.ServerCapabilities.builder()
                        .tools(true)
                        .build())
                .tools(McpToolUtils.toSyncToolSpecifications(tools.getToolCallbacks()))
                .build();
    }
}