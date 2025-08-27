package com.leyue.smartcs.dto.intent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 槽位填充测试结果DTO
 * 
 * @author Claude
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotFillingTestResultDTO {
    
    /**
     * 测试是否成功
     */
    private Boolean success;
    
    /**
     * 原始查询语句
     */
    private String originalQuery;
    
    /**
     * 识别的意图编码
     */
    private String intentCode;
    
    /**
     * 已填充的槽位信息
     */
    private Map<String, Object> filledSlots;
    
    /**
     * 缺失的必填槽位列表
     */
    private List<String> missingSlots;
    
    /**
     * 生成的澄清问题列表
     */
    private List<String> clarificationQuestions;
    
    /**
     * 槽位完整性得分（0.0-1.0）
     */
    private Double completenessScore;
    
    /**
     * 是否需要澄清
     */
    private Boolean clarificationRequired;
    
    /**
     * 是否阻断检索
     */
    private Boolean retrievalBlocked;
    
    /**
     * 槽位提取详情
     */
    private Map<String, SlotExtractionDetail> extractionDetails;
    
    /**
     * 错误信息（测试失败时）
     */
    private String errorMessage;
    
    /**
     * 处理时间（毫秒）
     */
    private Long processingTime;
    
    /**
     * 槽位提取详情
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SlotExtractionDetail {
        
        /**
         * 槽位名称
         */
        private String slotName;
        
        /**
         * 提取的原始值
         */
        private String extractedValue;
        
        /**
         * 标准化后的值
         */
        private Object normalizedValue;
        
        /**
         * 置信度得分
         */
        private Double confidence;
        
        /**
         * 提取方法（regex/example/manual等）
         */
        private String extractionMethod;
        
        /**
         * 是否通过验证
         */
        private Boolean validated;
        
        /**
         * 验证错误信息
         */
        private String validationError;
    }
}