package com.leyue.smartcs.dto.eval;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 简化评估请求DTO
 * 用于基准集评估和CI/CD质量闸门
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimpleEvalRequest {
    
    /**
     * 评估项列表
     */
    private List<EvalItem> items;
    
    /**
     * 元数据
     */
    private Map<String, Object> meta;
    
    /**
     * 评估项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EvalItem {
        /**
         * 问题
         */
        private String question;
        
        /**
         * 答案
         */
        private String answer;
        
        /**
         * 检索上下文
         */
        private List<RetrievedContext> retrievedContexts;
        
        /**
         * 标准答案(可选)
         */
        private String groundTruth;
        
        /**
         * 元数据
         */
        private Map<String, Object> meta;
    }
    
    /**
     * 检索上下文
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetrievedContext {
        /**
         * 文档ID
         */
        private String docId;
        
        /**
         * 文本内容
         */
        private String text;
        
        /**
         * 相似度分数
         */
        private Double score;
        
        /**
         * 排名
         */
        private Integer rank;
        
        /**
         * 来源
         */
        private String source;
        
        /**
         * 文档块ID
         */
        private String chunkId;
    }
}