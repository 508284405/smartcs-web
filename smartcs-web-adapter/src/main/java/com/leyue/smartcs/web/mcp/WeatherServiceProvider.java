package com.leyue.smartcs.web.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyue.smartcs.mcp.WeatherToolsService;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.WebMvcSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * 天气工具提供者 - 已迁移到LangChain4j
 * 
 * 原MCP功能已迁移到LangChain4j的@Tool注解系统
 * 工具服务WeatherToolsService直接集成到LLMGatewayImpl的AI Services中
 */
@Configuration
public class WeatherServiceProvider {
    // MCP功能已迁移到LangChain4j工具系统
    // 工具通过LLMGatewayImpl.tools()集成
}