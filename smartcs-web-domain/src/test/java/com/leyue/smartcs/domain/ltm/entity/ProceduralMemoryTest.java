package com.leyue.smartcs.domain.ltm.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 程序性记忆实体测试
 */
class ProceduralMemoryTest {

    private ProceduralMemory proceduralMemory;

    @BeforeEach
    void setUp() {
        Map<String, Object> triggerConditions = new HashMap<>();
        triggerConditions.put("keyword", "技术问题");
        triggerConditions.put("context", "work");

        proceduralMemory = ProceduralMemory.builder()
            .id(1L)
            .userId(100L)
            .patternType(ProceduralMemory.PatternType.PREFERENCE)
            .patternName("详细技术解答")
            .patternDescription("用户偏好详细的技术解答")
            .triggerConditions(triggerConditions)
            .actionTemplate("提供详细的技术说明和代码示例")
            .successCount(8)
            .failureCount(2)
            .successRate(0.8)
            .lastTriggeredAt(System.currentTimeMillis())
            .learningRate(0.1)
            .isActive(true)
            .createdAt(System.currentTimeMillis())
            .updatedAt(System.currentTimeMillis())
            .build();
    }

    @Test
    void testIsEffective() {
        // 测试有效模式
        proceduralMemory.setSuccessRate(0.8);
        assertTrue(proceduralMemory.isEffective());

        // 测试无效模式
        proceduralMemory.setSuccessRate(0.6);
        assertFalse(proceduralMemory.isEffective());

        // 测试边界值
        proceduralMemory.setSuccessRate(0.7);
        assertTrue(proceduralMemory.isEffective());

        // 测试null值
        proceduralMemory.setSuccessRate(null);
        assertFalse(proceduralMemory.isEffective());
    }

    @Test
    void testIsHighFrequency() {
        // 测试高频模式
        proceduralMemory.setSuccessCount(8);
        proceduralMemory.setFailureCount(2);
        assertTrue(proceduralMemory.isHighFrequency());

        // 测试低频模式
        proceduralMemory.setSuccessCount(3);
        proceduralMemory.setFailureCount(2);
        assertFalse(proceduralMemory.isHighFrequency());

        // 测试边界值
        proceduralMemory.setSuccessCount(7);
        proceduralMemory.setFailureCount(3);
        assertTrue(proceduralMemory.isHighFrequency());

        // 测试null值
        proceduralMemory.setSuccessCount(null);
        proceduralMemory.setFailureCount(null);
        assertFalse(proceduralMemory.isHighFrequency());
    }

    @Test
    void testNeedsAdjustment() {
        // 测试需要调整的模式（低成功率+高频次）
        proceduralMemory.setSuccessRate(0.4);
        proceduralMemory.setSuccessCount(3);
        proceduralMemory.setFailureCount(7);
        assertTrue(proceduralMemory.needsAdjustment());

        // 测试不需要调整的模式（高成功率）
        proceduralMemory.setSuccessRate(0.8);
        assertTrue(proceduralMemory.needsAdjustment()); // 仍然因为高频次

        // 测试低频次模式
        proceduralMemory.setSuccessRate(0.4);
        proceduralMemory.setSuccessCount(2);
        proceduralMemory.setFailureCount(3);
        assertFalse(proceduralMemory.needsAdjustment());

        // 测试null值
        proceduralMemory.setSuccessRate(null);
        assertFalse(proceduralMemory.needsAdjustment());
    }

    @Test
    void testRecordSuccess() {
        Long beforeTime = System.currentTimeMillis();
        int initialCount = proceduralMemory.getSuccessCount();
        
        proceduralMemory.recordSuccess();
        
        // 验证成功次数增加
        assertEquals(initialCount + 1, proceduralMemory.getSuccessCount());
        
        // 验证时间更新
        assertTrue(proceduralMemory.getLastTriggeredAt() >= beforeTime);
        assertTrue(proceduralMemory.getUpdatedAt() >= beforeTime);
        
        // 验证成功率重新计算
        assertNotNull(proceduralMemory.getSuccessRate());
        
        // 测试null初始值
        proceduralMemory.setSuccessCount(null);
        proceduralMemory.recordSuccess();
        assertEquals(1, proceduralMemory.getSuccessCount());
    }

    @Test
    void testRecordFailure() {
        Long beforeTime = System.currentTimeMillis();
        int initialCount = proceduralMemory.getFailureCount();
        
        proceduralMemory.recordFailure();
        
        // 验证失败次数增加
        assertEquals(initialCount + 1, proceduralMemory.getFailureCount());
        
        // 验证时间更新
        assertTrue(proceduralMemory.getLastTriggeredAt() >= beforeTime);
        assertTrue(proceduralMemory.getUpdatedAt() >= beforeTime);
        
        // 验证成功率重新计算
        assertNotNull(proceduralMemory.getSuccessRate());
        
        // 测试null初始值
        proceduralMemory.setFailureCount(null);
        proceduralMemory.recordFailure();
        assertEquals(1, proceduralMemory.getFailureCount());
    }

