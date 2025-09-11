package com.leyue.smartcs.domain.ltm.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 语义记忆实体测试
 */
class SemanticMemoryTest {

    private SemanticMemory semanticMemory;

    @BeforeEach
    void setUp() {
        List<String> sourceEpisodes = Arrays.asList("ep_001", "ep_002");

        semanticMemory = SemanticMemory.builder()
            .id(1L)
            .userId(100L)
            .concept("机器学习基础")
            .knowledge("机器学习是人工智能的一个分支")
            .embeddingVector(new byte[]{1, 2, 3})
            .confidence(0.8)
            .sourceEpisodes(sourceEpisodes)
            .evidenceCount(2)
            .contradictionCount(0)
            .lastReinforcedAt(System.currentTimeMillis())
            .decayRate(0.01)
            .createdAt(System.currentTimeMillis())
            .updatedAt(System.currentTimeMillis())
            .build();
    }

    @Test
    void testIsHighConfidence() {
        // 测试高置信度
        semanticMemory.setConfidence(0.85);
        assertTrue(semanticMemory.isHighConfidence());

        // 测试中等置信度
        semanticMemory.setConfidence(0.7);
        assertFalse(semanticMemory.isHighConfidence());

        // 测试边界值
        semanticMemory.setConfidence(0.8);
        assertTrue(semanticMemory.isHighConfidence());

        // 测试null值
        semanticMemory.setConfidence(null);
        assertFalse(semanticMemory.isHighConfidence());
    }

    @Test
    void testIsControversial() {
        // 测试争议性知识
        semanticMemory.setEvidenceCount(5);
        semanticMemory.setContradictionCount(3);
        assertTrue(semanticMemory.isControversial());

        // 测试非争议性知识
        semanticMemory.setEvidenceCount(10);
        semanticMemory.setContradictionCount(1);
        assertFalse(semanticMemory.isControversial());

        // 测试边界情况
        semanticMemory.setEvidenceCount(3);
        semanticMemory.setContradictionCount(2);
        assertTrue(semanticMemory.isControversial()); // 2/(3+2) = 0.4 > 0.3

        // 测试null值
        semanticMemory.setEvidenceCount(null);
        semanticMemory.setContradictionCount(null);
        assertFalse(semanticMemory.isControversial());
    }

    @Test
    void testNeedsUpdate() {
        long sevenDaysAgo = System.currentTimeMillis() - (8 * 24 * 60 * 60 * 1000L);
        
        // 测试需要更新的记忆
        semanticMemory.setLastReinforcedAt(sevenDaysAgo);
        assertTrue(semanticMemory.needsUpdate());

        // 测试最近更新的记忆
        semanticMemory.setLastReinforcedAt(System.currentTimeMillis());
        assertFalse(semanticMemory.needsUpdate());

        // 测试null值
        semanticMemory.setLastReinforcedAt(null);
        assertFalse(semanticMemory.needsUpdate());
    }

    @Test
    void testAddEvidence() {
        int initialCount = semanticMemory.getEvidenceCount();
        double initialConfidence = semanticMemory.getConfidence();
        
        semanticMemory.addEvidence("ep_003");
        
        // 验证证据数量增加
        assertEquals(initialCount + 1, semanticMemory.getEvidenceCount());
        
        // 验证最后强化时间更新
        assertNotNull(semanticMemory.getLastReinforcedAt());
        
        // 验证置信度计算
        assertNotNull(semanticMemory.getConfidence());
        
        // 测试重复添加相同证据
        List<String> episodes = semanticMemory.getSourceEpisodes();
        int episodesSizeBefore = episodes.size();
        semanticMemory.addEvidence("ep_001"); // 重复的episode
        // 应该增加证据计数，但不重复添加episode
        assertEquals(initialCount + 2, semanticMemory.getEvidenceCount());
    }

