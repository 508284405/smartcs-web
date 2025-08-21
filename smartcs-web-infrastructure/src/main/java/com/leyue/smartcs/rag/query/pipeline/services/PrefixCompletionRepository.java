package com.leyue.smartcs.rag.query.pipeline.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 前缀补全数据持久化层
 * 支持MySQL存储和Redis缓存
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class PrefixCompletionRepository {
    
    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate redisTemplate;
    private final PrefixCompletionConfig config;
    
    private static final String WORDS_TABLE = "prefix_completion_words";
    private static final String USER_PREFS_TABLE = "prefix_completion_user_prefs";
    private static final String SEARCH_LOGS_TABLE = "search_logs";
    
    private static final String REDIS_WORDS_KEY = "pc:words";
    private static final String REDIS_USER_PREFS_KEY = "pc:user_prefs";
    private static final String REDIS_STATS_KEY = "pc:stats";
    
    /**
     * 加载所有词条
     */
    public List<String> loadAllWords() {
        try {
            // 首先尝试从Redis加载
            List<String> words = loadWordsFromRedis();
            if (!words.isEmpty()) {
                log.debug("Loaded {} words from Redis", words.size());
                return words;
            }
            
            // 从MySQL加载
            words = loadWordsFromDatabase();
            if (!words.isEmpty()) {
                // 缓存到Redis
                cacheWordsToRedis(words);
                log.info("Loaded {} words from database and cached to Redis", words.size());
            }
            
            return words;
            
        } catch (Exception e) {
            log.error("Failed to load words", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 保存词条到存储
     */
    public void saveWords(Collection<String> words) {
        if (words == null || words.isEmpty()) return;
        
        try {
            // 保存到数据库
            saveWordsToDatabase(words);
            
            // 更新Redis缓存
            cacheWordsToRedis(new ArrayList<>(words));
            
            log.info("Saved {} words to storage", words.size());
            
        } catch (Exception e) {
            log.error("Failed to save words", e);
        }
    }
    
    /**
     * 加载用户偏好
     */
    public Map<String, Double> loadUserPreferences() {
        try {
            // 首先尝试从Redis加载
            Map<String, Double> prefs = loadUserPreferencesFromRedis();
            if (!prefs.isEmpty()) {
                log.debug("Loaded {} user preferences from Redis", prefs.size());
                return prefs;
            }
            
            // 从MySQL加载
            prefs = loadUserPreferencesFromDatabase();
            if (!prefs.isEmpty()) {
                // 缓存到Redis
                cacheUserPreferencesToRedis(prefs);
                log.info("Loaded {} user preferences from database", prefs.size());
            }
            
            return prefs;
            
        } catch (Exception e) {
            log.error("Failed to load user preferences", e);
            return Collections.emptyMap();
        }
    }
    
    /**
     * 保存用户偏好
     */
    public void saveUserPreferences(Map<String, Double> preferences) {
        if (preferences == null || preferences.isEmpty()) return;
        
        try {
            // 保存到数据库
            saveUserPreferencesToDatabase(preferences);
            
            // 更新Redis缓存
            cacheUserPreferencesToRedis(preferences);
            
            log.debug("Saved {} user preferences to storage", preferences.size());
            
        } catch (Exception e) {
            log.error("Failed to save user preferences", e);
        }
    }
    
    /**
     * 从搜索日志中学习热门词条
     */
    public List<String> extractWordsFromSearchLogs(int limit) {
        try {
            String sql = """
                SELECT query_text, COUNT(*) as frequency
                FROM """ + SEARCH_LOGS_TABLE + """
                WHERE created_time > DATE_SUB(NOW(), INTERVAL 30 DAY)
                  AND LENGTH(query_text) >= ?
                  AND LENGTH(query_text) <= ?
                GROUP BY query_text
                HAVING frequency >= 3
                ORDER BY frequency DESC
                LIMIT ?
                """;
                
            return jdbcTemplate.query(sql, 
                (rs, rowNum) -> rs.getString("query_text"),
                config.getMinPrefixLength(),
                config.getMaxPrefixLength(),
                limit);
                
        } catch (Exception e) {
            log.error("Failed to extract words from search logs", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 获取统计信息
     */
    public Map<String, Object> getStorageStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 数据库统计
            Integer wordCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + WORDS_TABLE, Integer.class);
            Integer userPrefCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + USER_PREFS_TABLE, Integer.class);
                
            stats.put("db_word_count", wordCount != null ? wordCount : 0);
            stats.put("db_user_pref_count", userPrefCount != null ? userPrefCount : 0);
            
            // Redis统计
            if (redisTemplate.hasKey(REDIS_WORDS_KEY)) {
                Long redisWordCount = redisTemplate.opsForSet().size(REDIS_WORDS_KEY);
                stats.put("redis_word_count", redisWordCount != null ? redisWordCount : 0);
            }
            
        } catch (Exception e) {
            log.error("Failed to get storage stats", e);
        }
        
        return stats;
    }
    
    /**
     * 清理过期数据
     */
    public void cleanupExpiredData() {
        try {
            // 清理过期词条
            int deletedWords = jdbcTemplate.update(
                "DELETE FROM " + WORDS_TABLE + " WHERE last_accessed < DATE_SUB(NOW(), INTERVAL ? HOUR)",
                config.getMaxWordAgeHours());
                
            // 清理过期用户偏好
            int deletedPrefs = jdbcTemplate.update(
                "DELETE FROM " + USER_PREFS_TABLE + " WHERE updated_time < DATE_SUB(NOW(), INTERVAL ? DAY)",
                config.getMaxWordAgeHours() / 24);
                
            log.info("Cleaned up {} expired words and {} user preferences", deletedWords, deletedPrefs);
            
            // 清理Redis过期键
            cleanupRedisExpiredKeys();
            
        } catch (Exception e) {
            log.error("Failed to cleanup expired data", e);
        }
    }
    
    // ========== 私有方法 ==========
    
    private List<String> loadWordsFromRedis() {
        try {
            Set<String> wordSet = redisTemplate.opsForSet().members(REDIS_WORDS_KEY);
            return wordSet != null ? new ArrayList<>(wordSet) : Collections.emptyList();
        } catch (Exception e) {
            log.warn("Failed to load words from Redis", e);
            return Collections.emptyList();
        }
    }
    
    private List<String> loadWordsFromDatabase() {
        String sql = "SELECT word FROM " + WORDS_TABLE + " WHERE weight >= ? ORDER BY weight DESC";
        return jdbcTemplate.query(sql, 
            (rs, rowNum) -> rs.getString("word"),
            config.getDataSource().getMinWordWeight());
    }
    
    private void cacheWordsToRedis(List<String> words) {
        if (words.isEmpty()) return;
        
        try {
            // 清空现有缓存
            redisTemplate.delete(REDIS_WORDS_KEY);
            
            // 批量添加
            String[] wordsArray = words.toArray(new String[0]);
            redisTemplate.opsForSet().add(REDIS_WORDS_KEY, wordsArray);
            
            // 设置过期时间
            redisTemplate.expire(REDIS_WORDS_KEY, 24, TimeUnit.HOURS);
            
        } catch (Exception e) {
            log.warn("Failed to cache words to Redis", e);
        }
    }
    
    private void saveWordsToDatabase(Collection<String> words) {
        String sql = """
            INSERT INTO """ + WORDS_TABLE + """ (word, weight, created_time, last_accessed)
            VALUES (?, 1.0, NOW(), NOW())
            ON DUPLICATE KEY UPDATE last_accessed = NOW(), access_count = access_count + 1
            """;
            
        List<Object[]> batchArgs = new ArrayList<>();
        for (String word : words) {
            batchArgs.add(new Object[]{word});
        }
        
        jdbcTemplate.batchUpdate(sql, batchArgs);
    }
    
    private Map<String, Double> loadUserPreferencesFromRedis() {
        try {
            Map<Object, Object> prefs = redisTemplate.opsForHash().entries(REDIS_USER_PREFS_KEY);
            Map<String, Double> result = new HashMap<>();
            
            for (Map.Entry<Object, Object> entry : prefs.entrySet()) {
                if (entry.getKey() instanceof String && entry.getValue() instanceof String) {
                    try {
                        result.put((String) entry.getKey(), Double.parseDouble((String) entry.getValue()));
                    } catch (NumberFormatException e) {
                        log.warn("Invalid preference value: {}", entry.getValue());
                    }
                }
            }
            
            return result;
            
        } catch (Exception e) {
            log.warn("Failed to load user preferences from Redis", e);
            return Collections.emptyMap();
        }
    }
    
    private Map<String, Double> loadUserPreferencesFromDatabase() {
        String sql = "SELECT pref_key, pref_value FROM " + USER_PREFS_TABLE;
        Map<String, Double> result = new HashMap<>();
        
        jdbcTemplate.query(sql, rs -> {
            result.put(rs.getString("pref_key"), rs.getDouble("pref_value"));
        });
        
        return result;
    }
    
    private void cacheUserPreferencesToRedis(Map<String, Double> preferences) {
        if (preferences.isEmpty()) return;
        
        try {
            Map<String, String> stringPrefs = new HashMap<>();
            for (Map.Entry<String, Double> entry : preferences.entrySet()) {
                stringPrefs.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
            
            redisTemplate.opsForHash().putAll(REDIS_USER_PREFS_KEY, stringPrefs);
            redisTemplate.expire(REDIS_USER_PREFS_KEY, 12, TimeUnit.HOURS);
            
        } catch (Exception e) {
            log.warn("Failed to cache user preferences to Redis", e);
        }
    }
    
    private void saveUserPreferencesToDatabase(Map<String, Double> preferences) {
        String sql = """
            INSERT INTO """ + USER_PREFS_TABLE + """ (pref_key, pref_value, updated_time)
            VALUES (?, ?, NOW())
            ON DUPLICATE KEY UPDATE pref_value = VALUES(pref_value), updated_time = NOW()
            """;
            
        List<Object[]> batchArgs = new ArrayList<>();
        for (Map.Entry<String, Double> entry : preferences.entrySet()) {
            batchArgs.add(new Object[]{entry.getKey(), entry.getValue()});
        }
        
        jdbcTemplate.batchUpdate(sql, batchArgs);
    }
    
    private void cleanupRedisExpiredKeys() {
        try {
            Set<String> keys = redisTemplate.keys("pc:*");
            if (keys != null) {
                for (String key : keys) {
                    Long ttl = redisTemplate.getExpire(key);
                    if (ttl != null && ttl == -1) { // 没有设置过期时间的键
                        redisTemplate.expire(key, 24, TimeUnit.HOURS);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to cleanup Redis expired keys", e);
        }
    }
}