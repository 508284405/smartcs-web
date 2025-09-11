package com.leyue.smartcs.domain.ltm.domainservice;

import com.leyue.smartcs.domain.ltm.entity.EpisodicMemory;
import com.leyue.smartcs.domain.ltm.entity.ProceduralMemory;
import com.leyue.smartcs.domain.ltm.entity.SemanticMemory;

import java.util.List;
import java.util.Map;

/**
 * LTM领域服务
 * 封装LTM相关的核心业务逻辑
 */
public interface LTMDomainService {

    /**
     * LTM上下文信息
     */
    class LTMContext {
        private List<EpisodicMemory> episodicMemories;
        private List<SemanticMemory> semanticMemories;
        private List<ProceduralMemory> proceduralMemories;
        
        public LTMContext(List<EpisodicMemory> episodicMemories, 
                         List<SemanticMemory> semanticMemories, 
                         List<ProceduralMemory> proceduralMemories) {
            this.episodicMemories = episodicMemories;
            this.semanticMemories = semanticMemories;
            this.proceduralMemories = proceduralMemories;
        }

        public List<EpisodicMemory> getEpisodicMemories() {
            return episodicMemories;
        }

        public List<SemanticMemory> getSemanticMemories() {
            return semanticMemories;
        }

        public List<ProceduralMemory> getProceduralMemories() {
            return proceduralMemories;
        }

        public boolean isEmpty() {
            return (episodicMemories == null || episodicMemories.isEmpty()) &&
                   (semanticMemories == null || semanticMemories.isEmpty()) &&
                   (proceduralMemories == null || proceduralMemories.isEmpty());
        }
    }

    /**
     * 记忆检索请求
     */
    class MemoryRetrievalRequest {
        private Long userId;
        private String query;
        private byte[] queryVector;
        private Map<String, Object> context;
        private Integer maxResults;
        private Double threshold;
        
        public MemoryRetrievalRequest(Long userId, String query, byte[] queryVector, 
                                    Map<String, Object> context, Integer maxResults, Double threshold) {
            this.userId = userId;
            this.query = query;
            this.queryVector = queryVector;
            this.context = context;
            this.maxResults = maxResults;
            this.threshold = threshold;
        }

        // Getters
        public Long getUserId() { return userId; }
        public String getQuery() { return query; }
        public byte[] getQueryVector() { return queryVector; }
        public Map<String, Object> getContext() { return context; }
        public Integer getMaxResults() { return maxResults; }
        public Double getThreshold() { return threshold; }
    }

    /**
     * 记忆形成请求
     */
    class MemoryFormationRequest {
        private Long userId;
        private Long sessionId;
        private String content;
        private Map<String, Object> context;
        private Long timestamp;
        
        public MemoryFormationRequest(Long userId, Long sessionId, String content, 
                                    Map<String, Object> context, Long timestamp) {
            this.userId = userId;
            this.sessionId = sessionId;
            this.content = content;
            this.context = context;
            this.timestamp = timestamp;
        }

        // Getters
        public Long getUserId() { return userId; }
        public Long getSessionId() { return sessionId; }
        public String getContent() { return content; }
        public Map<String, Object> getContext() { return context; }
        public Long getTimestamp() { return timestamp; }
    }

    /**
     * 检索用户的LTM上下文
     * 
     * @param request 记忆检索请求
     * @return LTM上下文信息
     */
    LTMContext retrieveMemoryContext(MemoryRetrievalRequest request);

    /**
     * 形成新的记忆
     * 
     * @param request 记忆形成请求
     */
    void formMemory(MemoryFormationRequest request);

    /**
     * 巩固记忆
     * 将重要的情景记忆转换为语义记忆或程序性记忆
     * 
     * @param userId 用户ID
     */
    void consolidateMemories(Long userId);

    /**
     * 强化记忆
     * 基于使用频率和重要性强化已有记忆
     * 
     * @param userId 用户ID
     * @param memoryType 记忆类型
     * @param memoryId 记忆ID
     */
    void reinforceMemory(Long userId, String memoryType, Long memoryId);

    /**
     * 应用遗忘算法
     * 对长期未访问的记忆应用衰减
     * 
     * @param userId 用户ID
     */
    void applyForgetting(Long userId);

    /**
     * 个性化响应增强
     * 基于用户的程序性记忆个性化响应内容
     * 
     * @param userId 用户ID
     * @param originalResponse 原始响应
     * @param context 上下文信息
     * @return 个性化后的响应
     */
    String personalizeResponse(Long userId, String originalResponse, Map<String, Object> context);

    /**
     * 学习用户偏好
     * 基于用户行为学习和更新程序性记忆
     * 
     * @param userId 用户ID
     * @param interaction 交互信息
     * @param feedback 用户反馈（成功/失败）
     */
    void learnUserPreference(Long userId, Map<String, Object> interaction, Boolean feedback);

    /**
     * 获取用户记忆摘要
     * 
     * @param userId 用户ID
     * @return 记忆摘要信息
     */
    Map<String, Object> getMemorySummary(Long userId);

    /**
     * 清理过期记忆
     * 根据用户配置清理过期或不重要的记忆
     * 
     * @param userId 用户ID
     */
    void cleanupExpiredMemories(Long userId);

    /**
     * 导出用户记忆
     * 
     * @param userId 用户ID
     * @return 记忆数据
     */
    Map<String, Object> exportUserMemories(Long userId);

    /**
     * 导入用户记忆
     * 
     * @param userId 用户ID
     * @param memoryData 记忆数据
     */
    void importUserMemories(Long userId, Map<String, Object> memoryData);

    /**
     * 分析记忆模式
     * 分析用户的记忆模式和趋势
     * 
     * @param userId 用户ID
     * @return 分析结果
     */
    Map<String, Object> analyzeMemoryPatterns(Long userId);
}