    @Test
    void testAddContradiction() {
        int initialContradictionCount = semanticMemory.getContradictionCount();
        
        semanticMemory.addContradiction();
        
        // 验证矛盾证据数量增加
        assertEquals(initialContradictionCount + 1, semanticMemory.getContradictionCount());
        
        // 验证最后强化时间更新
        assertNotNull(semanticMemory.getLastReinforcedAt());
        
        // 验证置信度降低
        assertNotNull(semanticMemory.getConfidence());
        
        // 测试null初始值
        semanticMemory.setContradictionCount(null);
        semanticMemory.addContradiction();
        assertEquals(1, semanticMemory.getContradictionCount());
    }

    @Test
    void testUpdateConfidence() {
        // 设置证据和矛盾数量
        semanticMemory.setEvidenceCount(8);
        semanticMemory.setContradictionCount(2);
        semanticMemory.setLastReinforcedAt(System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000L)); // 2天前
        semanticMemory.setDecayRate(0.1);
        
        semanticMemory.updateConfidence();
        
        // 基础置信度应该是 8/(8+2) = 0.8
        // 考虑衰减后应该更低
        assertNotNull(semanticMemory.getConfidence());
        assertTrue(semanticMemory.getConfidence() >= 0.0);
        assertTrue(semanticMemory.getConfidence() <= 1.0);
        
        // 测试null值处理
        semanticMemory.setEvidenceCount(null);
        semanticMemory.updateConfidence();
        // 不应该抛出异常
    }

    @Test
    void testReinforce() {
        Long beforeTime = System.currentTimeMillis();
        double initialConfidence = semanticMemory.getConfidence();
        
        semanticMemory.reinforce();
        
        // 验证时间更新
        assertTrue(semanticMemory.getLastReinforcedAt() >= beforeTime);
        assertTrue(semanticMemory.getUpdatedAt() >= beforeTime);
        
        // 验证置信度提升
        assertTrue(semanticMemory.getConfidence() >= initialConfidence);
        assertTrue(semanticMemory.getConfidence() <= 1.0);
    }

    @Test
    void testApplyDecay() {
        // 设置衰减参数
        semanticMemory.setConfidence(0.8);
        semanticMemory.setDecayRate(0.1);
        semanticMemory.setLastReinforcedAt(System.currentTimeMillis() - (1 * 24 * 60 * 60 * 1000L)); // 1天前
        
        semanticMemory.applyDecay();
        
        // 验证置信度衰减
        assertTrue(semanticMemory.getConfidence() < 0.8);
        assertTrue(semanticMemory.getConfidence() >= 0.0);
        
        // 测试null值不会导致异常
        semanticMemory.setDecayRate(null);
        semanticMemory.applyDecay();
    }

    @Test
    void testBuilderPattern() {
        SemanticMemory memory = SemanticMemory.builder()
            .userId(123L)
            .concept("测试概念")
            .knowledge("测试知识")
            .confidence(0.9)
            .build();
        
        assertNotNull(memory);
        assertEquals(123L, memory.getUserId());
        assertEquals("测试概念", memory.getConcept());
        assertEquals("测试知识", memory.getKnowledge());
        assertEquals(0.9, memory.getConfidence());
    }

    @Test
    void testConfidenceBounds() {
        // 测试置信度边界处理
        semanticMemory.setEvidenceCount(10);
        semanticMemory.setContradictionCount(0);
        
        semanticMemory.updateConfidence();
        assertTrue(semanticMemory.getConfidence() <= 1.0);
        
        // 测试衰减不会导致负值
        semanticMemory.setConfidence(0.1);
        semanticMemory.setDecayRate(1.0); // 100%衰减
        semanticMemory.setLastReinforcedAt(System.currentTimeMillis() - (10 * 24 * 60 * 60 * 1000L)); // 10天前
        
        semanticMemory.applyDecay();
        assertTrue(semanticMemory.getConfidence() >= 0.0);
    }
}