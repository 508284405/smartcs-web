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
import com.leyue.smartcs.mcp.OrderToolsService;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.WebMvcSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;

@Configuration
public class OrderMcpProvider {
    /* 订单MCP传输 */
    @Bean("orderTransport")
    WebMvcSseServerTransportProvider orderTransport(ObjectMapper mapper) {
        return new WebMvcSseServerTransportProvider(
                mapper, "/mcp/order/message", "/mcp/order/sse");
    }

    /* 订单MCP路由 */
    @Bean
    RouterFunction<ServerResponse> orderRouter(
            @Qualifier("orderTransport") WebMvcSseServerTransportProvider t) {
        return t.getRouterFunction();
    }

    /* 订单MCP服务 */
    @Bean("orderMcpServer")
    public McpSyncServer orderMcpServer(
            @Qualifier("orderTransport") WebMvcSseServerTransportProvider t,
            @Qualifier("orderTools") ToolCallbackProvider tools) {

        return McpServer.sync(t)
                .serverInfo("Order-MCP", "1.0")
                .capabilities(McpSchema.ServerCapabilities.builder().tools(true).build())
                .tools(McpToolUtils.toSyncToolSpecifications(tools.getToolCallbacks()))
                .build();
    }

    /* 订单工具 */
    @Bean("orderTools")
    public ToolCallbackProvider orderTools(OrderToolsService orderToolsService) {
        return MethodToolCallbackProvider.builder().toolObjects(orderToolsService).build();
    }
}
