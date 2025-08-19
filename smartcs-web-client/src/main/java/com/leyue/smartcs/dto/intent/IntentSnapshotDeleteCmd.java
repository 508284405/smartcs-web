package com.leyue.smartcs.dto.intent;

import lombok.Data;

/**
 * 意图快照删除命令
 * 
 * @author Claude
 */
@Data
public class IntentSnapshotDeleteCmd {
    
    /**
     * 快照ID
     */
    private Long snapshotId;
}