package com.leyue.smartcs.domain.ltm.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 情景记忆实体测试
 */
class EpisodicMemoryTest {

    private EpisodicMemory episodicMemory;

    @BeforeEach
    void setUp() {
        Map<String, Object> contextMetadata = new HashMap<>();
        contextMetadata.put("location", "office");
        contextMetadata.put("emotion", "curious");

        episodicMemory = EpisodicMemory.builder()
            .id(1L)
            .userId(100L)
            .sessionId(200L)
            .episodeId("ep_test123")
            .content("用户询问关于机器学习的问题")
            .embeddingVector(new byte[]{1, 2, 3})
            .contextMetadata(contextMetadata)
            .timestamp(System.currentTimeMillis())
            .importanceScore(0.8)
            .accessCount(5)
            .lastAccessedAt(System.currentTimeMillis())
            .consolidationStatus(0)
            .createdAt(System.currentTimeMillis())
            .updatedAt(System.currentTimeMillis())
            .build();
    }

    @Test
    void testIsImportant() {
        // 测试高重要性记忆
        episodicMemory.setImportanceScore(0.8);
        assertTrue(episodicMemory.isImportant());

        // 测试中等重要性记忆
        episodicMemory.setImportanceScore(0.6);
        assertFalse(episodicMemory.isImportant());

        // 测试边界值
        episodicMemory.setImportanceScore(0.7);
        assertTrue(episodicMemory.isImportant());

        // 测试null值
        episodicMemory.setImportanceScore(null);
        assertFalse(episodicMemory.isImportant());
    }

    @Test
    void testIsHighFrequency() {
        // 测试高频记忆
        episodicMemory.setAccessCount(15);
        assertTrue(episodicMemory.isHighFrequency());

        // 测试低频记忆
        episodicMemory.setAccessCount(5);
        assertFalse(episodicMemory.isHighFrequency());

        // 测试边界值
        episodicMemory.setAccessCount(10);
        assertTrue(episodicMemory.isHighFrequency());

        // 测试null值
        episodicMemory.setAccessCount(null);
        assertFalse(episodicMemory.isHighFrequency());
    }

    @Test
    void testNeedsConsolidation() {
        // 设置为重要且未巩固的记忆
        episodicMemory.setImportanceScore(0.8);
        episodicMemory.setConsolidationStatus(0);
        assertTrue(episodicMemory.needsConsolidation());

        // 设置为重要但已巩固的记忆
        episodicMemory.setConsolidationStatus(1);
        assertFalse(episodicMemory.needsConsolidation());

        // 设置为不重要且未巩固的记忆
        episodicMemory.setImportanceScore(0.5);
        episodicMemory.setConsolidationStatus(0);
        assertFalse(episodicMemory.needsConsolidation());
    }

    @Test
    void testIncreaseAccessCount() {
        Long beforeTime = System.currentTimeMillis();
        
        // 初始访问次数
        episodicMemory.setAccessCount(3);
        
        // 增加访问次数
        episodicMemory.increaseAccessCount();
        
        // 验证访问次数增加
        assertEquals(4, episodicMemory.getAccessCount());
        
        // 验证最后访问时间更新
        assertNotNull(episodicMemory.getLastAccessedAt());
        assertTrue(episodicMemory.getLastAccessedAt() >= beforeTime);
        
        // 测试null初始值
        episodicMemory.setAccessCount(null);
        episodicMemory.increaseAccessCount();
        assertEquals(1, episodicMemory.getAccessCount());
    }

    @Test
    void testUpdateImportanceScore() {
        Long beforeTime = System.currentTimeMillis();
        
        // 更新重要性评分
        episodicMemory.updateImportanceScore(0.9);
        
        assertEquals(0.9, episodicMemory.getImportanceScore());
        
        // 测试高重要性记忆的巩固状态更新
        episodicMemory.setConsolidationStatus(0);
        episodicMemory.updateImportanceScore(0.8);
        assertEquals(0, episodicMemory.getConsolidationStatus()); // 仍为0，需要巩固
        
        // 测试低重要性记忆
        episodicMemory.updateImportanceScore(0.5);
        assertEquals(0.5, episodicMemory.getImportanceScore());
    }

    @Test
    void testMarkAsConsolidated() {
        Long beforeTime = System.currentTimeMillis();
        
        episodicMemory.markAsConsolidated();
        
        assertEquals(1, episodicMemory.getConsolidationStatus());
        assertNotNull(episodicMemory.getUpdatedAt());
        assertTrue(episodicMemory.getUpdatedAt() >= beforeTime);
    }

    @Test
    void testMarkAsArchived() {
        Long beforeTime = System.currentTimeMillis();
        
        episodicMemory.markAsArchived();
        
        assertEquals(2, episodicMemory.getConsolidationStatus());
        assertNotNull(episodicMemory.getUpdatedAt());
        assertTrue(episodicMemory.getUpdatedAt() >= beforeTime);
    }

    @Test
    void testAddContextMetadata() {
        // 添加新的上下文元数据
        episodicMemory.addContextMetadata("topic", "machine_learning");
        
        assertEquals("machine_learning", episodicMemory.getContextMetadata().get("topic"));
        assertEquals("office", episodicMemory.getContextMetadata().get("location"));
        
        // 覆盖现有的上下文元数据
        episodicMemory.addContextMetadata("location", "home");
        assertEquals("home", episodicMemory.getContextMetadata().get("location"));
        
        // 测试null metadata
        episodicMemory.setContextMetadata(null);
        episodicMemory.addContextMetadata("new_key", "new_value");
        // 不应该抛出异常，但也不会添加数据
    }

    @Test
    void testBuilderPattern() {
        EpisodicMemory memory = EpisodicMemory.builder()
            .userId(123L)
            .episodeId("ep_builder_test")
            .content("测试内容")
            .importanceScore(0.7)
            .build();
        
        assertNotNull(memory);
        assertEquals(123L, memory.getUserId());
        assertEquals("ep_builder_test", memory.getEpisodeId());
        assertEquals("测试内容", memory.getContent());
        assertEquals(0.7, memory.getImportanceScore());
    }

    @Test
    void testEqualsAndHashCode() {
        EpisodicMemory memory1 = EpisodicMemory.builder()
            .id(1L)
            .userId(100L)
            .episodeId("ep_test")
            .build();

        EpisodicMemory memory2 = EpisodicMemory.builder()
            .id(1L)
            .userId(100L)
            .episodeId("ep_test")
            .build();

        EpisodicMemory memory3 = EpisodicMemory.builder()
            .id(2L)
            .userId(100L)
            .episodeId("ep_test")
            .build();

        // 测试相等性
        assertEquals(memory1, memory2);
        assertNotEquals(memory1, memory3);

        // 测试hashCode一致性
        assertEquals(memory1.hashCode(), memory2.hashCode());
    }
}