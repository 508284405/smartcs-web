package com.leyue.smartcs.app.ltm.executor;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.client.ltm.dto.memory.*;
import com.leyue.smartcs.domain.ltm.gateway.*;
import com.leyue.smartcs.domain.ltm.entity.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * 记忆查询执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MemoryQueryExe {

    private final EpisodicMemoryGateway episodicMemoryGateway;
    private final SemanticMemoryGateway semanticMemoryGateway;
    private final ProceduralMemoryGateway proceduralMemoryGateway;
    private final MemoryConverter memoryConverter;

    /**
     * 获取记忆摘要
     */
    public SingleResponse<MemorySummaryDTO> getMemorySummary(MemorySummaryQry qry) {
        log.debug("获取记忆摘要: userId={}", qry.getUserId());

        try {
            // 统计各类记忆数量
            Long episodicCount = episodicMemoryGateway.countByUserId(qry.getUserId());
            Long semanticCount = semanticMemoryGateway.countByUserId(qry.getUserId());
            Long proceduralCount = proceduralMemoryGateway.countByUserId(qry.getUserId());

            // 获取高重要性记忆数量
            Long highImportanceCount = episodicMemoryGateway.countByUserIdAndImportanceRange(
                qry.getUserId(), 0.7, 1.0);

            // 获取最近记忆数量
            long sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L);
            Long recentCount = episodicMemoryGateway.countByUserIdAndTimeRange(
                qry.getUserId(), sevenDaysAgo, System.currentTimeMillis());

            // 计算平均重要性评分
            Double avgImportance = episodicMemoryGateway.getAverageImportanceScore(qry.getUserId());

            // 获取时间范围
            Long earliestTime = episodicMemoryGateway.findEarliestMemoryTime(qry.getUserId()).orElse(null);
            Long latestTime = episodicMemoryGateway.findLatestMemoryTime(qry.getUserId()).orElse(null);

            // 构建记忆分类统计
            Map<String, Long> categoryStats = new HashMap<>();
            categoryStats.put("episodic", episodicCount);
            categoryStats.put("semantic", semanticCount);
            categoryStats.put("procedural", proceduralCount);

            // 获取偏好分析
            MemorySummaryDTO.PreferenceAnalysisDTO preferenceAnalysis = 
                analyzeUserPreferences(qry.getUserId());

            // 计算记忆形成频率
            double formationRate = calculateMemoryFormationRate(qry.getUserId(), earliestTime);

            // 构建摘要DTO
            MemorySummaryDTO summary = MemorySummaryDTO.builder()
                .userId(qry.getUserId())
                .totalEpisodicMemories(episodicCount)
                .totalSemanticMemories(semanticCount)
                .totalProceduralMemories(proceduralCount)
                .highImportanceCount(highImportanceCount)
                .recentMemoriesCount(recentCount)
                .averageImportanceScore(avgImportance != null ? avgImportance : 0.0)
                .mostActiveMemoryType(determineMostActiveType(episodicCount, semanticCount, proceduralCount))
                .memoryFormationRate(formationRate)
                .earliestMemoryTime(earliestTime)
                .latestMemoryTime(latestTime)
                .storageUsageMB(calculateStorageUsage(qry.getUserId()))
                .memoryCategoryStats(categoryStats)
                .preferenceAnalysis(preferenceAnalysis)
                .memoryHealthScore(calculateMemoryHealthScore(qry.getUserId()))
                .personalizationScore(calculatePersonalizationScore(qry.getUserId()))
                .recentConcepts(getRecentConcepts(qry.getUserId()))
                .optimizationSuggestions(generateOptimizationSuggestions(qry.getUserId()))
                .build();

            return SingleResponse.of(summary);

        } catch (Exception e) {
            log.error("获取记忆摘要失败: userId={}, error={}", qry.getUserId(), e.getMessage());
            return SingleResponse.buildFailure("MEMORY_SUMMARY_ERROR", "获取记忆摘要失败");
        }
    }

    /**
     * 获取情景记忆列表
     */
    public MultiResponse<EpisodicMemoryDTO> getEpisodicMemories(EpisodicMemoryPageQry qry) {
        log.debug("获取情景记忆列表: userId={}, page={}, size={}", 
                 qry.getUserId(), qry.getPage(), qry.getSize());

        try {
            List<EpisodicMemory> memories;

            if (qry.getMinImportance() != null) {
                memories = episodicMemoryGateway.findByImportanceScore(
                    qry.getUserId(), qry.getMinImportance(), qry.getSize());
            } else if (qry.getStartTime() != null && qry.getEndTime() != null) {
                memories = episodicMemoryGateway.findByUserIdAndTimeRange(
                    qry.getUserId(), qry.getStartTime(), qry.getEndTime());
            } else {
                memories = episodicMemoryGateway.findByUserId(
                    qry.getUserId(), qry.getPage() - 1, qry.getSize());
            }

            List<EpisodicMemoryDTO> memoryDTOs = memories.stream()
                .map(memoryConverter::toEpisodicMemoryDTO)
                .collect(Collectors.toList());

            return MultiResponse.of(memoryDTOs);

        } catch (Exception e) {
            log.error("获取情景记忆列表失败: userId={}, error={}", qry.getUserId(), e.getMessage());
            return MultiResponse.buildFailure("EPISODIC_MEMORY_QUERY_ERROR", "获取情景记忆列表失败");
        }
    }

    /**
     * 获取语义记忆列表
     */
    public MultiResponse<SemanticMemoryDTO> getSemanticMemories(SemanticMemoryPageQry qry) {
        log.debug("获取语义记忆列表: userId={}, page={}, size={}", 
                 qry.getUserId(), qry.getPage(), qry.getSize());

        try {
            List<SemanticMemory> memories;

            if (qry.getMinConfidence() != null) {
                memories = semanticMemoryGateway.findByConfidenceRange(
                    qry.getUserId(), qry.getMinConfidence(), 1.0, qry.getSize());
            } else if (qry.getConceptKeyword() != null) {
                memories = semanticMemoryGateway.findByConceptLike(
                    qry.getUserId(), "%" + qry.getConceptKeyword() + "%", qry.getSize());
            } else {
                memories = semanticMemoryGateway.findByUserId(
                    qry.getUserId(), qry.getPage() - 1, qry.getSize());
            }

            List<SemanticMemoryDTO> memoryDTOs = memories.stream()
                .map(memoryConverter::toSemanticMemoryDTO)
                .collect(Collectors.toList());

            return MultiResponse.of(memoryDTOs);

        } catch (Exception e) {
            log.error("获取语义记忆列表失败: userId={}, error={}", qry.getUserId(), e.getMessage());
            return MultiResponse.buildFailure("SEMANTIC_MEMORY_QUERY_ERROR", "获取语义记忆列表失败");
        }
    }

    /**
     * 获取程序性记忆列表
     */
    public MultiResponse<ProceduralMemoryDTO> getProceduralMemories(ProceduralMemoryQry qry) {
        log.debug("获取程序性记忆列表: userId={}, patternType={}", 
                 qry.getUserId(), qry.getPatternType());

        try {
            List<ProceduralMemory> memories;

            if (qry.getPatternType() != null) {
                memories = proceduralMemoryGateway.findByUserIdAndPatternType(
                    qry.getUserId(), qry.getPatternType());
            } else if (qry.getActiveOnly()) {
                memories = proceduralMemoryGateway.findActiveMemories(qry.getUserId());
            } else {
                memories = proceduralMemoryGateway.findByUserId(qry.getUserId(), 0, 100);
            }

            List<ProceduralMemoryDTO> memoryDTOs = memories.stream()
                .map(memoryConverter::toProceduralMemoryDTO)
                .collect(Collectors.toList());

            return MultiResponse.of(memoryDTOs);

        } catch (Exception e) {
            log.error("获取程序性记忆列表失败: userId={}, error={}", qry.getUserId(), e.getMessage());
            return MultiResponse.buildFailure("PROCEDURAL_MEMORY_QUERY_ERROR", "获取程序性记忆列表失败");
        }
    }

    /**
     * 搜索记忆
     */
    public MultiResponse<MemorySearchResultDTO> searchMemories(MemorySearchQry qry) {
        log.debug("搜索记忆: userId={}, keyword={}", qry.getUserId(), qry.getKeyword());

        try {
            List<MemorySearchResultDTO> results = new java.util.ArrayList<>();

            // 搜索情景记忆
            if (qry.getMemoryType() == null || "episodic".equals(qry.getMemoryType())) {
                List<EpisodicMemory> episodicResults = searchEpisodicMemories(
                    qry.getUserId(), qry.getKeyword(), qry.getLimit() / 3);
                results.addAll(episodicResults.stream()
                    .map(m -> memoryConverter.toMemorySearchResult(m, "episodic"))
                    .collect(Collectors.toList()));
            }

            // 搜索语义记忆
            if (qry.getMemoryType() == null || "semantic".equals(qry.getMemoryType())) {
                List<SemanticMemory> semanticResults = searchSemanticMemories(
                    qry.getUserId(), qry.getKeyword(), qry.getLimit() / 3);
                results.addAll(semanticResults.stream()
                    .map(m -> memoryConverter.toMemorySearchResult(m, "semantic"))
                    .collect(Collectors.toList()));
            }

            // 搜索程序性记忆
            if (qry.getMemoryType() == null || "procedural".equals(qry.getMemoryType())) {
                List<ProceduralMemory> proceduralResults = searchProceduralMemories(
                    qry.getUserId(), qry.getKeyword(), qry.getLimit() / 3);
                results.addAll(proceduralResults.stream()
                    .map(m -> memoryConverter.toMemorySearchResult(m, "procedural"))
                    .collect(Collectors.toList()));
            }

            // 按相关性排序并限制结果数量
            results = results.stream()
                .sorted((a, b) -> Double.compare(b.getRelevanceScore(), a.getRelevanceScore()))
                .limit(qry.getLimit())
                .collect(Collectors.toList());

            return MultiResponse.of(results);

        } catch (Exception e) {
            log.error("搜索记忆失败: userId={}, error={}", qry.getUserId(), e.getMessage());
            return MultiResponse.buildFailure("MEMORY_SEARCH_ERROR", "搜索记忆失败");
        }
    }

    // 私有辅助方法

    private MemorySummaryDTO.PreferenceAnalysisDTO analyzeUserPreferences(Long userId) {
        // 简化实现，实际应该分析程序性记忆中的偏好模式
        return MemorySummaryDTO.PreferenceAnalysisDTO.builder()
            .primaryInterests(List.of("技术", "学习"))
            .communicationStyle("友好专业")
            .learningMode("实践导向")
            .responseDetailLevel("中等详细")
            .preferenceConfidence(0.7)
            .build();
    }

    private double calculateMemoryFormationRate(Long userId, Long earliestTime) {
        if (earliestTime == null) return 0.0;
        
        long totalDays = (System.currentTimeMillis() - earliestTime) / (24 * 60 * 60 * 1000L);
        if (totalDays == 0) return 0.0;
        
        Long totalMemories = episodicMemoryGateway.countByUserId(userId);
        return totalMemories.doubleValue() / totalDays;
    }

    private String determineMostActiveType(Long episodic, Long semantic, Long procedural) {
        if (episodic >= semantic && episodic >= procedural) return "episodic";
        if (semantic >= procedural) return "semantic";
        return "procedural";
    }

    private double calculateStorageUsage(Long userId) {
        // 简化计算，实际应该计算向量和文本的存储大小
        Long totalMemories = episodicMemoryGateway.countByUserId(userId) +
                            semanticMemoryGateway.countByUserId(userId) +
                            proceduralMemoryGateway.countByUserId(userId);
        return totalMemories * 0.1; // 假设每条记忆占用0.1MB
    }

    private Integer calculateMemoryHealthScore(Long userId) {
        // 简化评分逻辑
        return 85;
    }

    private Integer calculatePersonalizationScore(Long userId) {
        // 简化评分逻辑
        return 78;
    }

    private List<String> getRecentConcepts(Long userId) {
        // 简化实现
        return List.of("机器学习", "系统架构", "个性化推荐");
    }

    private List<String> generateOptimizationSuggestions(Long userId) {
        // 简化实现
        return List.of(
            "建议增加更多技术相关的对话以丰富专业知识库",
            "可以定期回顾和确认重要记忆的准确性",
            "考虑调整个性化程度以获得更好的体验"
        );
    }

    private List<EpisodicMemory> searchEpisodicMemories(Long userId, String keyword, int limit) {
        // 简化搜索实现，实际应该使用全文搜索或向量搜索
        return episodicMemoryGateway.findByUserId(userId, 0, limit);
    }

    private List<SemanticMemory> searchSemanticMemories(Long userId, String keyword, int limit) {
        return semanticMemoryGateway.findByConceptLike(userId, "%" + keyword + "%", limit);
    }

    private List<ProceduralMemory> searchProceduralMemories(Long userId, String keyword, int limit) {
        return proceduralMemoryGateway.findByUserId(userId, 0, limit);
    }
}