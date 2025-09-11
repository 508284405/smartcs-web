package com.leyue.smartcs.dto.dictionary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 字典条目批量删除结果
 * 
 * @author Claude
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DictionaryBatchDeleteResult {
    
    /**
     * 总数
     */
    private Integer totalCount;
    
    /**
     * 成功数量
     */
    private Integer successCount;
    
    /**
     * 失败数量
     */
    private Integer failCount;
    
    /**
     * 成功删除的条目ID列表
     */
    private List<Long> successIds;
    
    /**
     * 失败详情列表
     */
    private List<FailureDetail> failureDetails;
    
    /**
     * 失败详情
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FailureDetail {
        
        /**
         * 失败的条目ID
         */
        private Long id;
        
        /**
         * 失败原因
         */
        private String reason;
    }
}