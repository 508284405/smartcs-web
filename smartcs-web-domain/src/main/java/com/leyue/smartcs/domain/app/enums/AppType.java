package com.leyue.smartcs.domain.app.enums;

/**
 * AI应用类型枚举
 */
public enum AppType {
    /**
     * 工作流 - 面向复杂任务的编排工作流
     */
    WORKFLOW("工作流", "面向单次运行的任务编排工作流"),
    
    /**
     * Chatflow - 支持记忆的复杂对话工作流  
     */
    CHATFLOW("Chatflow", "支持记忆的复杂对话工作流"),
    
    /**
     * 聊天助手 - 简单配置的智能助手
     */
    CHAT_ASSISTANT("聊天助手", "简单配置的智能助手"),
    
    /**
     * Agent - 具备推理能力和自主工具调用的智能助手
     */
    AGENT("Agent", "具备推理能力和自主工具调用的智能助手");
    
    private final String name;
    private final String description;
    
    AppType(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
}