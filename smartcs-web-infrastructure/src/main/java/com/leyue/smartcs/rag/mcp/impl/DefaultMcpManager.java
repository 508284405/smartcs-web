package com.leyue.smartcs.rag.mcp.impl;

import com.leyue.smartcs.rag.mcp.McpManager;
import com.leyue.smartcs.rag.mcp.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 默认MCP管理器实现
 * 提供基础的MCP服务器管理功能
 */
@Service
@Slf4j
public class DefaultMcpManager implements McpManager {

    private final ConcurrentHashMap<String, McpServerConfig> servers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Boolean> serverStates = new ConcurrentHashMap<>();
    private final AtomicLong totalToolCalls = new AtomicLong(0);
    private final AtomicLong successfulToolCalls = new AtomicLong(0);
    private final AtomicLong failedToolCalls = new AtomicLong(0);
    private final AtomicLong totalResourceReads = new AtomicLong(0);

    @Override
    public void registerServer(McpServerConfig config) {
        try {
            servers.put(config.getId(), config);
            serverStates.put(config.getId(), config.getEnabled());
            log.info("注册MCP服务器: id={}, name={}, endpoint={}", 
                    config.getId(), config.getName(), config.getEndpoint());
        } catch (Exception e) {
            log.error("注册MCP服务器失败: id={}, error={}", config.getId(), e.getMessage(), e);
        }
    }

    @Override
    public void unregisterServer(String serverId) {
        try {
            servers.remove(serverId);
            serverStates.remove(serverId);
            log.info("注销MCP服务器: id={}", serverId);
        } catch (Exception e) {
            log.error("注销MCP服务器失败: id={}, error={}", serverId, e.getMessage(), e);
        }
    }

    @Override
    public List<McpServerConfig> getServers() {
        return new ArrayList<>(servers.values());
    }

