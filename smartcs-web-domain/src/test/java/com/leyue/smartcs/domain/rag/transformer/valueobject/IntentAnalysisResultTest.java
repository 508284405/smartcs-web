package com.leyue.smartcs.domain.rag.transformer.valueobject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 意图分析结果值对象测试
 * 
 * @author Claude
 */
class IntentAnalysisResultTest {

    @Test
    void testCreate() {
        IntentAnalysisResult result = IntentAnalysisResult.create(
            "question", "inquiry", 0.8, "用户询问产品信息"
        );
        
        assertEquals("question", result.getIntentCode());
        assertEquals("inquiry", result.getCatalogCode());
        assertEquals(0.8, result.getConfidenceScore());
        assertEquals("用户询问产品信息", result.getReasoning());
        assertTrue(result.getClassificationTime() > 0);
    }

    @Test
    void testCreateDefault() {
        IntentAnalysisResult result = IntentAnalysisResult.createDefault();
        
        assertEquals("UNKNOWN", result.getIntentCode());
        assertEquals("UNKNOWN", result.getCatalogCode());
        assertEquals(0.0, result.getConfidenceScore());
        assertEquals("意图分析失败，使用默认处理", result.getReasoning());
    }

    @Test
    void testIsHighConfidence() {
        IntentAnalysisResult highConfidence = IntentAnalysisResult.create("question", "inquiry", 0.8, "test");
        IntentAnalysisResult lowConfidence = IntentAnalysisResult.create("question", "inquiry", 0.6, "test");
        
        assertTrue(highConfidence.isHighConfidence());
        assertFalse(lowConfidence.isHighConfidence());
    }

    @Test
    void testIsMediumConfidence() {
        IntentAnalysisResult mediumConfidence = IntentAnalysisResult.create("question", "inquiry", 0.6, "test");
        IntentAnalysisResult highConfidence = IntentAnalysisResult.create("question", "inquiry", 0.8, "test");
        IntentAnalysisResult lowConfidence = IntentAnalysisResult.create("question", "inquiry", 0.3, "test");
        
        assertTrue(mediumConfidence.isMediumConfidence());
        assertFalse(highConfidence.isMediumConfidence());
        assertFalse(lowConfidence.isMediumConfidence());
    }

    @Test
    void testIsLowConfidence() {
        IntentAnalysisResult lowConfidence = IntentAnalysisResult.create("question", "inquiry", 0.3, "test");
        IntentAnalysisResult mediumConfidence = IntentAnalysisResult.create("question", "inquiry", 0.6, "test");
        
        assertTrue(lowConfidence.isLowConfidence());
        assertFalse(mediumConfidence.isLowConfidence());
    }

    @Test
    void testRequiresExpansion() {
        IntentAnalysisResult greeting = IntentAnalysisResult.create("greeting", "social", 0.9, "问候语");
        IntentAnalysisResult question = IntentAnalysisResult.create("question", "inquiry", 0.8, "询问");
        IntentAnalysisResult unknown = IntentAnalysisResult.createDefault();
        
        assertFalse(greeting.requiresExpansion());
        assertTrue(question.requiresExpansion());
        assertTrue(unknown.requiresExpansion());
    }

    @Test
    void testIsGreetingOrGoodbye() {
        IntentAnalysisResult greeting = IntentAnalysisResult.create("greeting", "social", 0.9, "问候语");
        IntentAnalysisResult goodbye = IntentAnalysisResult.create("goodbye", "social", 0.9, "告别语");
        IntentAnalysisResult question = IntentAnalysisResult.create("question", "inquiry", 0.8, "询问");
        
        assertTrue(greeting.isGreetingOrGoodbye());
        assertTrue(goodbye.isGreetingOrGoodbye());
        assertFalse(question.isGreetingOrGoodbye());
    }

    @Test
    void testIsInquiry() {
        IntentAnalysisResult question = IntentAnalysisResult.create("question", "inquiry", 0.8, "询问");
        IntentAnalysisResult inquiry = IntentAnalysisResult.create("inquiry", "inquiry", 0.8, "询问");
        IntentAnalysisResult greeting = IntentAnalysisResult.create("greeting", "social", 0.9, "问候语");
        
        assertTrue(question.isInquiry());
        assertTrue(inquiry.isInquiry());
        assertFalse(greeting.isInquiry());
    }

    @Test
    void testIsComplaint() {
        IntentAnalysisResult complaint = IntentAnalysisResult.create("complaint", "service", 0.8, "投诉");
        IntentAnalysisResult question = IntentAnalysisResult.create("question", "inquiry", 0.8, "询问");
        
        assertTrue(complaint.isComplaint());
        assertFalse(question.isComplaint());
    }

    @Test
    void testIsTechnicalSupport() {
        IntentAnalysisResult technical = IntentAnalysisResult.create("technical_support", "support", 0.8, "技术支持");
        IntentAnalysisResult question = IntentAnalysisResult.create("question", "inquiry", 0.8, "询问");
        
        assertTrue(technical.isTechnicalSupport());
        assertFalse(question.isTechnicalSupport());
    }

    @Test
    void testIsValid() {
        IntentAnalysisResult valid = IntentAnalysisResult.create("question", "inquiry", 0.8, "询问");
        IntentAnalysisResult defaultResult = IntentAnalysisResult.createDefault();
        
        assertTrue(valid.isValid());
        assertFalse(defaultResult.isValid());
    }

    @Test
    void testToString() {
        IntentAnalysisResult result = IntentAnalysisResult.create("question", "inquiry", 0.8, "询问");
        String toString = result.toString();
        
        assertTrue(toString.contains("question"));
        assertTrue(toString.contains("inquiry"));
        assertTrue(toString.contains("0.80"));
    }
}