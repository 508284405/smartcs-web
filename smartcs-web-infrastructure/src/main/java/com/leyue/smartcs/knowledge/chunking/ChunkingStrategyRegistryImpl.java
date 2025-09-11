package com.leyue.smartcs.knowledge.chunking;

import com.leyue.smartcs.knowledge.enums.DocumentTypeEnum;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 分块策略注册器实现
 * 管理所有可用的分块策略，提供策略查找和管道创建功能
 */
@Component
@Slf4j
public class ChunkingStrategyRegistryImpl implements ChunkingStrategyRegistry {
    
    /**
     * 策略存储映射
     */
    private final Map<String, ChunkingStrategy> strategies = new ConcurrentHashMap<>();
    
    /**
     * 文档类型默认策略映射
     */
    private final Map<DocumentTypeEnum, List<String>> defaultStrategies = new ConcurrentHashMap<>();
    
    /**
     * 策略使用统计
     */
    private final Map<String, AtomicLong> strategyUsageStats = new ConcurrentHashMap<>();
    
    /**
     * 注册时间记录
     */
    private final Map<String, LocalDateTime> registrationTimes = new ConcurrentHashMap<>();
    
    /**
     * 自动注入所有策略实现
     */
    @Autowired
    private List<ChunkingStrategy> strategyBeans;
    
    /**
     * 初始化注册器，自动注册所有策略
     */
    @PostConstruct
    public void init() {
        log.info("开始初始化分块策略注册器");
        
        // 自动注册所有策略Bean
        if (strategyBeans != null) {
            for (ChunkingStrategy strategy : strategyBeans) {
                registerStrategy(strategy);
            }
        }
        
        // 初始化默认策略配置
        initializeDefaultStrategies();
        
        log.info("分块策略注册器初始化完成，注册了 {} 个策略", strategies.size());
    }
    
    @Override
    public void registerStrategy(ChunkingStrategy strategy) {
        if (strategy == null || strategy.getName() == null) {
            throw new IllegalArgumentException("策略及其名称不能为空");
        }
        
        String strategyName = strategy.getName();
        
        if (strategies.containsKey(strategyName)) {
            log.warn("策略 {} 已存在，将被覆盖", strategyName);
        }
        
        strategies.put(strategyName, strategy);
        strategyUsageStats.put(strategyName, new AtomicLong(0));
        registrationTimes.put(strategyName, LocalDateTime.now());
        
        log.info("注册分块策略: {} - {}", strategyName, strategy.getDescription());
    }
    
    @Override
    public void unregisterStrategy(String strategyName) {
        if (strategyName == null || strategyName.isEmpty()) {
            throw new IllegalArgumentException("策略名称不能为空");
        }
        
        ChunkingStrategy removed = strategies.remove(strategyName);
        if (removed != null) {
            strategyUsageStats.remove(strategyName);
            registrationTimes.remove(strategyName);
            log.info("注销分块策略: {}", strategyName);
        } else {
            log.warn("尝试注销不存在的策略: {}", strategyName);
        }
    }
    
    @Override
    public Optional<ChunkingStrategy> getStrategy(String strategyName) {
        if (strategyName == null || strategyName.isEmpty()) {
            return Optional.empty();
        }
        
        ChunkingStrategy strategy = strategies.get(strategyName);
        if (strategy != null) {
            // 更新使用统计
            strategyUsageStats.get(strategyName).incrementAndGet();
        }
        
        return Optional.ofNullable(strategy);
    }
    
    @Override
    public List<ChunkingStrategy> getStrategiesForDocumentType(DocumentTypeEnum documentType) {
        if (documentType == null) {
            return Collections.emptyList();
        }
        
        return strategies.values().stream()
                .filter(strategy -> strategy.getSupportedDocumentTypes().contains(documentType))
                .sorted(Comparator.comparingInt(ChunkingStrategy::getPriority))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ChunkingStrategy> getAllStrategies() {
        return new ArrayList<>(strategies.values());
    }
    
    @Override
    public List<String> getAllStrategyNames() {
        return new ArrayList<>(strategies.keySet());
    }
    
    @Override
    public boolean isRegistered(String strategyName) {
        return strategyName != null && strategies.containsKey(strategyName);
    }
    
    @Override
    public List<ChunkingStrategy> getDefaultStrategies(DocumentTypeEnum documentType) {
        if (documentType == null) {
            return Collections.emptyList();
        }
        
        List<String> defaultStrategyNames = defaultStrategies.get(documentType);
        if (defaultStrategyNames == null || defaultStrategyNames.isEmpty()) {
            // 如果没有预定义的默认策略，返回支持该文档类型的所有策略
            return getStrategiesForDocumentType(documentType);
        }
        
        return defaultStrategyNames.stream()
                .map(this::getStrategy)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
    
    @Override
    public ChunkingPipeline createPipeline(DocumentTypeChunkingConfig config) {
        if (config == null || !config.isValid()) {
            throw new IllegalArgumentException("分块配置无效");
        }
        
        List<ChunkingStrategy> pipelineStrategies = new ArrayList<>();
        
        // 根据配置创建策略列表
        for (DocumentTypeChunkingConfig.StrategyConfig strategyConfig : config.getEnabledStrategies()) {
            Optional<ChunkingStrategy> strategy = getStrategy(strategyConfig.getStrategyName());
            if (strategy.isPresent()) {
                pipelineStrategies.add(strategy.get());
            } else {
                log.warn("配置中指定的策略不存在: {}", strategyConfig.getStrategyName());
            }
        }
        
        if (pipelineStrategies.isEmpty()) {
            throw new IllegalStateException("没有找到有效的策略来构建管道");
        }
        
        // 创建管道
        ChunkingPipeline pipeline = ChunkingPipeline.builder()
                .strategies(pipelineStrategies)
                .documentType(config.getDocumentType())
                .config(config.getGlobalConfig())
                .enableParallel(config.isEnableParallel())
                .build();
        
        log.info("为文档类型 {} 创建了包含 {} 个策略的分块管道", 
                config.getDocumentType(), pipelineStrategies.size());
        
        return pipeline;
    }
    
    @Override
    public Map<String, Object> getStrategyStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalStrategies", strategies.size());
        stats.put("registrationTimes", new HashMap<>(registrationTimes));
        
        // 使用统计
        Map<String, Long> usageStats = strategyUsageStats.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().get()
                ));
        stats.put("usageStats", usageStats);
        
