package com.leyue.smartcs.domain.moderation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.leyue.smartcs.domain.moderation.enums.SeverityLevel;
import com.leyue.smartcs.domain.moderation.enums.ActionType;
import java.util.List;

/**
 * 内容审核违规分类领域实体
 * 支持树形结构的二级分类管理
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ModerationCategory {

    /**
     * 分类ID
     */
    private Long id;

    /**
     * 父分类ID，NULL表示一级分类
     */
    private Long parentId;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类编码，用于程序识别
     */
    private String code;

    /**
     * 分类描述
     */
    private String description;

    /**
     * 严重程度级别
     */
    private SeverityLevel severityLevel;

    /**
     * 默认处理动作类型
     */
    private ActionType actionType;

    /**
     * 是否启用
     */
    private Boolean isActive;

    /**
     * 排序权重
     */
    private Integer sortOrder;

    /**
     * 子分类列表（仅在需要构建树形结构时使用）
     */
    private List<ModerationCategory> children;

    /**
     * 创建者
     */
    private String createdBy;

    /**
     * 更新者
     */
    private String updatedBy;

    /**
     * 创建时间（毫秒时间戳）
     */
    private Long createdAt;

    /**
     * 更新时间（毫秒时间戳）
     */
    private Long updatedAt;

    /**
     * 判断是否为一级分类
     */
    public boolean isTopLevel() {
        return parentId == null;
    }

    /**
     * 判断是否为子分类
     */
    public boolean isSubCategory() {
        return parentId != null;
    }

    /**
     * 判断是否有子分类
     */
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    /**
     * 获取分类层级深度
     */
    public int getDepth() {
        return isTopLevel() ? 1 : 2;
    }

    /**
     * 验证分类数据的完整性
     */
    public boolean isValid() {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        if (code == null || code.trim().isEmpty()) {
            return false;
        }
        if (severityLevel == null || actionType == null) {
            return false;
        }
        // 如果是子分类，必须有parentId
        if (parentId == null && getDepth() == 2) {
            return false;
        }
        return true;
    }

    /**
     * 获取显示用的完整路径名称
     */
    public String getFullDisplayName() {
        if (isTopLevel()) {
            return name;
        }
        // 对于子分类，需要从外部获取父分类名称
        return name;  // 简化实现，实际可能需要包含父分类名称
    }

    /**
     * 比较排序权重
     */
    public int compareTo(ModerationCategory other) {
        if (other == null) {
            return 1;
        }
        
        // 首先按层级排序（一级分类在前）
        int depthCompare = Integer.compare(this.getDepth(), other.getDepth());
        if (depthCompare != 0) {
            return depthCompare;
        }
        
        // 同层级按排序权重排序
        int sortCompare = Integer.compare(
            this.sortOrder != null ? this.sortOrder : Integer.MAX_VALUE,
            other.sortOrder != null ? other.sortOrder : Integer.MAX_VALUE
        );
        if (sortCompare != 0) {
            return sortCompare;
        }
        
        // 最后按名称排序
        return this.name.compareTo(other.name);
    }

    /**
     * 创建新的分类实体（工厂方法）
     */
    public static ModerationCategory create(String name, String code, String description,
                                          SeverityLevel severityLevel, ActionType actionType,
                                          Long parentId, Integer sortOrder, String createdBy) {
        long currentTime = System.currentTimeMillis();
        return ModerationCategory.builder()
                .name(name)
                .code(code)
                .description(description)
                .severityLevel(severityLevel)
                .actionType(actionType)
                .parentId(parentId)
                .sortOrder(sortOrder != null ? sortOrder : 0)
                .isActive(true)
                .createdBy(createdBy)
                .updatedBy(createdBy)
                .createdAt(currentTime)
                .updatedAt(currentTime)
                .build();
    }

    /**
     * 更新分类信息
     */
    public void update(String name, String description, SeverityLevel severityLevel,
                      ActionType actionType, Integer sortOrder, String updatedBy) {
        this.name = name;
        this.description = description;
        this.severityLevel = severityLevel;
        this.actionType = actionType;
        this.sortOrder = sortOrder;
        this.updatedBy = updatedBy;
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * 启用分类
     */
    public void enable(String updatedBy) {
        this.isActive = true;
        this.updatedBy = updatedBy;
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * 禁用分类
     */
    public void disable(String updatedBy) {
        this.isActive = false;
        this.updatedBy = updatedBy;
        this.updatedAt = System.currentTimeMillis();
    }
}