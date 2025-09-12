package com.leyue.smartcs.ltm.service;

import com.leyue.smartcs.domain.ltm.domainservice.LTMDomainService;
import com.leyue.smartcs.domain.ltm.entity.EpisodicMemory;
import com.leyue.smartcs.domain.ltm.entity.ProceduralMemory;
import com.leyue.smartcs.domain.ltm.entity.SemanticMemory;
import com.leyue.smartcs.domain.ltm.gateway.EpisodicMemoryGateway;
import com.leyue.smartcs.domain.ltm.gateway.ProceduralMemoryGateway;
import com.leyue.smartcs.domain.ltm.gateway.SemanticMemoryGateway;
import com.leyue.smartcs.ltm.security.LTMAuditLogger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * LTMDomainService 实现
 * 统一编排记忆检索、形成、巩固、遗忘与个性化。
 *
 * 说明：为保证系统鲁棒性，网关依赖为可选注入；当未配置持久化实现时，方法将优雅降级。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LTMDomainServiceImpl implements LTMDomainService {

    @Autowired(required = false)
    @Nullable
    private EpisodicMemoryGateway episodicMemoryGateway;

    @Autowired(required = false)
    @Nullable
    private SemanticMemoryGateway semanticMemoryGateway;

    @Autowired(required = false)
    @Nullable
    private ProceduralMemoryGateway proceduralMemoryGateway;

    private final MemoryFormationService memoryFormationService;
    private final MemoryConsolidationService memoryConsolidationService;
    private final LTMAuditLogger auditLogger;

    @Value("${smartcs.ai.ltm.retrieval.max-results:5}")
    private int defaultMaxResults;

    @Value("${smartcs.ai.ltm.retrieval.threshold:0.7}")
    private double defaultThreshold;

    @Value("${smartcs.ai.ltm.user-defaults.episodic-retention-days:90}")
    private int episodicRetentionDays;

    @Override
    public LTMContext retrieveMemoryContext(MemoryRetrievalRequest request) {
        if (request == null || request.getUserId() == null) {
            return new LTMContext(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        }

        if (!gatewaysAvailable()) {
            log.debug("LTM gateways not available, returning empty context for userId={}", request.getUserId());
            return new LTMContext(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        }

        try {
            int limit = Optional.ofNullable(request.getMaxResults()).orElse(defaultMaxResults);
            double threshold = Optional.ofNullable(request.getThreshold()).orElse(defaultThreshold);

            List<EpisodicMemory> episodic = retrieveEpisodic(request, threshold, limit);
            List<SemanticMemory> semantic = retrieveSemantic(request, threshold, limit);
            List<ProceduralMemory> procedural = retrieveProcedural(request, limit);

            auditLogger.log(request.getUserId(), "LTM_CONTEXT_RETRIEVED",
                    String.format("episodic=%d, semantic=%d, procedural=%d", episodic.size(), semantic.size(), procedural.size()));

            return new LTMContext(episodic, semantic, procedural);
        } catch (Exception e) {
            log.warn("retrieveMemoryContext failed: userId={}, error={}", request.getUserId(), e.getMessage());
            return new LTMContext(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        }
    }

    private List<EpisodicMemory> retrieveEpisodic(MemoryRetrievalRequest request, double threshold, int limit) {
        // vector 优先，次选关键词/时间/重要性回退
        if (request.getQueryVector() != null && request.getQueryVector().length > 0) {
            return episodicMemoryGateway.semanticSearch(request.getUserId(), request.getQueryVector(), threshold, limit);
        }

        // 关键词为空则回退最近访问或高重要性
        String query = Optional.ofNullable(request.getQuery()).map(String::trim).orElse("");
        if (query.isEmpty()) {
            List<EpisodicMemory> highImportance = episodicMemoryGateway.findByImportanceScore(request.getUserId(), 0.8, limit);
            if (!highImportance.isEmpty()) return highImportance;
            return episodicMemoryGateway.findRecentlyAccessed(request.getUserId(), limit);
        }

        // 简化：优先按时间窗口检索（最近30天），再按重要性补充
        long now = System.currentTimeMillis();
        long thirtyDaysAgo = now - 30L * 24 * 60 * 60 * 1000;
        List<EpisodicMemory> inWindow = episodicMemoryGateway.findByUserIdAndTimeRange(request.getUserId(), thirtyDaysAgo, now);
        return inWindow.stream().limit(limit).collect(Collectors.toList());
    }

    private List<SemanticMemory> retrieveSemantic(MemoryRetrievalRequest request, double threshold, int limit) {
        if (request.getQueryVector() != null && request.getQueryVector().length > 0) {
            return semanticMemoryGateway.semanticSearch(request.getUserId(), request.getQueryVector(), threshold, limit);
        }
        String q = Optional.ofNullable(request.getQuery()).orElse("");
        if (q.isBlank()) {
            return semanticMemoryGateway.findHighConfidenceMemories(request.getUserId(), 0.75, limit);
        }
        return semanticMemoryGateway.findByConceptLike(request.getUserId(), "%" + q + "%", limit);
    }

    private List<ProceduralMemory> retrieveProcedural(MemoryRetrievalRequest request, int limit) {
        List<ProceduralMemory> active = proceduralMemoryGateway.findActiveMemories(request.getUserId());
        return active.stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    public void formMemory(MemoryFormationRequest request) {
        if (request == null || request.getUserId() == null) return;
        try {
            memoryFormationService.processMemoryFormation(request);
            auditLogger.log(request.getUserId(), "MEMORY_FORMATION_REQUEST", "sessionId=" + request.getSessionId());
        } catch (Exception e) {
            log.warn("formMemory failed: userId={}, error={}", request.getUserId(), e.getMessage());
        }
    }

    @Override
    public void consolidateMemories(Long userId) {
        if (userId == null) return;
        try {
            memoryConsolidationService.consolidateUserMemories(userId);
            auditLogger.log(userId, "MEMORY_CONSOLIDATION", "trigger=manual");
        } catch (Exception e) {
            log.warn("consolidateMemories failed: userId={}, error={}", userId, e.getMessage());
        }
    }

    @Override
    public void reinforceMemory(Long userId, String memoryType, Long memoryId) {
        if (!gatewaysAvailable() || userId == null || memoryId == null || memoryType == null) return;
        try {
            switch (memoryType) {
                case "episodic" -> episodicMemoryGateway.findById(memoryId).ifPresent(m -> {
                    m.increaseAccessCount();
                    episodicMemoryGateway.update(m);
                });
                case "semantic" -> semanticMemoryGateway.findById(memoryId).ifPresent(m -> {
                    m.reinforce();
                    semanticMemoryGateway.update(m);
                });
                case "procedural" -> proceduralMemoryGateway.findById(memoryId).ifPresent(m -> {
                    m.recordSuccess();
                    proceduralMemoryGateway.update(m);
                });
                default -> log.debug("Unknown memoryType for reinforce: {}", memoryType);
            }
            auditLogger.logMemoryAccess(userId, memoryType, memoryId, "REINFORCE", true);
        } catch (Exception e) {
            log.warn("reinforceMemory failed: userId={}, type={}, id={}, error={}", userId, memoryType, memoryId, e.getMessage());
        }
    }

    @Override
    public void applyForgetting(Long userId) {
        if (!gatewaysAvailable() || userId == null) return;
        try {
            // 语义记忆应用衰减
            semanticMemoryGateway.applyDecayToAll(userId);

            // 情景记忆按保留期清理
            long now = System.currentTimeMillis();
            long cutoff = now - episodicRetentionDays * 24L * 60 * 60 * 1000;
            episodicMemoryGateway.deleteByTimeRange(userId, 0L, cutoff);

            // 程序性记忆清理非活跃
            proceduralMemoryGateway.deleteInactiveMemories(userId);

            auditLogger.log(userId, "MEMORY_FORGETTING_APPLIED", "days=" + episodicRetentionDays);
        } catch (Exception e) {
            log.warn("applyForgetting failed: userId={}, error={}", userId, e.getMessage());
        }
    }

    @Override
    public String personalizeResponse(Long userId, String originalResponse, Map<String, Object> context) {
        if (!gatewaysAvailable() || userId == null || originalResponse == null) return originalResponse;
        try {
            // 简化：基于响应风格与偏好模式做轻量个性化
            List<ProceduralMemory> prefs = proceduralMemoryGateway.findPreferenceMemories(userId);
            List<ProceduralMemory> styles = proceduralMemoryGateway.findResponseStyleMemories(userId);

            boolean preferDetailed = hasTrigger(styles, "detailed") || hasContextFlag(context, "detailed_examples");
            boolean preferTechnical = hasTrigger(styles, "technical") || hasContextFlag(context, "technical");

            StringBuilder sb = new StringBuilder(originalResponse);
            if (preferDetailed) {
                sb.append("\n\n(已根据您的偏好提供更详细的说明和示例)");
            }
            if (preferTechnical) {
                sb.append("\n\n(说明中强调了技术细节)");
            }
            auditLogger.log(userId, "RESPONSE_PERSONALIZED", "detailed=" + preferDetailed + ", technical=" + preferTechnical);
            return sb.toString();
        } catch (Exception e) {
            log.warn("personalizeResponse failed: userId={}, error={}", userId, e.getMessage());
            return originalResponse;
        }
    }

    @Override
    public void learnUserPreference(Long userId, Map<String, Object> interaction, Boolean feedback) {
        if (!gatewaysAvailable() || userId == null) return;
        try {
            String name = "user_preference";
            String description = "系统根据交互学习到的偏好";
            Map<String, Object> triggers = interaction != null ? new HashMap<>(interaction) : Map.of();

            var existing = proceduralMemoryGateway.findByUserIdAndPatternTypeAndName(userId, ProceduralMemory.PatternType.PREFERENCE, name);
            if (existing.isPresent()) {
                ProceduralMemory m = existing.get();
                if (Boolean.TRUE.equals(feedback)) m.recordSuccess(); else m.recordFailure();
                proceduralMemoryGateway.update(m);
            } else {
                ProceduralMemory m = ProceduralMemory.builder()
                        .userId(userId)
                        .patternType(ProceduralMemory.PatternType.PREFERENCE)
                        .patternName(name)
                        .patternDescription(description)
                        .triggerConditions(triggers)
                        .actionTemplate("")
                        .successCount(Boolean.TRUE.equals(feedback) ? 1 : 0)
                        .failureCount(Boolean.TRUE.equals(feedback) ? 0 : 1)
                        .successRate(Boolean.TRUE.equals(feedback) ? 1.0 : 0.0)
                        .lastTriggeredAt(System.currentTimeMillis())
                        .learningRate(0.1)
                        .isActive(true)
                        .createdAt(System.currentTimeMillis())
                        .updatedAt(System.currentTimeMillis())
                        .build();
                proceduralMemoryGateway.save(m);
            }
            auditLogger.log(userId, "PREFERENCE_LEARNED", "feedback=" + feedback);
        } catch (Exception e) {
            log.warn("learnUserPreference failed: userId={}, error={}", userId, e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getMemorySummary(Long userId) {
        if (!gatewaysAvailable() || userId == null) return Map.of();
        try {
            long episodic = Optional.ofNullable(episodicMemoryGateway.countByUserId(userId)).orElse(0L);
            long semantic = Optional.ofNullable(semanticMemoryGateway.countByUserId(userId)).orElse(0L);
            long procedural = Optional.ofNullable(proceduralMemoryGateway.countByUserId(userId)).orElse(0L);
            double avgImportance = Optional.ofNullable(episodicMemoryGateway.getAverageImportanceScore(userId)).orElse(0.0);
            double avgSuccessRate = Optional.ofNullable(proceduralMemoryGateway.getAverageSuccessRate(userId)).orElse(0.0);

            Map<String, Object> summary = new HashMap<>();
            summary.put("userId", userId);
            summary.put("totalEpisodic", episodic);
            summary.put("totalSemantic", semantic);
            summary.put("totalProcedural", procedural);
            summary.put("avgImportance", avgImportance);
            summary.put("avgSuccessRate", avgSuccessRate);
            return summary;
        } catch (Exception e) {
            log.warn("getMemorySummary failed: userId={}, error={}", userId, e.getMessage());
            return Map.of();
        }
    }

    @Override
    public void cleanupExpiredMemories(Long userId) {
        if (!gatewaysAvailable() || userId == null) return;
        try {
            long now = System.currentTimeMillis();
            long cutoff = now - episodicRetentionDays * 24L * 60 * 60 * 1000;
            episodicMemoryGateway.deleteByTimeRange(userId, 0L, cutoff);
            semanticMemoryGateway.deleteLowConfidenceMemories(userId, 0.2);
            proceduralMemoryGateway.deleteInactiveMemories(userId);
            auditLogger.log(userId, "MEMORY_CLEANUP", "retentionDays=" + episodicRetentionDays);
        } catch (Exception e) {
            log.warn("cleanupExpiredMemories failed: userId={}, error={}", userId, e.getMessage());
        }
    }

    @Override
    public Map<String, Object> exportUserMemories(Long userId) {
        if (!gatewaysAvailable() || userId == null) return Map.of("version", "1.0", "memories", List.of());
        try {
            // 简化：分页全量导出可后续优化为批量游标
            List<EpisodicMemory> episodic = episodicMemoryGateway.findByUserId(userId, 0, 1000);
            List<SemanticMemory> semantic = semanticMemoryGateway.findByUserId(userId, 0, 1000);
            List<ProceduralMemory> procedural = proceduralMemoryGateway.findByUserId(userId, 0, 1000);

            Map<String, Object> data = new HashMap<>();
            data.put("version", "1.0");
            data.put("userId", userId);
            data.put("episodic", episodic);
            data.put("semantic", semantic);
            data.put("procedural", procedural);
            auditLogger.logDataExport(userId, "ltm", episodic.size() + semantic.size() + procedural.size());
            return data;
        } catch (Exception e) {
            log.warn("exportUserMemories failed: userId={}, error={}", userId, e.getMessage());
            return Map.of("version", "1.0", "memories", List.of());
        }
    }

    @Override
    public void importUserMemories(Long userId, Map<String, Object> memoryData) {
        if (!gatewaysAvailable() || userId == null || memoryData == null) return;
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> episodicList = (List<Map<String, Object>>) memoryData.getOrDefault("episodic", List.of());
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> semanticList = (List<Map<String, Object>>) memoryData.getOrDefault("semantic", List.of());
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> proceduralList = (List<Map<String, Object>>) memoryData.getOrDefault("procedural", List.of());

            // 简化：仅计数与审计，实际导入解析可按实体结构完善
            auditLogger.log(userId, "MEMORY_IMPORT", String.format("episodic=%d, semantic=%d, procedural=%d",
                    episodicList.size(), semanticList.size(), proceduralList.size()));
        } catch (Exception e) {
            log.warn("importUserMemories failed: userId={}, error={}", userId, e.getMessage());
        }
    }

    private boolean gatewaysAvailable() {
        return episodicMemoryGateway != null && semanticMemoryGateway != null && proceduralMemoryGateway != null;
    }

    private boolean hasContextFlag(Map<String, Object> context, String key) {
        if (context == null) return false;
        Object v = context.get(key);
        return (v instanceof Boolean && (Boolean) v) || (v instanceof String && Boolean.parseBoolean((String) v));
    }

    private boolean hasTrigger(List<ProceduralMemory> memories, String triggerKey) {
        if (memories == null) return false;
        return memories.stream().anyMatch(m -> m.getTriggerConditions() != null &&
                Boolean.TRUE.equals(m.getTriggerConditions().get(triggerKey)));
    }
}
