package com.leyue.smartcs.intent.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 意图快照项数据对象，对应t_intent_snapshot_item表
 * 
 * @author Claude
 */
@Data
@TableName("t_intent_snapshot_item")
public class IntentSnapshotItemDO {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 快照ID
     */
    @TableField("snapshot_id")
    private Long snapshotId;
    
    /**
     * 版本ID
     */
    @TableField("version_id")
    private Long versionId;
    
    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Long createdAt;
}