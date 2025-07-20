package com.leyue.smartcs.web.mcp;

import org.springframework.ai.mcp.McpToolUtils;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyue.smartcs.mcp.SqlQueryToolsService;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.WebMvcSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;

@Configuration
public class SqlQueryMcpProvider {

    /* SQL查询MCP传输 */
    @Bean("sqlQueryTransport")
    WebMvcSseServerTransportProvider sqlQueryTransport(ObjectMapper mapper) {
        return new WebMvcSseServerTransportProvider(
                mapper, "/mcp/sql/message", "/mcp/sql/sse");
    }

    /* SQL查询MCP路由 */
    @Bean
    RouterFunction<ServerResponse> sqlQueryRouter(
            @Qualifier("sqlQueryTransport") WebMvcSseServerTransportProvider t) {
        return t.getRouterFunction();
    }

    /* SQL查询MCP服务 */
    @Bean("sqlQueryMcpServer")
    public McpSyncServer sqlQueryMcpServer(
            @Qualifier("sqlQueryTransport") WebMvcSseServerTransportProvider t,
            @Qualifier("sqlQueryTools") ToolCallbackProvider tools) {

        return McpServer.sync(t)
                .serverInfo("SQL-Query-MCP", "1.0")
                .capabilities(McpSchema.ServerCapabilities.builder().tools(true).build())
                .tools(McpToolUtils.toSyncToolSpecifications(tools.getToolCallbacks()))
                .build();
    }

    /* SQL查询工具 */
    @Bean("sqlQueryTools")
    public ToolCallbackProvider sqlQueryTools(SqlQueryToolsService sqlQueryToolsService) {
        return MethodToolCallbackProvider.builder().toolObjects(sqlQueryToolsService).build();
    }
} 