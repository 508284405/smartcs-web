package com.leyue.smartcs.domain.ltm.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 语义记忆领域实体
 * 存储从情景记忆中提取的概念性知识
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SemanticMemory {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 概念/知识点名称
     */
    private String concept;

    /**
     * 知识内容描述
     */
    private String knowledge;

    /**
     * 向量嵌入，用于语义检索
     */
    private byte[] embeddingVector;

    /**
     * 知识置信度 0.000-1.000
     */
    private Double confidence;

    /**
     * 来源情景记忆ID列表，追溯知识来源
     */
    private List<String> sourceEpisodes;

    /**
     * 支持证据数量
     */
    private Integer evidenceCount;

    /**
     * 矛盾证据数量
     */
    private Integer contradictionCount;

    /**
     * 最后强化时间
     */
    private Long lastReinforcedAt;

    /**
     * 遗忘衰减率
     */
    private Double decayRate;

    /**
     * 创建时间
     */
    private Long createdAt;

    /**
     * 更新时间
     */
    private Long updatedAt;

    /**
     * 是否为高置信度知识
     */
    public boolean isHighConfidence() {
        return confidence != null && confidence >= 0.8;
    }

    /**
     * 是否为争议性知识
     */
    public boolean isControversial() {
        if (evidenceCount == null || contradictionCount == null) {
            return false;
        }
        return contradictionCount > 0 && 
               (double) contradictionCount / (evidenceCount + contradictionCount) > 0.3;
    }

    /**
     * 是否需要更新
     */
    public boolean needsUpdate() {
        return lastReinforcedAt != null && 
               System.currentTimeMillis() - lastReinforcedAt > 7 * 24 * 60 * 60 * 1000L; // 7天
    }

    /**
     * 添加支持证据
     */
    public void addEvidence(String episodeId) {
        if (this.evidenceCount == null) {
            this.evidenceCount = 0;
        }
        this.evidenceCount++;
        
        if (this.sourceEpisodes != null && !this.sourceEpisodes.contains(episodeId)) {
            this.sourceEpisodes.add(episodeId);
        }
        
        // 更新置信度
        updateConfidence();
        this.lastReinforcedAt = System.currentTimeMillis();
    }

    /**
     * 添加矛盾证据
     */
    public void addContradiction() {
        if (this.contradictionCount == null) {
            this.contradictionCount = 0;
        }
        this.contradictionCount++;
        
        // 更新置信度
        updateConfidence();
        this.lastReinforcedAt = System.currentTimeMillis();
    }

    /**
     * 更新置信度
     */
    public void updateConfidence() {
        if (evidenceCount == null || contradictionCount == null) {
            return;
        }
        
        int totalEvidence = evidenceCount + contradictionCount;
        if (totalEvidence > 0) {
            double newConfidence = (double) evidenceCount / totalEvidence;
            // 应用衰减
            if (decayRate != null && lastReinforcedAt != null) {
                long daysPassed = (System.currentTimeMillis() - lastReinforcedAt) / (24 * 60 * 60 * 1000L);
                newConfidence = newConfidence * Math.pow(1 - decayRate, daysPassed);
            }
            this.confidence = Math.max(0.0, Math.min(1.0, newConfidence));
        }
    }

    /**
     * 强化记忆
     */
    public void reinforce() {
        this.lastReinforcedAt = System.currentTimeMillis();
        // 每次强化提升置信度
        if (this.confidence != null) {
            this.confidence = Math.min(1.0, this.confidence + 0.05);
        }
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * 应用遗忘衰减
     */
    public void applyDecay() {
        if (decayRate != null && lastReinforcedAt != null && confidence != null) {
            long daysPassed = (System.currentTimeMillis() - lastReinforcedAt) / (24 * 60 * 60 * 1000L);
            if (daysPassed > 0) {
                this.confidence = this.confidence * Math.pow(1 - decayRate, daysPassed);
                this.confidence = Math.max(0.0, this.confidence);
            }
        }
    }
}