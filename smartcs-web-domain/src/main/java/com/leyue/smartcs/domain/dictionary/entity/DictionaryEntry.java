package com.leyue.smartcs.domain.dictionary.entity;

import com.leyue.smartcs.domain.dictionary.enums.DictionaryType;
import com.leyue.smartcs.domain.dictionary.enums.EntryStatus;
import com.leyue.smartcs.domain.dictionary.valueobject.DictionaryConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 字典条目实体
 * 字典模块的核心聚合根，代表一个具体的字典数据条目
 * 
 * 设计原则：
 * - 遵循DDD聚合根设计，封装业务规则和不变量
 * - 支持多租户、多渠道、多领域的配置隔离
 * - 提供版本管理和状态控制
 * - 确保数据一致性和业务规则完整性
 * 
 * @author Claude
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DictionaryEntry {
    
    /**
     * 条目唯一标识
     */
    private Long id;
    
    /**
     * 字典类型
     */
    private DictionaryType dictionaryType;
    
    /**
     * 配置信息（租户、渠道、领域）
     */
    private DictionaryConfig config;
    
    /**
     * 条目键
     * 在同一配置下的同一字典类型中必须唯一
     */
    private String entryKey;
    
    /**
     * 条目值（JSON格式）
     * 根据字典类型的数据结构要求存储相应的JSON数据
     */
    private String entryValue;
    
    /**
     * 描述说明
     */
    private String description;
    
    /**
     * 条目状态
     */
    private EntryStatus status;
    
    /**
     * 优先级
     * 数值越大优先级越高，用于同键条目的覆盖顺序
     */
    private Integer priority;
    
    /**
     * 版本号
     * 每次更新递增，用于乐观锁控制
     */
    private Long version;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 创建人
     */
    private String createdBy;
    
    /**
     * 更新人
     */
    private String updatedBy;
    
    /**
     * 创建新的字典条目
     * 工厂方法，确保创建时的业务规则
     * 
     * @param dictionaryType 字典类型
     * @param config 配置信息
     * @param entryKey 条目键
     * @param entryValue 条目值
     * @param description 描述
     * @param createdBy 创建人
     * @return 新创建的字典条目
     */
    public static DictionaryEntry create(DictionaryType dictionaryType, 
                                       DictionaryConfig config,
                                       String entryKey, 
                                       String entryValue,
                                       String description, 
                                       String createdBy) {
        // 参数校验
        validateCreateParams(dictionaryType, config, entryKey, entryValue, createdBy);
        
        LocalDateTime now = LocalDateTime.now();
        
        return DictionaryEntry.builder()
                .dictionaryType(dictionaryType)
                .config(config)
                .entryKey(entryKey.trim())
                .entryValue(entryValue.trim())
                .description(description != null ? description.trim() : null)
                .status(EntryStatus.ACTIVE)
                .priority(100) // 默认优先级
                .version(1L) // 初始版本
                .createTime(now)
                .updateTime(now)
                .createdBy(createdBy)
                .updatedBy(createdBy)
                .build();
    }
    
    /**
     * 更新条目内容
     * 
     * @param newValue 新的条目值
     * @param newDescription 新的描述
     * @param updatedBy 更新人
     */
    public void updateContent(String newValue, String newDescription, String updatedBy) {
        validateUpdateParams(newValue, updatedBy);
        
        this.entryValue = newValue.trim();
        this.description = newDescription != null ? newDescription.trim() : null;
        this.updateTime = LocalDateTime.now();
        this.updatedBy = updatedBy;
        this.version++; // 版本递增
    }
    
    /**
     * 更新优先级
     * 
     * @param newPriority 新优先级
     * @param updatedBy 更新人
     */
    public void updatePriority(Integer newPriority, String updatedBy) {
        if (newPriority == null || newPriority < 0) {
            throw new IllegalArgumentException("优先级不能为空且必须为非负数");
        }
        if (updatedBy == null || updatedBy.trim().isEmpty()) {
            throw new IllegalArgumentException("更新人不能为空");
        }
        
        this.priority = newPriority;
        this.updateTime = LocalDateTime.now();
        this.updatedBy = updatedBy;
        this.version++; // 版本递增
    }
    
    /**
     * 激活条目
     * 
     * @param updatedBy 更新人
     */
    public void activate(String updatedBy) {
        changeStatus(EntryStatus.ACTIVE, updatedBy);
    }
    
    /**
     * 停用条目
     * 
     * @param updatedBy 更新人
     */
    public void deactivate(String updatedBy) {
        changeStatus(EntryStatus.INACTIVE, updatedBy);
    }
    
    /**
     * 设为草稿状态
     * 
     * @param updatedBy 更新人
     */
    public void setToDraft(String updatedBy) {
        changeStatus(EntryStatus.DRAFT, updatedBy);
    }
    
    /**
     * 检查条目是否处于活跃状态
     * 
     * @return 是否活跃
     */
    public boolean isActive() {
        return EntryStatus.ACTIVE.equals(this.status);
    }
    
    /**
     * 检查条目是否为草稿状态
     * 
     * @return 是否为草稿
     */
    public boolean isDraft() {
        return EntryStatus.DRAFT.equals(this.status);
    }
    
    /**
     * 获取配置标识字符串
     * 格式: tenant:channel:domain
     * 
     * @return 配置标识
     */
    public String getConfigIdentifier() {
        return config != null ? config.getIdentifier() : null;
    }
    
    /**
     * 生成条目的唯一业务键
     * 格式: dictionaryType:tenant:channel:domain:entryKey
     * 
     * @return 唯一业务键
     */
    public String getBusinessKey() {
        if (dictionaryType == null || config == null || entryKey == null) {
            return null;
        }
        return String.format("%s:%s:%s", dictionaryType.getCode(), config.getIdentifier(), entryKey);
    }
    
    /**
     * 改变状态的通用方法
     */
    private void changeStatus(EntryStatus newStatus, String updatedBy) {
        if (newStatus == null) {
            throw new IllegalArgumentException("状态不能为空");
        }
        if (updatedBy == null || updatedBy.trim().isEmpty()) {
            throw new IllegalArgumentException("更新人不能为空");
        }
        
        this.status = newStatus;
        this.updateTime = LocalDateTime.now();
        this.updatedBy = updatedBy;
        this.version++; // 版本递增
    }
    
    /**
     * 创建参数校验
     */
    private static void validateCreateParams(DictionaryType dictionaryType, 
                                           DictionaryConfig config,
                                           String entryKey, 
                                           String entryValue, 
                                           String createdBy) {
        if (dictionaryType == null) {
            throw new IllegalArgumentException("字典类型不能为空");
        }
        if (config == null) {
            throw new IllegalArgumentException("配置信息不能为空");
        }
        if (entryKey == null || entryKey.trim().isEmpty()) {
            throw new IllegalArgumentException("条目键不能为空");
        }
        if (entryValue == null || entryValue.trim().isEmpty()) {
            throw new IllegalArgumentException("条目值不能为空");
        }
        if (createdBy == null || createdBy.trim().isEmpty()) {
            throw new IllegalArgumentException("创建人不能为空");
        }
        
        // 条目键长度限制
        if (entryKey.trim().length() > 200) {
            throw new IllegalArgumentException("条目键长度不能超过200个字符");
        }
        
        // 条目值长度限制
        if (entryValue.trim().length() > 4000) {
            throw new IllegalArgumentException("条目值长度不能超过4000个字符");
        }
    }
    
    /**
     * 更新参数校验
     */
    private void validateUpdateParams(String newValue, String updatedBy) {
        if (newValue == null || newValue.trim().isEmpty()) {
            throw new IllegalArgumentException("条目值不能为空");
        }
        if (updatedBy == null || updatedBy.trim().isEmpty()) {
            throw new IllegalArgumentException("更新人不能为空");
        }
        
        // 条目值长度限制
        if (newValue.trim().length() > 4000) {
            throw new IllegalArgumentException("条目值长度不能超过4000个字符");
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DictionaryEntry that = (DictionaryEntry) o;
        return Objects.equals(id, that.id) && Objects.equals(getBusinessKey(), that.getBusinessKey());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, getBusinessKey());
    }
    
    @Override
    public String toString() {
        return String.format("DictionaryEntry{id=%d, type=%s, config=%s, key='%s', status=%s, version=%d}", 
                id, dictionaryType != null ? dictionaryType.getCode() : null, 
                getConfigIdentifier(), entryKey, status, version);
    }
}