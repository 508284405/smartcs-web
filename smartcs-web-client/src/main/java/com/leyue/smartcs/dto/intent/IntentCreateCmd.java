package com.leyue.smartcs.dto.intent;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 意图创建命令
 * 
 * @author Claude
 */
@Data
public class IntentCreateCmd {
    
    /**
     * 目录ID
     */
    private Long catalogId;
    
    /**
     * 意图名称
     */
    private String name;
    
    /**
     * 意图编码
     */
    private String code;
    
    /**
     * 意图描述
     */
    private String description;
    
    /**
     * 标签数组
     */
    private List<String> labels;
    
    /**
     * 边界定义
     */
    private Map<String, Object> boundaries;
}