package com.leyue.smartcs.domain.model.enums;

/**
 * 模型特性枚举
 */
public enum Feature {
    /**
     * 工具调用
     */
    TOOL_CALL("tool-call", "工具调用"),
    
    /**
     * 智能体思维
     */
    AGENT_THOUGHT("agent-thought", "智能体思维"),
    
    /**
     * 多工具调用
     */
    MULTI_TOOL_CALL("multi-tool-call", "多工具调用"),
    
    /**
     * 流式工具调用
     */
    STREAM_TOOL_CALL("stream-tool-call", "流式工具调用");
    
    private final String code;
    private final String displayName;
    
    Feature(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * 根据代码获取枚举值
     */
    public static Feature fromCode(String code) {
        if (code == null) {
            return null;
        }
        
        for (Feature feature : values()) {
            if (feature.code.equals(code)) {
                return feature;
            }
        }
        
        throw new IllegalArgumentException("Unknown feature code: " + code);
    }
}