package com.leyue.smartcs.dto.eval.event;

/**
 * 评估系统常量定义
 */
public final class EvalConstants {
    
    private EvalConstants() {}
    
    /**
     * Kafka主题名称
     */
    public static final class Topics {
        public static final String RAG_EVENTS = "rag.events";
        public static final String RAG_EVENTS_DLQ = "rag.events.dlq";
        public static final String RAG_EVAL_IN = "rag.eval.in";
        public static final String RAG_EVAL_IN_DLQ = "rag.eval.in.dlq";
    }
    
    /**
     * HTTP请求头
     */
    public static final class Headers {
        public static final String RAG_EVAL_FORCE = "X-RAG-EVAL";
        public static final String RAG_EVAL_FORCE_VALUE = "true";
    }
    
    /**
     * 采样配置
     */
    public static final class Sampling {
        public static final double DEFAULT_RATE = 0.05; // 5%
        public static final String FORCE_HEADER = "X-RAG-EVAL";
    }
    
    /**
     * 评估指标名称
     */
    public static final class Metrics {
        public static final String FAITHFULNESS = "faithfulness";
        public static final String ANSWER_RELEVANCY = "answer_relevancy";
        public static final String CONTEXT_PRECISION = "context_precision";
        public static final String CONTEXT_RECALL = "context_recall";
        public static final String NOISE_SENSITIVITY = "noise_sensitivity";
    }
    
    /**
     * 默认SLO阈值
     */
    public static final class DefaultThresholds {
        public static final double FAITHFULNESS_MIN = 0.85;
        public static final double ANSWER_RELEVANCY_MIN = 0.80;
        public static final double CONTEXT_PRECISION_MIN = 0.70;
        public static final double CONTEXT_RECALL_MIN = 0.75;
        public static final double NOISE_SENSITIVITY_MAX_VARIANCE = 0.10;
    }
}