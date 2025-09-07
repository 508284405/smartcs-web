package com.leyue.smartcs.rag.query.pipeline.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import com.leyue.smartcs.service.TracingSupport;

/**
 * 前缀补全服务初始化器
 * 负责在应用启动时初始化词典和预热缓存
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class PrefixCompletionInitializer {
    
    private final PrefixCompletionConfig config;
    private final PrefixCompletionRepository repository;
    private final PrefixCompletionCacheManager cacheManager;
    private final ResourceLoader resourceLoader;
    
    @Bean
    public ApplicationRunner initializePrefixCompletion() {
        return args -> {
            if (!config.isEnabled()) {
                log.info("Prefix completion service is disabled");
                return;
            }
            
            log.info("Initializing prefix completion service...");
            
            try {
                // 验证配置
                config.validate();
                
                // 异步初始化各个组件
                List<CompletableFuture<Void>> initTasks = Arrays.asList(
                    TracingSupport.runAsync(this::loadDefaultDictionaries),
                    TracingSupport.runAsync(this::loadFromSearchLogs),
                    TracingSupport.runAsync(this::warmupCache)
                );
                
                // 等待所有初始化任务完成
                CompletableFuture.allOf(initTasks.toArray(new CompletableFuture[0]))
                    .get();
                
                log.info("Prefix completion service initialized successfully");
                
            } catch (Exception e) {
                log.error("Failed to initialize prefix completion service", e);
            }
        };
    }
    
    /**
     * 加载默认词典
     */
    private void loadDefaultDictionaries() {
        try {
            Set<String> allWords = new HashSet<>();
            
            // 加载默认词典
            if (StringUtils.hasText(config.getDataSource().getDefaultDictionaryPath())) {
                List<String> defaultWords = loadDictionaryFromResource(
                    config.getDataSource().getDefaultDictionaryPath());
                allWords.addAll(defaultWords);
                log.info("Loaded {} words from default dictionary", defaultWords.size());
            }
            
            // 加载行业词典
            if (StringUtils.hasText(config.getDataSource().getIndustryDictionaryPath())) {
                List<String> industryWords = loadDictionaryFromResource(
                    config.getDataSource().getIndustryDictionaryPath());
                allWords.addAll(industryWords);
                log.info("Loaded {} words from industry dictionary", industryWords.size());
            }
            
            // 保存到存储
            if (!allWords.isEmpty()) {
                repository.saveWords(allWords);
                log.info("Saved {} unique words to repository", allWords.size());
            }
            
        } catch (Exception e) {
            log.error("Failed to load default dictionaries", e);
        }
    }
    
    /**
     * 从搜索日志学习词条
     */
    private void loadFromSearchLogs() {
        if (!config.getDataSource().isLearnFromSearchLogs()) {
            log.debug("Learning from search logs is disabled");
            return;
        }
        
        try {
            List<String> searchWords = repository.extractWordsFromSearchLogs(5000);
            if (!searchWords.isEmpty()) {
                repository.saveWords(searchWords);
                log.info("Learned {} words from search logs", searchWords.size());
            }
            
        } catch (Exception e) {
            log.error("Failed to load from search logs", e);
        }
    }
    
    /**
     * 预热缓存
     */
    private void warmupCache() {
        if (!config.isAutoWarmup()) {
            log.debug("Auto warmup is disabled");
            return;
        }
        
        try {
            // 获取热门前缀
            List<String> topPrefixes = getTopPrefixes(config.getWarmupPrefixCount());
            
            // 模拟预热数据
            Map<String, List<String>> warmupData = generateWarmupData(topPrefixes);
            
            if (!warmupData.isEmpty()) {
                cacheManager.warmup(warmupData);
                log.info("Cache warmed up with {} entries", warmupData.size());
            }
            
        } catch (Exception e) {
            log.error("Failed to warmup cache", e);
        }
    }
    
    /**
     * 从资源文件加载词典
     */
    private List<String> loadDictionaryFromResource(String resourcePath) {
        List<String> words = new ArrayList<>();
        
        try {
            Resource resource = resourceLoader.getResource(resourcePath);
            if (!resource.exists()) {
                log.warn("Dictionary resource not found: {}", resourcePath);
                return getBuiltinDictionary();
            }
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        words.add(line);
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("Failed to load dictionary from resource: " + resourcePath, e);
            return getBuiltinDictionary();
        }
        
        return words;
    }
    
    /**
     * 获取内置词典
     */
    private List<String> getBuiltinDictionary() {
        return Arrays.asList(
            // 通用词汇
            "搜索", "查询", "查找", "检索", "筛选",
            "用户", "客户", "账户", "订单", "产品",
            "服务", "支持", "帮助", "问题", "解决",
            "设置", "配置", "管理", "维护", "更新",
            
            // 技术词汇
            "系统", "平台", "应用", "软件", "程序",
            "数据", "信息", "文件", "报告", "统计",
            "接口", "API", "服务器", "数据库", "缓存",
            "监控", "日志", "错误", "异常", "调试",
            
            // 业务词汇
            "销售", "营销", "推广", "广告", "活动",
            "分析", "报表", "指标", "KPI", "ROI",
            "流程", "审批", "权限", "角色", "部门",
            "项目", "任务", "计划", "进度", "完成"
        );
    }
    
    /**
     * 获取热门前缀
     */
    private List<String> getTopPrefixes(int limit) {
        // 基于常用词汇生成前缀
        List<String> commonWords = getBuiltinDictionary();
        Set<String> prefixes = new HashSet<>();
        
        for (String word : commonWords) {
            if (word.length() >= 2) {
                for (int i = 1; i <= Math.min(word.length(), 4); i++) {
                    prefixes.add(word.substring(0, i));
                }
            }
        }
        
        return prefixes.stream()
            .sorted()
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    /**
     * 生成预热数据
     */
    private Map<String, List<String>> generateWarmupData(List<String> prefixes) {
        Map<String, List<String>> warmupData = new HashMap<>();
        List<String> dictionary = getBuiltinDictionary();
        
        for (String prefix : prefixes) {
            List<String> matches = dictionary.stream()
                .filter(word -> word.startsWith(prefix))
                .sorted()
                .limit(config.getDefaultLimit())
                .collect(Collectors.toList());
            
            if (!matches.isEmpty()) {
                String cacheKey = prefix + ":" + config.getDefaultLimit();
                warmupData.put(cacheKey, matches);
            }
        }
        
        return warmupData;
    }
}
