package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * RAG评估重新运行命令
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalRunRerunCmd {
    
    /**
     * 原始运行ID
     */
    private String originalRunId;
    
    /**
     * 新的运行名称
     */
    private String newRunName;
    
    /**
     * 新的运行描述
     */
    private String newRunDescription;
    
    /**
     * 是否使用相同的配置
     */
    private Boolean useSameConfig;
    
    /**
     * 覆盖的配置参数
     */
    private Map<String, Object> overrideConfig;
    
    /**
     * 是否包含原始运行的所有测试用例
     */
    private Boolean includeAllCases;
    
    /**
     * 指定要包含的测试用例ID列表
     */
    private java.util.List<String> includeCaseIds;
    
    /**
     * 是否跳过失败的测试用例
     */
    private Boolean skipFailedCases;
}
