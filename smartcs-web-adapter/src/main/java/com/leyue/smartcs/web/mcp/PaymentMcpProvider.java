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
import com.leyue.smartcs.mcp.PaymentToolsService;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.WebMvcSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;

@Configuration
public class PaymentMcpProvider {

    /* 支付MCP传输 */
    @Bean("paymentTransport")
    WebMvcSseServerTransportProvider paymentTransport(ObjectMapper mapper) {
        return new WebMvcSseServerTransportProvider(
                mapper, "/mcp/pay/message", "/mcp/pay/sse");
    }

    /* 支付MCP路由 */
    @Bean
    RouterFunction<ServerResponse> paymentRouter(
            @Qualifier("paymentTransport") WebMvcSseServerTransportProvider t) {
        return t.getRouterFunction();
    }

    /* 支付MCP服务 */
    @Bean("paymentMcpServer")
    McpSyncServer paymentMcpServer(
            @Qualifier("paymentTransport") WebMvcSseServerTransportProvider t,
            @Qualifier("paymentTools") ToolCallbackProvider tools) {

        return McpServer.sync(t)
                .serverInfo("Payment-MCP", "2.0")
                .capabilities(McpSchema.ServerCapabilities.builder().tools(true).build())
                .tools(McpToolUtils.toSyncToolSpecifications(tools.getToolCallbacks()))
                .build();
    }

    /* 支付工具 */
    @Bean("paymentTools")
    ToolCallbackProvider paymentTools(PaymentToolsService paymentToolsService) {
        return MethodToolCallbackProvider.builder().toolObjects(paymentToolsService).build();
    }
}