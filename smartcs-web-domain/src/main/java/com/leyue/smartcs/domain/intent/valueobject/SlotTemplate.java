package com.leyue.smartcs.domain.intent.valueobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 槽位模板值对象
 * 定义意图级别的槽位模板配置
 * 
 * @author Claude
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotTemplate {
    
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
    private List<SlotDefinition> slotDefinitions;
    
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
     * 创建时间
     */
    private Long createdAt;
    
    /**
     * 更新时间
     */
    private Long updatedAt;
    
    /**
     * 获取必填槽位定义
     */
    public List<SlotDefinition> getRequiredSlots() {
        return slotDefinitions == null ? List.of() : 
               slotDefinitions.stream()
                   .filter(slot -> Boolean.TRUE.equals(slot.getRequired()))
                   .toList();
    }
    
    /**
     * 获取可选槽位定义
     */
    public List<SlotDefinition> getOptionalSlots() {
        return slotDefinitions == null ? List.of() : 
               slotDefinitions.stream()
                   .filter(slot -> !Boolean.TRUE.equals(slot.getRequired()))
                   .toList();
    }
    
    /**
     * 根据名称查找槽位定义
     */
    public SlotDefinition findSlotByName(String slotName) {
        if (slotDefinitions == null || slotName == null) {
            return null;
        }
        return slotDefinitions.stream()
                .filter(slot -> slotName.equals(slot.getName()))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 检查是否启用了槽位填充功能
     */
    public boolean isSlotFillingActive() {
        return Boolean.TRUE.equals(slotFillingEnabled) && 
               slotDefinitions != null && 
               !slotDefinitions.isEmpty();
    }
}