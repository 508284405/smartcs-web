package com.leyue.smartcs.rag.mcp;

import com.leyue.smartcs.rag.mcp.model.*;

import java.util.List;
import java.util.Map;

/**
 * MCP管理器接口
 * Model Context Protocol 协议支持，用于集成外部工具和资源
 */
public interface McpManager {

    /**
     * 注册MCP服务器
     * 
     * @param config 服务器配置
     */
    void registerServer(McpServerConfig config);

    /**
     * 注销MCP服务器
     * 
     * @param serverId 服务器ID
     */
    void unregisterServer(String serverId);

    /**
     * 获取服务器列表
     * 
     * @return 服务器配置列表
     */
    List<McpServerConfig> getServers();

    /**
     * 获取服务器资源列表
     * 
     * @param serverId 服务器ID
     * @return 资源列表
     */
    List<McpResource> listResources(String serverId);

    /**
     * 获取服务器工具列表
     * 
     * @param serverId 服务器ID
     * @return 工具列表
     */
    List<McpTool> listTools(String serverId);

    /**
     * 调用MCP工具
     * 
     * @param serverId 服务器ID
     * @param toolName 工具名称
     * @param params 参数
     * @return 调用结果
     */
    McpResult callTool(String serverId, String toolName, Map<String, Object> params);

    /**
     * 读取MCP资源
     * 
     * @param serverId 服务器ID
     * @param resourceUri 资源URI
     * @return 资源内容
     */
    McpResource readResource(String serverId, String resourceUri);

    /**
     * 搜索MCP资源
     * 
     * @param serverId 服务器ID
     * @param query 搜索查询
     * @return 搜索结果
     */
    List<McpResource> searchResources(String serverId, String query);

    /**
     * 检查服务器连接状态
     * 
     * @param serverId 服务器ID
     * @return 连接状态
     */
    McpConnectionStatus getConnectionStatus(String serverId);

    /**
     * 重新连接服务器
     * 
     * @param serverId 服务器ID
     * @return 是否成功
     */
    boolean reconnectServer(String serverId);

    /**
     * 获取服务器能力
     * 
     * @param serverId 服务器ID
     * @return 服务器能力
     */
    McpCapabilities getServerCapabilities(String serverId);

    /**
     * 获取MCP统计信息
     * 
     * @return 统计信息
     */
    McpStats getStats();

    /**
     * 启用/禁用服务器
     * 
     * @param serverId 服务器ID
     * @param enabled 是否启用
     */
    void setServerEnabled(String serverId, boolean enabled);

    /**
     * 检查MCP是否可用
     * 
     * @return 是否可用
     */
    boolean isAvailable();

    /**
     * MCP统计信息接口
     */
    interface McpStats {
        int getTotalServers();
        int getActiveServers();
        int getDisconnectedServers();
        long getTotalToolCalls();
        long getSuccessfulToolCalls();
        long getFailedToolCalls();
        long getTotalResourceReads();
        double getAverageResponseTime();
    }
}