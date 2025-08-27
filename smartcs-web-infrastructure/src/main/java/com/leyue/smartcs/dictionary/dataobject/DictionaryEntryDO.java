package com.leyue.smartcs.dictionary.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字典条目数据对象
 * 对应数据库表 t_dictionary_entry
 * 
 * @author Claude
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_dictionary_entry")
public class DictionaryEntryDO {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 字典类型
     */
    private String dictionaryType;
    
    /**
     * 租户标识
     */
    private String tenant;
    
    /**
     * 渠道标识
     */
    private String channel;
    
    /**
     * 领域标识
     */
    private String domain;
    
    /**
     * 条目键
     */
    private String entryKey;
    
    /**
     * 条目值（JSON格式）
     */
    private String entryValue;
    
    /**
     * 描述说明
     */
    private String description;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 优先级
     */
    private Integer priority;
    
    /**
     * 版本号
     */
    @Version
    private Long version;
    
    /**
     * 逻辑删除标记
     */
    @TableLogic
    private Integer isDeleted;
    
    /**
     * 创建时间戳
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createdAt;
    
    /**
     * 更新时间戳
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedAt;
    
    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;
    
    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;
}