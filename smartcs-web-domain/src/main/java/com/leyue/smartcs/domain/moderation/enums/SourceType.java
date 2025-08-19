package com.leyue.smartcs.domain.moderation.enums;

/**
 * 内容来源类型枚举
 */
public enum SourceType {
    
    /**
     * 聊天会话
     */
    CHAT("CHAT", "聊天会话", "来自聊天会话的内容"),
    
    /**
     * 知识库管理
     */
    KNOWLEDGE_BASE("KNOWLEDGE_BASE", "知识库管理", "来自知识库管理的内容"),
    
    /**
     * RAG查询系统
     */
    RAG_QUERY("RAG_QUERY", "RAG查询", "来自RAG查询系统的内容"),
    
    /**
     * 文件上传
     */
    FILE_UPLOAD("FILE_UPLOAD", "文件上传", "来自文件上传的内容"),
    
    /**
     * API接口
     */
    API("API", "API接口", "来自API接口的内容"),
    
    /**
     * Web表单
     */
    WEB_FORM("WEB_FORM", "Web表单", "来自Web表单的内容"),
    
    /**
     * 批量导入
     */
    BATCH_IMPORT("BATCH_IMPORT", "批量导入", "来自批量导入的内容"),
    
    /**
     * 系统生成
     */
    SYSTEM("SYSTEM", "系统生成", "系统自动生成的内容");

    /**
     * 来源编码
     */
    private final String code;

    /**
     * 来源名称
     */
    private final String displayName;

    /**
     * 来源描述
     */
    private final String description;

    SourceType(String code, String displayName, String description) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据编码获取枚举值
     */
    public static SourceType fromCode(String code) {
        for (SourceType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown source type code: " + code);
    }

    /**
     * 判断是否为实时交互来源
     */
    public boolean isRealTimeSource() {
        return this == CHAT || this == RAG_QUERY || this == API;
    }

    /**
     * 判断是否为批处理来源
     */
    public boolean isBatchSource() {
        return this == FILE_UPLOAD || this == BATCH_IMPORT;
    }

    /**
     * 判断是否需要高优先级处理
     */
    public boolean needsHighPriority() {
        return this == CHAT || this == RAG_QUERY;
    }

    /**
     * 判断是否为用户主动触发
     */
    public boolean isUserTriggered() {
        return this != SYSTEM;
    }
}