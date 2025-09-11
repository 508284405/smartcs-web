package com.leyue.smartcs.rag.query.pipeline.services;

import java.util.List;
import java.util.Collection;

/**
 * 前缀补全服务接口
 * 提供智能前缀补全功能，支持多种补全策略和个性化推荐
 */
public interface IPrefixCompletionService {
    
    /**
     * 前缀补全
     * @param prefix 输入前缀
     * @param limit 最大返回数量
     * @return 补全结果列表，按相关性排序
     */
    List<String> complete(String prefix, int limit);
    
    /**
     * 上下文感知的前缀补全
     * @param prefix 输入前缀
     * @param context 上下文信息（如用户历史、当前对话等）
     * @param limit 最大返回数量
     * @return 补全结果列表
     */
    List<String> completeWithContext(String prefix, CompletionContext context, int limit);
    
    /**
     * 批量添加词条到词典
     * @param words 词条集合
     */
    void addWords(Collection<String> words);
    
    /**
     * 添加单个词条
     * @param word 词条
     * @param weight 权重（影响排序优先级）
     */
    void addWord(String word, double weight);
    
    /**
     * 更新词条权重（基于用户行为反馈）
     * @param word 词条
     * @param feedback 反馈分数（正数增加权重，负数降低权重）
     */
    void updateWordWeight(String word, double feedback);
    
    /**
     * 预热缓存（应用启动时调用）
     * @param topPrefixes 热门前缀列表
     */
    void warmupCache(Collection<String> topPrefixes);
    
    /**
     * 获取词典统计信息
     * @return 统计信息对象
     */
    DictionaryStats getStats();
    
    /**
     * 清理过期或低频词条
     */
    void cleanup();
    
    /**
     * 补全上下文信息
     */
    public static class CompletionContext {
        private final String userId;
        private final String sessionId;
        private final List<String> recentQueries;
        private final String domain; // 业务领域
        
        public CompletionContext(String userId, String sessionId, List<String> recentQueries, String domain) {
            this.userId = userId;
            this.sessionId = sessionId;
            this.recentQueries = recentQueries;
            this.domain = domain;
        }
        
        public String getUserId() { return userId; }
        public String getSessionId() { return sessionId; }
        public List<String> getRecentQueries() { return recentQueries; }
        public String getDomain() { return domain; }
    }
    
    /**
     * 词典统计信息
     */
    public static class DictionaryStats {
        private final int totalWords;
        private final int totalNodes;
        private final long memoryUsage;
        private final long cacheHitRate;
        
        public DictionaryStats(int totalWords, int totalNodes, long memoryUsage, long cacheHitRate) {
            this.totalWords = totalWords;
            this.totalNodes = totalNodes;
            this.memoryUsage = memoryUsage;
            this.cacheHitRate = cacheHitRate;
        }
        
        public int getTotalWords() { return totalWords; }
        public int getTotalNodes() { return totalNodes; }
        public long getMemoryUsage() { return memoryUsage; }
        public long getCacheHitRate() { return cacheHitRate; }
    }
}