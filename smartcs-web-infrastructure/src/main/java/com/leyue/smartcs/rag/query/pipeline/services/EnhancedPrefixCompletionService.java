package com.leyue.smartcs.rag.query.pipeline.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * 增强版前缀补全服务实现
 * 支持智能排序、缓存、持久化和个性化推荐
 */
@Slf4j
@Service
public class EnhancedPrefixCompletionService implements IPrefixCompletionService {
    
    private final CompressedTrieNode root = new CompressedTrieNode();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<String, List<String>> cache = new ConcurrentHashMap<>();
    private final Map<String, Double> userPreferences = new ConcurrentHashMap<>();
    private final PrefixCompletionRepository repository;
    private final StringRedisTemplate redisTemplate;
    private final ScheduledExecutorService scheduler;
    private final PrefixCompletionConfig config;
    
    // 统计信息
    private long cacheHits = 0;
    private long totalRequests = 0;
    
    public EnhancedPrefixCompletionService(PrefixCompletionRepository repository, 
                                         StringRedisTemplate redisTemplate,
                                         PrefixCompletionConfig config) {
        this.repository = repository;
        this.redisTemplate = redisTemplate;
        this.config = config;
        this.scheduler = Executors.newScheduledThreadPool(2);
        
        // 启动定期清理任务
        scheduler.scheduleAtFixedRate(this::cleanup, 1, config.getCleanupIntervalHours(), TimeUnit.HOURS);
        scheduler.scheduleAtFixedRate(this::persistToStorage, 10, config.getPersistIntervalMinutes(), TimeUnit.MINUTES);
        
        // 初始化数据
        initializeFromStorage();
    }
    
    @Override
    public List<String> complete(String prefix, int limit) {
        return completeWithContext(prefix, null, limit);
    }
    
    @Override
    public List<String> completeWithContext(String prefix, CompletionContext context, int limit) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        totalRequests++;
        prefix = prefix.trim().toLowerCase();
        
        // 检查缓存
        String cacheKey = buildCacheKey(prefix, context, limit);
        List<String> cached = getFromCache(cacheKey);
        if (cached != null) {
            cacheHits++;
            return cached;
        }
        
        List<String> results = performCompletion(prefix, context, limit);
        
        // 缓存结果
        putToCache(cacheKey, results);
        
