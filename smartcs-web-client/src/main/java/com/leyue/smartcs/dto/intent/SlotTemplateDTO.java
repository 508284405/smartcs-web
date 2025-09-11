package com.leyue.smartcs.dto.intent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 槽位模板DTO
 * 
 * @author Claude
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotTemplateDTO {
    
    /**
     * 模板ID
     */
    private String templateId;
    
    /**
     * 模板名称
     */
    private String templateName;
    
    /**
     * 模板描述
     */
    private String description;
    
    /**
     * 意图编码
     */
    private String intentCode;
    
    /**
     * 槽位定义列表
     */
    private List<SlotDefinitionDTO> slotDefinitions;
    
    /**
     * 提示模板（用于生成澄清问题）
     */
    private String promptTemplate;
    
    /**
     * 澄清问题模板
     */
    private Map<String, String> clarificationTemplates;
    
    /**
     * 是否启用槽位填充
     */
    private Boolean slotFillingEnabled;
    
    /**
     * 最大澄清次数
     */
    private Integer maxClarificationAttempts;
    
    /**
     * 是否阻断后续检索（当存在必填槽位缺失时）
     */
    private Boolean blockRetrievalOnMissing;
    
    /**
     * 槽位完整性阈值（0.0-1.0）
     */
    private Double completenessThreshold;
    
    /**
     * 语言代码
     */
    private String language;
    
    /**
     * 版本
     */
    private String version;
    
    /**
     * 扩展配置
     */
    private Map<String, Object> extensions;
    
    /**
     * 检查是否启用了槽位填充功能
     */
    public boolean isSlotFillingActive() {
        return Boolean.TRUE.equals(slotFillingEnabled) && 
               slotDefinitions != null && 
               !slotDefinitions.isEmpty();
    }
}