package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * RAG评估配置查询
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalConfigGetQry {
    
    /**
     * 配置ID
     */
    private String configId;
    
    /**
     * 配置名称
     */
    private String configName;
    
    /**
     * 是否包含默认配置
     */
    private Boolean includeDefaultConfig;
    
    /**
     * 是否包含用户自定义配置
     */
    private Boolean includeUserConfig;
    
    /**
     * 是否包含系统配置
     */
    private Boolean includeSystemConfig;
    
    /**
     * 配置类型列表
     */
    private List<String> configTypes;
    
    /**
     * 是否包含敏感信息
     */
    private Boolean includeSensitiveInfo;
    
    /**
     * 是否包含配置历史
     */
    private Boolean includeConfigHistory;
    
    /**
     * 历史版本数量限制
     */
    private Integer historyLimit;
}
