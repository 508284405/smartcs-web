package com.leyue.smartcs.dto.intent;

import lombok.Data;

/**
 * 意图快照比较命令
 * 
 * @author Claude
 */
@Data
public class IntentSnapshotCompareCmd {
    
    /**
     * 基准快照ID
     */
    private Long baseSnapshotId;
    
    /**
     * 目标快照ID
     */
    private Long targetSnapshotId;
}