        return results;
    }
    
    private List<String> performCompletion(String prefix, CompletionContext context, int limit) {
        lock.readLock().lock();
        try {
            List<CompressedTrieNode.CompletionResult> results = new ArrayList<>();
            
            // 从Trie树搜索
            CompressedTrieNode current = navigateToPrefix(prefix);
            if (current != null) {
                current.findCompletions(prefix, prefix, results, limit * 2); // 获取更多候选以便过滤
            }
            
            // 应用上下文权重和个性化
            return results.stream()
                    .map(r -> new ScoredResult(r.getText(), calculateFinalScore(r, context)))
                    .sorted(Comparator.comparingDouble(ScoredResult::getScore).reversed())
                    .limit(limit)
                    .map(ScoredResult::getText)
                    .collect(Collectors.toList());
                    
        } finally {
            lock.readLock().unlock();
        }
    }
    
    private CompressedTrieNode navigateToPrefix(String prefix) {
        CompressedTrieNode current = root;
        int index = 0;
        
        while (index < prefix.length()) {
            String key = String.valueOf(prefix.charAt(index));
            current = current.getChildren().get(key);
            
            if (current == null) return null;
            
            if (current.getCompressedPath() != null) {
                String remaining = prefix.substring(index + 1);
                if (!remaining.startsWith(current.getCompressedPath())) {
                    return null;
                }
                index += current.getCompressedPath().length() + 1;
            } else {
                index++;
            }
        }
        
        return current;
    }
    
    private double calculateFinalScore(CompressedTrieNode.CompletionResult result, CompletionContext context) {
        double baseScore = result.getScore();
        
        if (context == null) return baseScore;
        
        double contextBoost = 0.0;
        
        // 用户历史偏好
        if (context.getUserId() != null) {
            String userKey = "user_pref:" + context.getUserId() + ":" + result.getText();
            Double userScore = userPreferences.get(userKey);
            if (userScore != null) {
                contextBoost += userScore * 0.3;
            }
        }
        
        // 最近查询相似度
        if (context.getRecentQueries() != null) {
            for (String recent : context.getRecentQueries()) {
                double similarity = calculateStringSimilarity(result.getText(), recent);
                contextBoost += similarity * 0.2;
            }
        }
        
        // 领域相关性
        if (context.getDomain() != null) {
            String domainKey = "domain:" + context.getDomain() + ":" + result.getText();
            Double domainScore = userPreferences.get(domainKey);
            if (domainScore != null) {
                contextBoost += domainScore * 0.25;
            }
        }
        
        return baseScore + contextBoost;
    }
    
    private double calculateStringSimilarity(String str1, String str2) {
        if (str1 == null || str2 == null) return 0.0;
        
        str1 = str1.toLowerCase();
        str2 = str2.toLowerCase();
        
        if (str1.equals(str2)) return 1.0;
        if (str1.contains(str2) || str2.contains(str1)) return 0.8;
        
        // 简单的编辑距离相似度
        int maxLen = Math.max(str1.length(), str2.length());
        int distance = levenshteinDistance(str1, str2);
        return Math.max(0.0, 1.0 - (double) distance / maxLen);
    }
    
    private int levenshteinDistance(String str1, String str2) {
        int[][] dp = new int[str1.length() + 1][str2.length() + 1];
        
        for (int i = 0; i <= str1.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= str2.length(); j++) dp[0][j] = j;
        
        for (int i = 1; i <= str1.length(); i++) {
            for (int j = 1; j <= str2.length(); j++) {
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.min(Math.min(dp[i - 1][j], dp[i][j - 1]), dp[i - 1][j - 1]) + 1;
                }
            }
        }
        
        return dp[str1.length()][str2.length()];
    }
    
    @Override
    public void addWords(Collection<String> words) {
        if (words == null || words.isEmpty()) return;
        
        lock.writeLock().lock();
        try {
            for (String word : words) {
                if (word != null && !word.trim().isEmpty()) {
                    root.insert(word.trim().toLowerCase(), 1.0);
                }
            }
            clearCache();
        } finally {
            lock.writeLock().unlock();
        }
        
        log.info("Added {} words to prefix completion dictionary", words.size());
    }
    
    @Override
    public void addWord(String word, double weight) {
        if (word == null || word.trim().isEmpty()) return;
        
        lock.writeLock().lock();
        try {
            root.insert(word.trim().toLowerCase(), weight);
            clearCache();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public void updateWordWeight(String word, double feedback) {
        if (word == null || word.trim().isEmpty()) return;
        
        lock.writeLock().lock();
        try {
            boolean updated = root.updateWeight(word.trim().toLowerCase(), feedback);
            if (updated) {
                clearCache();
                log.debug("Updated weight for word: {}, feedback: {}", word, feedback);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public void warmupCache(Collection<String> topPrefixes) {
        if (topPrefixes == null || topPrefixes.isEmpty()) return;
        
        log.info("Starting cache warmup with {} prefixes", topPrefixes.size());
        
        for (String prefix : topPrefixes) {
            try {
                complete(prefix, config.getDefaultLimit());
            } catch (Exception e) {
                log.warn("Failed to warmup cache for prefix: {}", prefix, e);
            }
        }
        
        log.info("Cache warmup completed");
    }
    
    @Override
    public DictionaryStats getStats() {
        lock.readLock().lock();
        try {
            int totalWords = root.getWordCount();
            int totalNodes = root.getNodeCount();
            long memoryUsage = estimateMemoryUsage();
            long cacheHitRate = totalRequests > 0 ? (cacheHits * 100 / totalRequests) : 0;
            
            return new DictionaryStats(totalWords, totalNodes, memoryUsage, cacheHitRate);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public void cleanup() {
        lock.writeLock().lock();
        try {
            long maxAgeMs = config.getMaxWordAgeHours() * 60 * 60 * 1000;
            root.cleanup(maxAgeMs);
            
            // 清理缓存中过期的条目
            cache.entrySet().removeIf(entry -> Math.random() < 0.1); // 随机清理10%的缓存
            
            log.info("Cleanup completed. Current stats: {}", getStats());
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    private void initializeFromStorage() {
        try {
            List<String> words = repository.loadAllWords();
            if (!words.isEmpty()) {
                addWords(words);
                log.info("Loaded {} words from storage", words.size());
            }
            
            // 加载用户偏好
            Map<String, Double> preferences = repository.loadUserPreferences();
            userPreferences.putAll(preferences);
            log.info("Loaded {} user preferences from storage", preferences.size());
            
        } catch (Exception e) {
            log.error("Failed to initialize from storage", e);
        }
    }
    
    private void persistToStorage() {
        try {
            // 持久化用户偏好
            repository.saveUserPreferences(new HashMap<>(userPreferences));
            
            log.debug("Persisted data to storage");
        } catch (Exception e) {
            log.error("Failed to persist data to storage", e);
        }
    }
    
    private String buildCacheKey(String prefix, CompletionContext context, int limit) {
        StringBuilder key = new StringBuilder(prefix).append(":").append(limit);
        if (context != null) {
            if (context.getUserId() != null) key.append(":u:").append(context.getUserId());
            if (context.getDomain() != null) key.append(":d:").append(context.getDomain());
        }
        return key.toString();
    }
    
    private List<String> getFromCache(String key) {
        if (redisTemplate != null && config.isUseRedisCache()) {
            try {
                List<String> cached = redisTemplate.opsForList().range("pc:" + key, 0, -1);
                if (cached != null && !cached.isEmpty()) {
                    return cached;
                }
            } catch (Exception e) {
                log.warn("Failed to get from Redis cache", e);
            }
        }
        
        return cache.get(key);
    }
    
    private void putToCache(String key, List<String> results) {
        if (results == null || results.isEmpty()) return;
        
        // 本地缓存
        if (cache.size() < config.getMaxCacheSize()) {
            cache.put(key, new ArrayList<>(results));
        }
        
        // Redis缓存
        if (redisTemplate != null && config.isUseRedisCache()) {
            try {
                String redisKey = "pc:" + key;
                redisTemplate.opsForList().rightPushAll(redisKey, results);
                redisTemplate.expire(redisKey, config.getCacheExpireMinutes(), TimeUnit.MINUTES);
            } catch (Exception e) {
                log.warn("Failed to put to Redis cache", e);
            }
        }
    }
    
    private void clearCache() {
        cache.clear();
        
        if (redisTemplate != null && config.isUseRedisCache()) {
            try {
                Set<String> keys = redisTemplate.keys("pc:*");
                if (keys != null && !keys.isEmpty()) {
                    redisTemplate.delete(keys);
                }
            } catch (Exception e) {
                log.warn("Failed to clear Redis cache", e);
            }
        }
    }
    
    private long estimateMemoryUsage() {
        // 粗略估算内存使用量
        return root.getNodeCount() * 200L; // 假设每个节点占用200字节
    }
    
    /**
     * 内部类：评分结果
     */
    private static class ScoredResult {
        private final String text;
        private final double score;
        
        public ScoredResult(String text, double score) {
            this.text = text;
            this.score = score;
        }
        
        public String getText() { return text; }
        public double getScore() { return score; }
    }
}