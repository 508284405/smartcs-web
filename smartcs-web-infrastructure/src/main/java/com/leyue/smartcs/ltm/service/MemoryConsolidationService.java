package com.leyue.smartcs.ltm.service;

import com.leyue.smartcs.domain.ltm.entity.EpisodicMemory;
import com.leyue.smartcs.domain.ltm.entity.SemanticMemory;
import com.leyue.smartcs.domain.ltm.entity.ProceduralMemory;
import com.leyue.smartcs.domain.ltm.gateway.EpisodicMemoryGateway;
import com.leyue.smartcs.domain.ltm.gateway.SemanticMemoryGateway;
import com.leyue.smartcs.domain.ltm.gateway.ProceduralMemoryGateway;
import com.leyue.smartcs.ltm.config.MemoryConsolidationProperties;

import dev.langchain4j.model.language.LanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.data.embedding.Embedding;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;

/**
 * 记忆巩固服务
 * 负责将重要的情景记忆转换为语义记忆和程序性记忆
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MemoryConsolidationService {

    private final EpisodicMemoryGateway episodicMemoryGateway;
    private final SemanticMemoryGateway semanticMemoryGateway;
    private final ProceduralMemoryGateway proceduralMemoryGateway;
    private final LanguageModel languageModel;
    private final EmbeddingModel embeddingModel;
    private final MemoryConsolidationProperties consolidationProperties;

    /**
     * 为特定用户执行记忆巩固
     */
    @Async("ltmTaskExecutor")
    public CompletableFuture<Void> consolidateUserMemoriesAsync(Long userId) {
        consolidateUserMemories(userId);
        return CompletableFuture.completedFuture(null);
    }

    /**
     * 巩固用户记忆
     */
    public void consolidateUserMemories(Long userId) {
        log.debug("开始巩固用户记忆: userId={}", userId);

        try {
            if (!consolidationProperties.isEnabled()) {
                log.debug("记忆巩固已禁用，跳过用户: userId={}", userId);
                return;
            }

            // 获取需要巩固的情景记忆
            int batchSize = consolidationProperties.getBatchSize();
            double minImportance = consolidationProperties.getImportanceThreshold();
            boolean semanticEnabled = consolidationProperties.isSemanticEnabled();
            boolean proceduralEnabled = consolidationProperties.isProceduralEnabled();

            List<EpisodicMemory> candidateMemories = episodicMemoryGateway
                .findMemoriesNeedingConsolidation(userId, minImportance, batchSize);

            if (candidateMemories.isEmpty()) {
                log.debug("用户无需巩固的记忆: userId={}", userId);
                return;
            }

            log.debug("找到{}条待巩固记忆: userId={}", candidateMemories.size(), userId);

            // 执行语义巩固
            if (semanticEnabled) {
                consolidateToSemanticMemories(userId, candidateMemories);
            }

            // 执行程序性巩固
            if (proceduralEnabled) {
                consolidateToProceduralMemories(userId, candidateMemories);
            }

            // 标记记忆为已巩固
            markMemoriesAsConsolidated(candidateMemories);

            log.debug("完成用户记忆巩固: userId={}", userId);

        } catch (Exception e) {
            log.error("用户记忆巩固失败: userId={}, error={}", userId, e.getMessage(), e);
        }
    }

    /**
     * 巩固为语义记忆
     */
    private void consolidateToSemanticMemories(Long userId, List<EpisodicMemory> memories) {
        log.debug("开始语义记忆巩固: userId={}, memoryCount={}", userId, memories.size());

        try {
            // 按相似性聚类情景记忆
            Map<String, List<EpisodicMemory>> clusters = clusterMemoriesByConcept(memories);

            for (Map.Entry<String, List<EpisodicMemory>> entry : clusters.entrySet()) {
                String concept = entry.getKey();
                List<EpisodicMemory> clusterMemories = entry.getValue();

                if (clusterMemories.size() < 2) {
                    continue; // 至少需要2个记忆才能形成稳定的语义记忆
                }

                // 生成语义知识
                String semanticKnowledge = generateSemanticKnowledge(clusterMemories);
                if (semanticKnowledge.isEmpty()) {
                    continue;
                }

                // 检查是否已存在相似的语义记忆
                var existingMemory = semanticMemoryGateway.findByUserIdAndConcept(userId, concept);

                if (existingMemory.isPresent()) {
                    // 更新现有语义记忆
                    updateSemanticMemory(existingMemory.get(), clusterMemories, semanticKnowledge);
                } else {
                    // 创建新的语义记忆
                    createSemanticMemory(userId, concept, semanticKnowledge, clusterMemories);
                }

                log.debug("语义记忆巩固完成: concept={}, sourceMemories={}", 
                         concept, clusterMemories.size());
            }

        } catch (Exception e) {
            log.warn("语义记忆巩固失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    /**
     * 巩固为程序性记忆
     */
    private void consolidateToProceduralMemories(Long userId, List<EpisodicMemory> memories) {
        log.debug("开始程序性记忆巩固: userId={}, memoryCount={}", userId, memories.size());

        try {
            // 分析行为模式
            Map<String, PatternAnalysis> patterns = analyzeBehaviorPatterns(memories);

            for (Map.Entry<String, PatternAnalysis> entry : patterns.entrySet()) {
                String patternType = entry.getKey();
                PatternAnalysis analysis = entry.getValue();

                if (analysis.frequency < 3) {
                    continue; // 频率不足的模式不进行巩固
                }

                String patternName = analysis.patternName;
                String description = analysis.description;
                Map<String, Object> triggers = analysis.triggerConditions;

                // 查找现有程序性记忆
                var existingPattern = proceduralMemoryGateway
                    .findByUserIdAndPatternTypeAndName(userId, patternType, patternName);

                if (existingPattern.isPresent()) {
                    // 强化现有程序性记忆
                    reinforceProceduralMemory(existingPattern.get(), analysis);
                } else {
                    // 创建新的程序性记忆
                    createProceduralMemory(userId, patternType, patternName, description, triggers, analysis);
                }

                log.debug("程序性记忆巩固完成: patternType={}, patternName={}", 
                         patternType, patternName);
            }

        } catch (Exception e) {
            log.warn("程序性记忆巩固失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    /**
     * 按概念聚类记忆
     */
    private Map<String, List<EpisodicMemory>> clusterMemoriesByConcept(List<EpisodicMemory> memories) {
        Map<String, List<EpisodicMemory>> clusters = new HashMap<>();

        for (EpisodicMemory memory : memories) {
            try {
                // 使用LLM提取主要概念
                String concept = extractMainConcept(memory.getContent());
                if (concept != null && !concept.isEmpty()) {
                    clusters.computeIfAbsent(concept, k -> new ArrayList<>()).add(memory);
                }
            } catch (Exception e) {
                log.warn("概念提取失败: episodeId={}, error={}", memory.getEpisodeId(), e.getMessage());
            }
        }

        // 过滤掉单个记忆的聚类
        clusters.entrySet().removeIf(entry -> entry.getValue().size() < 2);
        
        return clusters;
    }

    /**
     * 生成语义知识
     */
    private String generateSemanticKnowledge(List<EpisodicMemory> memories) {
        int maxEpisodesPerConcept = consolidationProperties.getMaxEpisodesPerConcept();
        String consolidatedContent = memories.stream()
            .map(EpisodicMemory::getContent)
            .limit(maxEpisodesPerConcept)
            .collect(Collectors.joining("\n---\n"));

        String prompt = """
            请将以下多段对话内容整合为一个完整的知识描述。
            
            对话内容：
            %s
            
            要求：
            1. 提取核心信息和关键知识点
            2. 去除冗余和重复内容
            3. 形成简洁清晰的知识描述
            4. 保持客观中性的语调
            
            请直接返回整合后的知识描述，不需要额外格式。
            """.formatted(consolidatedContent);

        try {
            return languageModel.generate(prompt).content();
        } catch (Exception e) {
            log.warn("语义知识生成失败: {}", e.getMessage());
            return "";
        }
    }

    /**
     * 提取主要概念
     */
    private String extractMainConcept(String content) {
        String prompt = """
            请从以下对话内容中提取一个最核心的概念或主题（限制在2-4个词）。
            
            对话内容：
            %s
            
            只返回概念名称，不需要解释。
            例如：用户偏好、技术问题、产品咨询
            """.formatted(content);

        try {
            return languageModel.generate(prompt).content();
        } catch (Exception e) {
            log.warn("概念提取失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 分析行为模式
     */
    private Map<String, PatternAnalysis> analyzeBehaviorPatterns(List<EpisodicMemory> memories) {
        Map<String, PatternAnalysis> patterns = new HashMap<>();

        // 简化的模式分析逻辑
        Map<String, Integer> questionCount = new HashMap<>();
        Map<String, Integer> preferenceCount = new HashMap<>();
        Map<String, Integer> skillCount = new HashMap<>();

        for (EpisodicMemory memory : memories) {
            String content = memory.getContent();
            
            if (content.contains("?") || content.contains("？")) {
                questionCount.merge("询问习惯", 1, Integer::sum);
            }
            
            if (content.matches(".*\\b(喜欢|偏好|希望|想要)\\b.*")) {
                preferenceCount.merge("偏好表达", 1, Integer::sum);
            }
            
            if (content.matches(".*\\b(会|懂|了解|熟悉)\\b.*")) {
                skillCount.merge("技能展示", 1, Integer::sum);
            }
        }

        // 构建模式分析结果
        questionCount.forEach((pattern, count) -> {
            if (count >= 2) {
                PatternAnalysis analysis = new PatternAnalysis();
                analysis.patternName = pattern;
                analysis.description = "用户倾向于通过提问的方式获取信息";
                analysis.frequency = count;
                analysis.triggerConditions = Map.of("has_question", true);
                patterns.put("habit", analysis);
            }
        });

        preferenceCount.forEach((pattern, count) -> {
            if (count >= 2) {
                PatternAnalysis analysis = new PatternAnalysis();
                analysis.patternName = pattern;
                analysis.description = "用户会明确表达个人偏好";
                analysis.frequency = count;
                analysis.triggerConditions = Map.of("expresses_preference", true);
                patterns.put("preference", analysis);
            }
        });

        skillCount.forEach((pattern, count) -> {
            if (count >= 2) {
                PatternAnalysis analysis = new PatternAnalysis();
                analysis.patternName = pattern;
                analysis.description = "用户会主动展示技能和知识";
                analysis.frequency = count;
                analysis.triggerConditions = Map.of("shows_expertise", true);
                patterns.put("response_style", analysis);
            }
        });

        return patterns;
    }

    /**
     * 更新语义记忆
     */
    private void updateSemanticMemory(SemanticMemory memory, List<EpisodicMemory> sourceMemories, String newKnowledge) {
        // 添加新的源记忆
        for (EpisodicMemory episodic : sourceMemories) {
            memory.addEvidence(episodic.getEpisodeId());
        }

        // 如果知识内容有显著差异，可以考虑更新知识描述
        if (!memory.getKnowledge().equals(newKnowledge)) {
            memory.setKnowledge(newKnowledge);
            memory.setUpdatedAt(System.currentTimeMillis());
        }

        semanticMemoryGateway.update(memory);
    }

    /**
     * 创建语义记忆
     */
    private void createSemanticMemory(Long userId, String concept, String knowledge, List<EpisodicMemory> sourceMemories) {
        List<String> sourceEpisodes = sourceMemories.stream()
            .map(EpisodicMemory::getEpisodeId)
            .collect(Collectors.toList());

        byte[] embeddingVector = generateEmbedding(concept + " " + knowledge);

        SemanticMemory semanticMemory = SemanticMemory.builder()
            .userId(userId)
            .concept(concept)
            .knowledge(knowledge)
            .embeddingVector(embeddingVector)
            .confidence(0.8) // 巩固产生的记忆置信度较高
            .sourceEpisodes(sourceEpisodes)
            .evidenceCount(sourceMemories.size())
            .contradictionCount(0)
            .lastReinforcedAt(System.currentTimeMillis())
            .decayRate(0.005) // 较低的衰减率
            .createdAt(System.currentTimeMillis())
            .updatedAt(System.currentTimeMillis())
            .build();

        semanticMemoryGateway.save(semanticMemory);
    }

    /**
     * 强化程序性记忆
     */
    private void reinforceProceduralMemory(ProceduralMemory memory, PatternAnalysis analysis) {
        // 增加成功次数
        for (int i = 0; i < analysis.frequency; i++) {
            memory.recordSuccess();
        }
        proceduralMemoryGateway.update(memory);
    }

    /**
     * 创建程序性记忆
     */
    private void createProceduralMemory(Long userId, String patternType, String patternName, 
                                       String description, Map<String, Object> triggers, PatternAnalysis analysis) {
        ProceduralMemory proceduralMemory = ProceduralMemory.builder()
            .userId(userId)
            .patternType(patternType)
            .patternName(patternName)
            .patternDescription(description)
            .triggerConditions(triggers)
            .actionTemplate("")
            .successCount(analysis.frequency)
            .failureCount(0)
            .successRate(1.0)
            .lastTriggeredAt(System.currentTimeMillis())
            .learningRate(0.05) // 巩固产生的记忆学习率较低，更稳定
            .isActive(true)
            .createdAt(System.currentTimeMillis())
            .updatedAt(System.currentTimeMillis())
            .build();

        proceduralMemoryGateway.save(proceduralMemory);
    }

    /**
     * 标记记忆为已巩固
     */
    private void markMemoriesAsConsolidated(List<EpisodicMemory> memories) {
        List<Long> memoryIds = memories.stream()
            .map(EpisodicMemory::getId)
            .collect(Collectors.toList());

        episodicMemoryGateway.batchUpdateConsolidationStatus(memoryIds, 1);
        log.debug("标记{}条记忆为已巩固", memoryIds.size());
    }

    /**
     * 生成向量嵌入
     */
    private byte[] generateEmbedding(String text) {
        try {
            Embedding embedding = embeddingModel.embed(text).content();
            float[] vector = embedding.vector();
            byte[] bytes = new byte[vector.length * 4];
            
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
     * 模式分析结果
     */
    private static class PatternAnalysis {
        String patternName;
        String description;
        int frequency;
        Map<String, Object> triggerConditions;
    }
}
