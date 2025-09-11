package com.leyue.smartcs.domain.app.enums;

/**
 * AI应用状态枚举
 */
public enum AppStatus {
    /**
     * 草稿状态
     */
    DRAFT("草稿", "应用处于草稿状态，未发布"),
    
    /**
     * 已发布状态
     */
    PUBLISHED("已发布", "应用已发布，可以使用"),
    
    /**
     * 已停用状态
     */
    DISABLED("已停用", "应用已停用，不可使用");
    
    private final String name;
    private final String description;
    
    AppStatus(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 检查是否可以使用
     * @return 是否可用
     */
    public boolean isUsable() {
        return this == PUBLISHED;
    }
    
    /**
     * 检查是否可以编辑
     * @return 是否可编辑
     */
    public boolean isEditable() {
        return this == DRAFT;
    }
}