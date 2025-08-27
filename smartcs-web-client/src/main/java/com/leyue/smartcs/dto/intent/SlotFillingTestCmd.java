package com.leyue.smartcs.dto.intent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * 槽位填充测试命令
 * 
 * @author Claude
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotFillingTestCmd {
    
    /**
     * 测试查询语句
     */
    @NotBlank(message = "测试查询语句不能为空")
    private String query;
    
    /**
     * 槽位模板（可选，如果不提供则使用意图当前的槽位模板）
     */
    private SlotTemplateDTO slotTemplate;
    
    /**
     * 租户标识（可选，默认使用default）
     */
    private String tenant;
    
    /**
     * 渠道标识（可选，默认使用default）
     */
    private String channel;
    
    /**
     * 领域标识（可选，默认使用default）
     */
    private String domain;
}