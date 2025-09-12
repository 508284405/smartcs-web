package com.leyue.smartcs.ltm.service;

import com.leyue.smartcs.domain.ltm.entity.EpisodicMemory;
import com.leyue.smartcs.domain.ltm.entity.SemanticMemory;
import com.leyue.smartcs.domain.ltm.entity.ProceduralMemory;
import com.leyue.smartcs.domain.ltm.gateway.EpisodicMemoryGateway;
import com.leyue.smartcs.domain.ltm.gateway.SemanticMemoryGateway;
import com.leyue.smartcs.domain.ltm.gateway.ProceduralMemoryGateway;
import com.leyue.smartcs.domain.ltm.domainservice.LTMDomainService.MemoryFormationRequest;

import dev.langchain4j.model.language.LanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.data.embedding.Embedding;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

/**
 * 记忆形成服务
 * 负责将对话内容转换为不同类型的长期记忆
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MemoryFormationService {

    private final EpisodicMemoryGateway episodicMemoryGateway;
    private final SemanticMemoryGateway semanticMemoryGateway;
    private final ProceduralMemoryGateway proceduralMemoryGateway;
    private final LanguageModel languageModel;
    private final EmbeddingModel embeddingModel;
    private final MemoryAnalyzer memoryAnalyzer;

    @Value("${smartcs.ai.ltm.formation.importance-threshold:0.5}")
    private double importanceThreshold;

    @Value("${smartcs.ai.ltm.formation.semantic-extraction.enabled:true}")
    private boolean semanticExtractionEnabled;

    @Value("${smartcs.ai.ltm.formation.procedural-learning.enabled:true}")
    private boolean proceduralLearningEnabled;

    @Value("${smartcs.ai.ltm.formation.async.enabled:true}")
    private boolean asyncFormationEnabled;

    /**
     * 处理记忆形成请求
     */
    public void processMemoryFormation(MemoryFormationRequest request) {
        log.debug("开始处理记忆形成: userId={}, sessionId={}", request.getUserId(), request.getSessionId());

        if (asyncFormationEnabled) {
            // 异步处理以避免阻塞主流程
            CompletableFuture.runAsync(() -> doProcessMemoryFormation(request))
                .exceptionally(throwable -> {
                    log.error("记忆形成处理异常: userId={}, error={}", request.getUserId(), throwable.getMessage());
                    return null;
                });
        } else {
            doProcessMemoryFormation(request);
        }
    }

    /**
     * 执行记忆形成处理
     */
    private void doProcessMemoryFormation(MemoryFormationRequest request) {
        try {
            // 0. 简单去重：最近若干条内容有高度相同文本则跳过
            if (isDuplicateContent(request.getUserId(), request.getContent())) {
                log.debug("检测到重复内容，跳过记忆形成: userId={}", request.getUserId());
                return;
            }

            // 1. 分析内容重要性
            double importance = memoryAnalyzer.analyzeImportance(request.getContent(), request.getContext());
            
            if (importance < importanceThreshold) {
                log.debug("内容重要性不足，跳过记忆形成: userId={}, importance={}", 
                         request.getUserId(), importance);
                return;
            }

            // 2. 形成情景记忆
            EpisodicMemory episodicMemory = createEpisodicMemory(request, importance);
            episodicMemoryGateway.save(episodicMemory);
            log.debug("成功创建情景记忆: userId={}, episodeId={}", 
                     request.getUserId(), episodicMemory.getEpisodeId());

            // 3. 尝试提取语义记忆
            if (semanticExtractionEnabled && importance >= 0.7) {
                extractSemanticMemories(request, episodicMemory);
            }

            // 4. 尝试学习程序性记忆
            if (proceduralLearningEnabled) {
                learnProceduralMemories(request, episodicMemory);
            }

        } catch (Exception e) {
            log.error("记忆形成处理失败: userId={}, error={}", request.getUserId(), e.getMessage(), e);
        }
    }

    /**
     * 基于最近访问的记忆进行轻量级文本去重
     */
    private boolean isDuplicateContent(Long userId, String content) {
        if (userId == null || content == null || content.isBlank()) return false;
        try {
            String norm = normalize(content);
            // 查询最近访问的若干条记忆（如果网关实现可用）
            java.util.List<EpisodicMemory> recents = episodicMemoryGateway.findRecentlyAccessed(userId, 5);
            for (EpisodicMemory m : recents) {
                String mc = m.getContent();
                if (mc == null) continue;
                if (normalize(mc).equals(norm)) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.debug("去重检查失败，忽略并继续: {}", e.getMessage());
        }
        return false;
    }

    private String normalize(String s) {
        return s.replaceAll("\\s+", " ")
                .replaceAll("[，。！？,.!?]", "")
                .trim()
                .toLowerCase();
    }

    /**
     * 创建情景记忆
     */
    private EpisodicMemory createEpisodicMemory(MemoryFormationRequest request, double importance) {
        // 生成向量嵌入
        byte[] embeddingVector = generateEmbedding(request.getContent());

        // 构建上下文元数据
        Map<String, Object> contextMetadata = new HashMap<>(request.getContext());
        contextMetadata.put("formation_timestamp", System.currentTimeMillis());
        contextMetadata.put("content_length", request.getContent().length());

        return EpisodicMemory.builder()
            .userId(request.getUserId())
            .sessionId(request.getSessionId())
            .episodeId(generateEpisodeId())
            .content(request.getContent())
            .embeddingVector(embeddingVector)
            .contextMetadata(contextMetadata)
            .timestamp(request.getTimestamp())
            .importanceScore(importance)
            .accessCount(0)
            .consolidationStatus(0) // 新记忆
            .createdAt(System.currentTimeMillis())
            .updatedAt(System.currentTimeMillis())
            .build();
    }

    /**
     * 提取语义记忆
     */
    private void extractSemanticMemories(MemoryFormationRequest request, EpisodicMemory episodicMemory) {
        try {
            log.debug("开始提取语义记忆: userId={}, episodeId={}", 
                     request.getUserId(), episodicMemory.getEpisodeId());

            // 使用LLM提取概念和知识
            String extractionPrompt = buildSemanticExtractionPrompt(request.getContent());
            String extractionResult = languageModel.generate(extractionPrompt);

            // 解析提取结果
            Map<String, String> extractedConcepts = parseExtractionResult(extractionResult);

            for (Map.Entry<String, String> entry : extractedConcepts.entrySet()) {
                String concept = entry.getKey();
                String knowledge = entry.getValue();

                // 检查是否已存在相同概念的语义记忆
                var existingMemory = semanticMemoryGateway.findByUserIdAndConcept(request.getUserId(), concept);
                
                if (existingMemory.isPresent()) {
                    // 更新现有记忆
                    SemanticMemory memory = existingMemory.get();
                    memory.addEvidence(episodicMemory.getEpisodeId());
                    semanticMemoryGateway.update(memory);
                    log.debug("更新现有语义记忆: concept={}", concept);
                } else {
                    // 创建新的语义记忆
                    SemanticMemory newMemory = createSemanticMemory(
                        request.getUserId(), concept, knowledge, episodicMemory.getEpisodeId());
                    semanticMemoryGateway.save(newMemory);
                    log.debug("创建新语义记忆: concept={}", concept);
                }
            }

        } catch (Exception e) {
            log.warn("语义记忆提取失败: userId={}, error={}", request.getUserId(), e.getMessage());
        }
    }

    /**
     * 学习程序性记忆
     */
    private void learnProceduralMemories(MemoryFormationRequest request, EpisodicMemory episodicMemory) {
        try {
            log.debug("开始学习程序性记忆: userId={}, episodeId={}", 
                     request.getUserId(), episodicMemory.getEpisodeId());

            // 分析用户行为模式
            Map<String, Object> behaviorPatterns = memoryAnalyzer.analyzeBehaviorPatterns(
                request.getContent(), request.getContext());

            for (Map.Entry<String, Object> entry : behaviorPatterns.entrySet()) {
                String patternType = entry.getKey();
                @SuppressWarnings("unchecked")
                Map<String, Object> patternData = (Map<String, Object>) entry.getValue();

                String patternName = (String) patternData.get("name");
                String patternDescription = (String) patternData.get("description");
                @SuppressWarnings("unchecked")
                Map<String, Object> triggerConditions = (Map<String, Object>) patternData.get("triggers");

                // 查找或创建程序性记忆
                var existingPattern = proceduralMemoryGateway.findByUserIdAndPatternTypeAndName(
                    request.getUserId(), patternType, patternName);

                if (existingPattern.isPresent()) {
                    // 强化现有模式
                    ProceduralMemory pattern = existingPattern.get();
                    pattern.recordSuccess(); // 假设观察到成功执行
                    proceduralMemoryGateway.update(pattern);
                    log.debug("强化现有程序性记忆: patternType={}, patternName={}", 
                             patternType, patternName);
                } else {
                    // 创建新的程序性记忆
                    ProceduralMemory newPattern = createProceduralMemory(
                        request.getUserId(), patternType, patternName, 
                        patternDescription, triggerConditions);
                    proceduralMemoryGateway.save(newPattern);
                    log.debug("创建新程序性记忆: patternType={}, patternName={}", 
                             patternType, patternName);
                }
            }

        } catch (Exception e) {
            log.warn("程序性记忆学习失败: userId={}, error={}", request.getUserId(), e.getMessage());
        }
    }

    /**
     * 创建语义记忆
     */
    private SemanticMemory createSemanticMemory(Long userId, String concept, String knowledge, String sourceEpisodeId) {
        byte[] embeddingVector = generateEmbedding(concept + " " + knowledge);

        return SemanticMemory.builder()
            .userId(userId)
            .concept(concept)
            .knowledge(knowledge)
            .embeddingVector(embeddingVector)
            .confidence(0.7) // 初始置信度
            .sourceEpisodes(java.util.Arrays.asList(sourceEpisodeId))
            .evidenceCount(1)
            .contradictionCount(0)
            .lastReinforcedAt(System.currentTimeMillis())
            .decayRate(0.01) // 默认衰减率
            .createdAt(System.currentTimeMillis())
            .updatedAt(System.currentTimeMillis())
            .build();
    }

    /**
     * 创建程序性记忆
     */
    private ProceduralMemory createProceduralMemory(Long userId, String patternType, String patternName,
                                                   String patternDescription, Map<String, Object> triggerConditions) {
        return ProceduralMemory.builder()
            .userId(userId)
            .patternType(patternType)
            .patternName(patternName)
            .patternDescription(patternDescription)
            .triggerConditions(triggerConditions)
            .actionTemplate("") // 可后续完善
            .successCount(1) // 初始成功次数
            .failureCount(0)
            .successRate(1.0) // 初始成功率
            .lastTriggeredAt(System.currentTimeMillis())
            .learningRate(0.1) // 默认学习率
            .isActive(true)
            .createdAt(System.currentTimeMillis())
            .updatedAt(System.currentTimeMillis())
            .build();
    }

    /**
     * 生成向量嵌入
     */
    private byte[] generateEmbedding(String text) {
        try {
            Embedding embedding = embeddingModel.embed(text).content();
            // 将float数组转换为byte数组
            float[] vector = embedding.vector();
            byte[] bytes = new byte[vector.length * 4]; // float是4字节
            
            java.nio.ByteBuffer buffer = java.nio.ByteBuffer.wrap(bytes);
            for (float f : vector) {
                buffer.putFloat(f);
            }
            
            return bytes;
        } catch (Exception e) {
            log.warn("生成向量嵌入失败: {}", e.getMessage());
            return new byte[0];
        }
    }

    /**
     * 生成情节ID
     */
    private String generateEpisodeId() {
        return "ep_" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 构建语义提取提示
     */
    private String buildSemanticExtractionPrompt(String content) {
        return """
            请从以下对话内容中提取用户相关的知识和概念。
            
            对话内容：
            %s
            
            请以以下格式返回提取的概念和知识（每行一个概念）：
            概念名称::知识描述
            
            例如：
            用户偏好::用户喜欢简洁的回答
            技术专长::用户熟悉Java编程语言
            
            只提取明确的、有价值的知识，避免过度解读。如果没有明确的概念，请返回"无"。
            """.formatted(content);
    }

    /**
     * 解析提取结果
     */
    private Map<String, String> parseExtractionResult(String result) {
        Map<String, String> concepts = new HashMap<>();
        
        if (result == null || result.trim().equals("无")) {
            return concepts;
        }

        String[] lines = result.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.contains("::")) {
                String[] parts = line.split("::", 2);
                if (parts.length == 2) {
                    String concept = parts[0].trim();
                    String knowledge = parts[1].trim();
                    if (!concept.isEmpty() && !knowledge.isEmpty()) {
                        concepts.put(concept, knowledge);
                    }
                }
            }
        }

        return concepts;
    }
}
