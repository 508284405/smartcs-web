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
import com.leyue.smartcs.mcp.WeatherToolsService;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.WebMvcSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;

@Configuration
public class WeatherServiceProvider {

    /** 天气MCP传输 */
    @Bean("weatherTransport")
    WebMvcSseServerTransportProvider weatherTransport(ObjectMapper mapper) {
        return new WebMvcSseServerTransportProvider(
                mapper, "/mcp/weather/message", "/mcp/weather/sse");
    }

    /* RouterFunction 把 SSE/Message 端点挂到嵌入式 Tomcat */
    @Bean
    RouterFunction<ServerResponse> weatherRouter(
            @Qualifier("weatherTransport") WebMvcSseServerTransportProvider t) {
        return t.getRouterFunction();
    }

    /* 天气MCP服务 */
    @Bean("weatherMcpServer")
    McpSyncServer weatherMcpServer(
            @Qualifier("weatherTransport") WebMvcSseServerTransportProvider t,
            @Qualifier("weatherTools") ToolCallbackProvider tools) {

        return McpServer.sync(t)
                .serverInfo("Weather-MCP", "1.0")
                .capabilities(McpSchema.ServerCapabilities.builder().tools(true).build())
                .tools(McpToolUtils.toSyncToolSpecifications(tools.getToolCallbacks()))
                .build();
    }

    /* 天气工具 */
    @Bean("weatherTools")
    ToolCallbackProvider weatherTools(WeatherToolsService weatherToolsService) {
		return MethodToolCallbackProvider.builder().toolObjects(weatherToolsService).build();
	}
}