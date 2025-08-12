package com.leyue.smartcs.dto.eval;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 简化评估响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimpleEvalResponse {
    
    /**
     * 评估结果列表
     */
    private List<EvalResult> results;
    
    /**
     * 聚合分数
     */
    private AggregateScores aggregate;
    
    /**
     * 评估结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EvalResult {
        /**
         * 忠实度分数
         */
        private Double faithfulness;
        
        /**
         * 答案相关性分数
         */
        private Double answerRelevancy;
        
        /**
         * 上下文精确度分数
         */
        private Double contextPrecision;
        
        /**
         * 上下文召回度分数
         */
        private Double contextRecall;
        
        /**
         * 噪声敏感度分数
         */
        private Double noiseSensitivity;
        
        /**
         * 元数据
         */
        private Map<String, Object> meta;
    }
    
    /**
     * 聚合分数
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AggregateScores {
        /**
         * 平均忠实度分数
         */
        private Double avgFaithfulness;
        
        /**
         * 平均答案相关性分数
         */
        private Double avgAnswerRelevancy;
        
        /**
         * 平均上下文精确度分数
         */
        private Double avgContextPrecision;
        
        /**
         * 平均上下文召回度分数
         */
        private Double avgContextRecall;
        
        /**
         * 平均噪声敏感度分数
         */
        private Double avgNoiseSensitivity;
        
        /**
         * 评估项总数
         */
        private Integer totalItems;
        
        /**
         * 是否通过阈值检查
         */
        private Boolean passThreshold;
        
        /**
         * 失败的指标列表
         */
        private List<String> failedMetrics;
    }
}