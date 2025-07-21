package com.leyue.smartcs.web.mcp;

import org.springframework.context.annotation.Configuration;

/**
 * 订单工具提供者 - 已迁移到LangChain4j
 * 
 * 原MCP功能已迁移到LangChain4j的@Tool注解系统
 * 工具服务OrderToolsService直接集成到LLMGatewayImpl的AI Services中
 * 
 * 如需MCP协议支持，请考虑：
 * 1. 使用Claude Desktop等支持LangChain4j工具的客户端
 * 2. 实现自定义MCP适配器
 * 3. 继续使用Spring AI的MCP实现（如果必要）
 */
@Configuration
public class OrderMcpProvider {
    // MCP功能已迁移到LangChain4j工具系统
    // 工具通过LLMGatewayImpl.tools()集成
}