    @Test
    void testGetTotalAttempts() {
        // 正常情况
        proceduralMemory.setSuccessCount(8);
        proceduralMemory.setFailureCount(2);
        assertEquals(10, proceduralMemory.getTotalAttempts());

        // null值处理
        proceduralMemory.setSuccessCount(null);
        proceduralMemory.setFailureCount(null);
        assertEquals(0, proceduralMemory.getTotalAttempts());

        // 部分null值
        proceduralMemory.setSuccessCount(5);
        proceduralMemory.setFailureCount(null);
        assertEquals(5, proceduralMemory.getTotalAttempts());
    }

    @Test
    void testActivateAndDeactivate() {
        Long beforeTime = System.currentTimeMillis();
        
        // 测试激活
        proceduralMemory.activate();
        assertTrue(proceduralMemory.getIsActive());
        assertTrue(proceduralMemory.getUpdatedAt() >= beforeTime);
        
        // 测试停用
        proceduralMemory.deactivate();
        assertFalse(proceduralMemory.getIsActive());
        assertTrue(proceduralMemory.getUpdatedAt() >= beforeTime);
    }

    @Test
    void testAddTriggerCondition() {
        Long beforeTime = System.currentTimeMillis();
        
        proceduralMemory.addTriggerCondition("urgency", "high");
        
        assertEquals("high", proceduralMemory.getTriggerConditions().get("urgency"));
        assertTrue(proceduralMemory.getUpdatedAt() >= beforeTime);
        
        // 测试覆盖现有条件
        proceduralMemory.addTriggerCondition("keyword", "新关键词");
        assertEquals("新关键词", proceduralMemory.getTriggerConditions().get("keyword"));
    }

    @Test
    void testMatchesTriggerConditions() {
        Map<String, Object> context = new HashMap<>();
        context.put("keyword", "技术问题");
        context.put("context", "work");
        context.put("extra", "ignored");
        
        // 测试完全匹配
        assertTrue(proceduralMemory.matchesTriggerConditions(context));
        
        // 测试部分匹配（缺少条件）
        context.remove("context");
        assertFalse(proceduralMemory.matchesTriggerConditions(context));
        
        // 测试不匹配
        context.put("keyword", "其他问题");
        context.put("context", "work");
        assertFalse(proceduralMemory.matchesTriggerConditions(context));
        
        // 测试空上下文
        assertFalse(proceduralMemory.matchesTriggerConditions(null));
        assertFalse(proceduralMemory.matchesTriggerConditions(new HashMap<>()));
        
        // 测试空触发条件
        proceduralMemory.setTriggerConditions(null);
        assertTrue(proceduralMemory.matchesTriggerConditions(context));
        
        proceduralMemory.setTriggerConditions(new HashMap<>());
        assertTrue(proceduralMemory.matchesTriggerConditions(context));
    }

    @Test
    void testSuccessRateCalculation() {
        // 设置学习率
        proceduralMemory.setLearningRate(0.2);
        proceduralMemory.setSuccessRate(0.6); // 初始成功率
        
        // 记录成功，验证平滑更新
        proceduralMemory.setSuccessCount(7);
        proceduralMemory.setFailureCount(3);
        
        proceduralMemory.recordSuccess(); // 8成功，3失败 = 0.727
        
        // 成功率应该在平滑更新后介于初始值和新计算值之间
        assertTrue(proceduralMemory.getSuccessRate() > 0.6);
        assertTrue(proceduralMemory.getSuccessRate() < 0.727);
    }

    @Test
    void testActiveStatusUpdate() {
        // 测试低成功率导致停用
        proceduralMemory.setSuccessCount(6);
        proceduralMemory.setFailureCount(24); // 20%成功率
        proceduralMemory.setIsActive(true);
        
        proceduralMemory.recordFailure(); // 触发状态更新
        
        assertFalse(proceduralMemory.getIsActive());
        
        // 测试高成功率保持活跃
        proceduralMemory.setSuccessCount(18);
        proceduralMemory.setFailureCount(2); // 90%成功率
        proceduralMemory.setIsActive(false);
        
        proceduralMemory.recordSuccess();
        
        assertTrue(proceduralMemory.getIsActive());
    }

    @Test
    void testPatternTypeConstants() {
        assertEquals("preference", ProceduralMemory.PatternType.PREFERENCE);
        assertEquals("rule", ProceduralMemory.PatternType.RULE);
        assertEquals("habit", ProceduralMemory.PatternType.HABIT);
        assertEquals("response_style", ProceduralMemory.PatternType.RESPONSE_STYLE);
    }

    @Test
    void testBuilderPattern() {
        ProceduralMemory memory = ProceduralMemory.builder()
            .userId(123L)
            .patternType("test_type")
            .patternName("测试模式")
            .isActive(true)
            .build();
        
        assertNotNull(memory);
        assertEquals(123L, memory.getUserId());
        assertEquals("test_type", memory.getPatternType());
        assertEquals("测试模式", memory.getPatternName());
        assertTrue(memory.getIsActive());
    }
}