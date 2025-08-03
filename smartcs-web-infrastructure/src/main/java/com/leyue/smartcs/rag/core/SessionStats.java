package com.leyue.smartcs.rag.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;

/**
 * 会话统计信息
 * 提供RAG会话的详细统计数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionStats {

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 总消息数
     */
    private Integer totalMessages;

    /**
     * 用户消息数
     */
    private Integer userMessages;

    /**
     * 助手消息数
     */
    private Integer assistantMessages;

    /**
     * 总Token使用量
     */
    private Integer totalTokens;

    /**
     * 输入Token使用量
     */
    private Integer inputTokens;

    /**
     * 输出Token使用量
     */
    private Integer outputTokens;

    /**
     * 平均响应时间（毫秒）
     */
    private Long averageResponseTime;

    /**
     * 最大响应时间（毫秒）
     */
    private Long maxResponseTime;

    /**
     * 最小响应时间（毫秒）
     */
    private Long minResponseTime;

    /**
     * 知识库查询次数
     */
    private Integer knowledgeQueries;

    /**
     * 工具调用次数
     */
    private Integer toolCalls;

    /**
     * MCP调用次数
     */
    private Integer mcpCalls;

    /**
     * 错误次数
     */
    private Integer errorCount;

    /**
     * 会话总时长
     */
    private Duration sessionDuration;

    /**
     * 内存使用量（字节）
     */
    private Long memoryUsage;

    /**
     * 缓存命中率
     */
    private Double cacheHitRate;
}