        // 文档类型支持统计
        Map<DocumentTypeEnum, Integer> documentTypeStats = new HashMap<>();
        for (DocumentTypeEnum docType : DocumentTypeEnum.values()) {
            int count = getStrategiesForDocumentType(docType).size();
            if (count > 0) {
                documentTypeStats.put(docType, count);
            }
        }
        stats.put("documentTypeSupport", documentTypeStats);
        
        return stats;
    }
    
    /**
     * 初始化默认策略配置
     * 为每种文档类型定义推荐的策略组合
     */
    private void initializeDefaultStrategies() {
        // PDF文档：图像处理 + 文本内容分块
        defaultStrategies.put(DocumentTypeEnum.PDF, 
                Arrays.asList("IMAGE_PROCESSING", "TEXT_CONTENT"));
        
        // Excel/CSV：表格处理
        defaultStrategies.put(DocumentTypeEnum.XLSX, 
                Arrays.asList("TABLE_PROCESSING"));
        defaultStrategies.put(DocumentTypeEnum.XLS, 
                Arrays.asList("TABLE_PROCESSING"));
        defaultStrategies.put(DocumentTypeEnum.CSV, 
                Arrays.asList("TABLE_PROCESSING"));
        
        // HTML文档：图像处理 + 表格处理 + 文本内容分块
        defaultStrategies.put(DocumentTypeEnum.HTML, 
                Arrays.asList("IMAGE_PROCESSING", "TABLE_PROCESSING", "TEXT_CONTENT"));
        
        // Markdown文档：图像处理 + 文本内容分块
        defaultStrategies.put(DocumentTypeEnum.MARKDOWN, 
                Arrays.asList("IMAGE_PROCESSING", "TEXT_CONTENT"));
        defaultStrategies.put(DocumentTypeEnum.MDX, 
                Arrays.asList("IMAGE_PROCESSING", "TEXT_CONTENT"));
        
        // Word文档：图像处理 + 文本内容分块
        defaultStrategies.put(DocumentTypeEnum.DOCX, 
                Arrays.asList("IMAGE_PROCESSING", "TEXT_CONTENT"));
        
        // 纯文本文档：仅文本内容分块
        defaultStrategies.put(DocumentTypeEnum.TXT, 
                Arrays.asList("TEXT_CONTENT"));
        
        // 其他格式使用文本内容分块
        defaultStrategies.put(DocumentTypeEnum.VTT, 
                Arrays.asList("TEXT_CONTENT"));
        defaultStrategies.put(DocumentTypeEnum.PROPERTIES, 
                Arrays.asList("TEXT_CONTENT"));
        
        log.info("默认策略配置初始化完成");
    }
    
    /**
     * 根据文档类型和配置创建推荐的配置
     */
    public DocumentTypeChunkingConfig createRecommendedConfig(DocumentTypeEnum documentType) {
        List<String> recommendedStrategies = defaultStrategies.get(documentType);
        if (recommendedStrategies == null) {
            recommendedStrategies = Arrays.asList("TEXT_CONTENT"); // 默认策略
        }
        
        List<DocumentTypeChunkingConfig.StrategyConfig> strategyConfigs = recommendedStrategies.stream()
                .map(strategyName -> DocumentTypeChunkingConfig.StrategyConfig.builder()
                        .strategyName(strategyName)
                        .enabled(true)
                        .weight(1.0)
                        .config(getDefaultConfigForStrategy(strategyName))
                        .build())
                .collect(Collectors.toList());
        
        return DocumentTypeChunkingConfig.builder()
                .documentType(documentType)
                .strategyConfigs(strategyConfigs)
                .globalConfig(getDefaultGlobalConfig())
                .enableParallel(false) // 默认串行处理
                .description("针对 " + documentType.name() + " 文档的推荐分块配置")
                .build();
    }
    
    /**
     * 获取策略的默认配置
     */
    private Map<String, Object> getDefaultConfigForStrategy(String strategyName) {
        Map<String, Object> config = new HashMap<>();
        
        switch (strategyName) {
            case "TEXT_CONTENT":
                config.put("chunkSize", 1000);
                config.put("overlapSize", 200);
                config.put("preserveSentences", true);
                break;
            case "TABLE_PROCESSING":
                config.put("maxRowsPerChunk", 50);
                config.put("preserveHeaders", true);
                config.put("extractTableContext", true);
                break;
            case "IMAGE_PROCESSING":
                config.put("enableOCR", false);
                config.put("enableImageDescription", false);
                config.put("extractImageMetadata", true);
                config.put("maxImageSize", 5 * 1024 * 1024); // 5MB
                break;
        }
        
        return config;
    }
    
    /**
     * 获取默认全局配置
     */
    private Map<String, Object> getDefaultGlobalConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("preserveMetadata", true);
        config.put("removeEmptyChunks", true);
        config.put("minChunkLength", 10);
        return config;
    }
}