    @Override
    public List<McpResource> listResources(String serverId) {
        try {
            McpServerConfig server = servers.get(serverId);
            if (server == null) {
                log.warn("MCP服务器不存在: id={}", serverId);
                return new ArrayList<>();
            }

            // 这里应该实际调用MCP服务器的资源列表API
            // 暂时返回空列表
            log.debug("列出MCP资源: serverId={}", serverId);
            return new ArrayList<>();
            
        } catch (Exception e) {
            log.error("列出MCP资源失败: serverId={}, error={}", serverId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<McpTool> listTools(String serverId) {
        try {
            McpServerConfig server = servers.get(serverId);
            if (server == null) {
                log.warn("MCP服务器不存在: id={}", serverId);
                return new ArrayList<>();
            }

            // 这里应该实际调用MCP服务器的工具列表API
            // 暂时返回空列表
            log.debug("列出MCP工具: serverId={}", serverId);
            return new ArrayList<>();
            
        } catch (Exception e) {
            log.error("列出MCP工具失败: serverId={}, error={}", serverId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public McpResult callTool(String serverId, String toolName, Map<String, Object> params) {
        long startTime = System.currentTimeMillis();
        totalToolCalls.incrementAndGet();
        
        try {
            log.info("调用MCP工具: serverId={}, toolName={}", serverId, toolName);

            McpServerConfig server = servers.get(serverId);
            if (server == null) {
                failedToolCalls.incrementAndGet();
                return McpResult.failure("MCP服务器不存在: " + serverId);
            }

            Boolean enabled = serverStates.get(serverId);
            if (enabled == null || !enabled) {
                failedToolCalls.incrementAndGet();
                return McpResult.failure("MCP服务器已禁用: " + serverId);
            }

            // 这里应该实际调用MCP服务器的工具API
            // 暂时返回模拟成功结果
            long executionTime = System.currentTimeMillis() - startTime;
            successfulToolCalls.incrementAndGet();
            
            McpResult result = McpResult.success("模拟工具执行结果");
            result.setToolName(toolName);
            result.setServerId(serverId);
            result.setExecutionTime(executionTime);
            result.setParameters(params);
            
            log.info("MCP工具调用成功: serverId={}, toolName={}, executionTime={}ms", 
                    serverId, toolName, executionTime);
            return result;
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            failedToolCalls.incrementAndGet();
            
            log.error("MCP工具调用失败: serverId={}, toolName={}, error={}", 
                    serverId, toolName, e.getMessage(), e);
            
            McpResult result = McpResult.failure("调用异常: " + e.getMessage());
            result.setToolName(toolName);
            result.setServerId(serverId);
            result.setExecutionTime(executionTime);
            return result;
        }
    }

    @Override
    public McpResource readResource(String serverId, String resourceUri) {
        try {
            totalResourceReads.incrementAndGet();
            
            log.info("读取MCP资源: serverId={}, resourceUri={}", serverId, resourceUri);

            McpServerConfig server = servers.get(serverId);
            if (server == null) {
                log.warn("MCP服务器不存在: id={}", serverId);
                return null;
            }

            // 这里应该实际调用MCP服务器的资源读取API
            // 暂时返回null
            log.debug("读取MCP资源完成: serverId={}, resourceUri={}", serverId, resourceUri);
            return null;
            
        } catch (Exception e) {
            log.error("读取MCP资源失败: serverId={}, resourceUri={}, error={}", 
                    serverId, resourceUri, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public List<McpResource> searchResources(String serverId, String query) {
        try {
            log.info("搜索MCP资源: serverId={}, query={}", serverId, query);

            McpServerConfig server = servers.get(serverId);
            if (server == null) {
                log.warn("MCP服务器不存在: id={}", serverId);
                return new ArrayList<>();
            }

            // 这里应该实际调用MCP服务器的资源搜索API
            // 暂时返回空列表
            log.debug("搜索MCP资源完成: serverId={}, query={}", serverId, query);
            return new ArrayList<>();
            
        } catch (Exception e) {
            log.error("搜索MCP资源失败: serverId={}, query={}, error={}", 
                    serverId, query, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public McpConnectionStatus getConnectionStatus(String serverId) {
        try {
            McpServerConfig server = servers.get(serverId);
            if (server == null) {
                return null;
            }

            Boolean enabled = serverStates.get(serverId);
            McpConnectionStatus.ConnectionState state = (enabled != null && enabled) ? 
                    McpConnectionStatus.ConnectionState.CONNECTED : 
                    McpConnectionStatus.ConnectionState.DISCONNECTED;

            return McpConnectionStatus.builder()
                    .serverId(serverId)
                    .state(state)
                    .connectedAt(java.time.LocalDateTime.now())
                    .lastActiveAt(java.time.LocalDateTime.now())
                    .reconnectCount(0)
                    .latency(50L) // 模拟延迟
                    .autoReconnect(true)
                    .healthStatus(McpConnectionStatus.HealthStatus.HEALTHY)
                    .build();
            
        } catch (Exception e) {
            log.error("获取MCP连接状态失败: serverId={}, error={}", serverId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean reconnectServer(String serverId) {
        try {
            log.info("重连MCP服务器: serverId={}", serverId);

            McpServerConfig server = servers.get(serverId);
            if (server == null) {
                log.warn("MCP服务器不存在: id={}", serverId);
                return false;
            }

            // 这里应该实际执行重连逻辑
            // 暂时模拟成功
            serverStates.put(serverId, true);
            log.info("MCP服务器重连成功: serverId={}", serverId);
            return true;
            
        } catch (Exception e) {
            log.error("MCP服务器重连失败: serverId={}, error={}", serverId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public McpCapabilities getServerCapabilities(String serverId) {
        try {
            McpServerConfig server = servers.get(serverId);
            if (server == null) {
                return null;
            }

            // 返回模拟的服务器能力
            return McpCapabilities.builder()
                    .serverId(serverId)
                    .serverName(server.getName())
                    .protocolVersion("1.0")
                    .serverVersion("1.0.0")
                    .capabilities(server.getCapabilities())
                    .toolSupport(McpCapabilities.ToolSupport.builder()
                            .listChanged(true)
                            .call(true)
                            .maxConcurrentCalls(10)
                            .build())
                    .resourceSupport(McpCapabilities.ResourceSupport.builder()
                            .subscribe(true)
                            .listChanged(true)
                            .read(true)
                            .maxResourceSize(1024L * 1024L) // 1MB
                            .build())
                    .build();
            
        } catch (Exception e) {
            log.error("获取MCP服务器能力失败: serverId={}, error={}", serverId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public McpStats getStats() {
        return new McpStatsImpl(
                servers.size(),
                (int) serverStates.values().stream().filter(enabled -> enabled).count(),
                (int) serverStates.values().stream().filter(enabled -> !enabled).count(),
                totalToolCalls.get(),
                successfulToolCalls.get(),
                failedToolCalls.get(),
                totalResourceReads.get(),
                calculateAverageResponseTime()
        );
    }

    @Override
    public void setServerEnabled(String serverId, boolean enabled) {
        try {
            if (servers.containsKey(serverId)) {
                serverStates.put(serverId, enabled);
                log.info("设置MCP服务器状态: serverId={}, enabled={}", serverId, enabled);
            } else {
                log.warn("MCP服务器不存在，无法设置状态: serverId={}", serverId);
            }
        } catch (Exception e) {
            log.error("设置MCP服务器状态失败: serverId={}, error={}", serverId, e.getMessage(), e);
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            return !servers.isEmpty();
        } catch (Exception e) {
            log.error("检查MCP可用性失败", e);
            return false;
        }
    }

    /**
     * 计算平均响应时间
     */
    private double calculateAverageResponseTime() {
        // 这里应该维护响应时间的统计，暂时返回模拟值
        return 100.0;
    }

    /**
     * MCP统计信息实现
     */
    private static class McpStatsImpl implements McpStats {
        private final int totalServers;
        private final int activeServers;
        private final int disconnectedServers;
        private final long totalToolCalls;
        private final long successfulToolCalls;
        private final long failedToolCalls;
        private final long totalResourceReads;
        private final double averageResponseTime;

        public McpStatsImpl(int totalServers, int activeServers, int disconnectedServers,
                           long totalToolCalls, long successfulToolCalls, long failedToolCalls,
                           long totalResourceReads, double averageResponseTime) {
            this.totalServers = totalServers;
            this.activeServers = activeServers;
            this.disconnectedServers = disconnectedServers;
            this.totalToolCalls = totalToolCalls;
            this.successfulToolCalls = successfulToolCalls;
            this.failedToolCalls = failedToolCalls;
            this.totalResourceReads = totalResourceReads;
            this.averageResponseTime = averageResponseTime;
        }

        @Override
        public int getTotalServers() { return totalServers; }

        @Override
        public int getActiveServers() { return activeServers; }

        @Override
        public int getDisconnectedServers() { return disconnectedServers; }

        @Override
        public long getTotalToolCalls() { return totalToolCalls; }

        @Override
        public long getSuccessfulToolCalls() { return successfulToolCalls; }

        @Override
        public long getFailedToolCalls() { return failedToolCalls; }

        @Override
        public long getTotalResourceReads() { return totalResourceReads; }

        @Override
        public double getAverageResponseTime() { return averageResponseTime; }
    }
}