package com.leyue.smartcs.rag.query.pipeline.services;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 前缀补全服务配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "smartcs.rag.prefix-completion")
public class PrefixCompletionConfig {
    
    /**
     * 是否启用前缀补全功能
     */
    private boolean enabled = true;
    
    /**
     * 默认返回结果数量限制
     */
    private int defaultLimit = 10;
    
    /**
     * 最大返回结果数量限制
     */
    private int maxLimit = 50;
    
    /**
     * 最小前缀长度（小于此长度不触发补全）
     */
    private int minPrefixLength = 1;
    
    /**
     * 最大前缀长度（超过此长度不触发补全）
     */
    private int maxPrefixLength = 50;
    
    /**
     * 词条最大保存时长（小时）
     */
    private int maxWordAgeHours = 24 * 30; // 30天
    
    /**
     * 清理任务执行间隔（小时）
     */
    private int cleanupIntervalHours = 6;
    
    /**
     * 持久化任务执行间隔（分钟）
     */
    private int persistIntervalMinutes = 30;
    
    /**
     * 本地缓存最大大小
     */
    private int maxCacheSize = 10000;
    
    /**
     * 是否使用Redis缓存
     */
    private boolean useRedisCache = true;
    
    /**
     * 缓存过期时间（分钟）
     */
    private int cacheExpireMinutes = 60;
    
    /**
     * 是否启用智能排序
     */
    private boolean enableIntelligentRanking = true;
    
    /**
     * 是否启用个性化推荐
     */
    private boolean enablePersonalization = true;
    
    /**
     * 用户偏好学习率
     */
    private double learningRate = 0.1;
    
    /**
     * 时效性衰减因子
     */
    private double timeDecayFactor = 0.95;
    
    /**
     * 字符串相似度阈值
     */
    private double similarityThreshold = 0.6;
    
    /**
     * 预热缓存的热门前缀数量
     */
    private int warmupPrefixCount = 100;
    
    /**
     * 是否在启动时自动预热缓存
     */
    private boolean autoWarmup = true;
    
    /**
     * 数据源配置
     */
    private DataSource dataSource = new DataSource();
    
    /**
     * 排序策略配置
     */
    private RankingStrategy rankingStrategy = new RankingStrategy();
    
    @Data
    public static class DataSource {
        /**
         * 是否从知识库加载词条
         */
        private boolean loadFromKnowledgeBase = true;
        
        /**
         * 是否从搜索日志学习
         */
        private boolean learnFromSearchLogs = true;
        
        /**
         * 默认词典路径
         */
        private String defaultDictionaryPath = "classpath:dictionaries/default.txt";
        
        /**
         * 行业词典路径
         */
        private String industryDictionaryPath = "classpath:dictionaries/industry.txt";
        
        /**
         * 词条最小权重（低于此权重的词条将被过滤）
         */
        private double minWordWeight = 0.1;
        
        /**
         * 批量加载大小
         */
        private int batchSize = 1000;
    }
    
    @Data
    public static class RankingStrategy {
        /**
         * 基础权重占比
         */
        private double baseWeightRatio = 0.4;
        
        /**
         * 频率权重占比
         */
        private double frequencyWeightRatio = 0.3;
        
        /**
         * 时效性权重占比
         */
        private double timeWeightRatio = 0.2;
        
        /**
         * 个性化权重占比
         */
        private double personalizationWeightRatio = 0.1;
        
        /**
         * 是否启用长度惩罚（较短的词条得分更高）
         */
        private boolean enableLengthPenalty = true;
        
        /**
         * 长度惩罚系数
         */
        private double lengthPenaltyFactor = 0.05;
        
        /**
         * 是否启用编辑距离加权
         */
        private boolean enableEditDistanceBoost = true;
        
        /**
         * 编辑距离加权系数
         */
        private double editDistanceBoostFactor = 0.1;
    }
    
    /**
     * 验证配置参数的有效性
     */
    public void validate() {
        if (defaultLimit <= 0 || defaultLimit > maxLimit) {
            throw new IllegalArgumentException("Invalid defaultLimit: " + defaultLimit);
        }
        
        if (minPrefixLength < 0 || minPrefixLength > maxPrefixLength) {
            throw new IllegalArgumentException("Invalid prefix length range");
        }
        
        if (learningRate < 0 || learningRate > 1) {
            throw new IllegalArgumentException("Learning rate must be between 0 and 1");
        }
        
        if (timeDecayFactor < 0 || timeDecayFactor > 1) {
            throw new IllegalArgumentException("Time decay factor must be between 0 and 1");
        }
        
        // 验证排序策略权重总和
        RankingStrategy rs = getRankingStrategy();
        double totalWeight = rs.getBaseWeightRatio() + rs.getFrequencyWeightRatio() + 
                           rs.getTimeWeightRatio() + rs.getPersonalizationWeightRatio();
        if (Math.abs(totalWeight - 1.0) > 0.01) {
            throw new IllegalArgumentException("Ranking strategy weight ratios must sum to 1.0");
        }
    }
}