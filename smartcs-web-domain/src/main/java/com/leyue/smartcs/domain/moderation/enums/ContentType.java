package com.leyue.smartcs.domain.moderation.enums;

/**
 * 内容类型枚举
 */
public enum ContentType {
    
    /**
     * 聊天消息
     */
    MESSAGE("MESSAGE", "聊天消息", "用户在聊天会话中发送的消息"),
    
    /**
     * 知识库内容
     */
    KNOWLEDGE("KNOWLEDGE", "知识库内容", "上传到知识库的文档内容"),
    
    /**
     * 文档文件
     */
    DOCUMENT("DOCUMENT", "文档文件", "上传的文档文件"),
    
    /**
     * 常见问题
     */
    FAQ("FAQ", "常见问题", "FAQ问答内容"),
    
    /**
     * RAG查询
     */
    RAG_QUERY("RAG_QUERY", "RAG查询", "RAG系统的查询请求"),
    
    /**
     * 用户评论
     */
    COMMENT("COMMENT", "用户评论", "用户发表的评论内容"),
    
    /**
     * 反馈内容
     */
    FEEDBACK("FEEDBACK", "反馈内容", "用户提交的反馈内容");

    /**
     * 类型编码
     */
    private final String code;

    /**
     * 类型名称
     */
    private final String displayName;

    /**
     * 类型描述
     */
    private final String description;

    ContentType(String code, String displayName, String description) {
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
    public static ContentType fromCode(String code) {
        for (ContentType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown content type code: " + code);
    }

    /**
     * 判断是否为用户生成内容
     */
    public boolean isUserGenerated() {
        return this == MESSAGE || this == COMMENT || this == FEEDBACK || this == RAG_QUERY;
    }

    /**
     * 判断是否为系统管理内容
     */
    public boolean isSystemManaged() {
        return this == KNOWLEDGE || this == DOCUMENT || this == FAQ;
    }

    /**
     * 判断是否需要实时审核
     */
    public boolean needsRealTimeModeration() {
        return this == MESSAGE || this == RAG_QUERY || this == COMMENT;
    }

    /**
     * 判断是否可以延迟审核
     */
    public boolean canBeDeferredModeration() {
        return this == KNOWLEDGE || this == DOCUMENT || this == FAQ || this == FEEDBACK;
    }
}