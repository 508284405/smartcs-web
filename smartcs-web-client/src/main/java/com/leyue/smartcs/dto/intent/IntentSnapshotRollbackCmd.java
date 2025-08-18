package com.leyue.smartcs.dto.intent;

import lombok.Data;

/**
 * 意图快照回滚命令
 * 
 * @author Claude
 */
@Data
public class IntentSnapshotRollbackCmd {
    
    /**
     * 快照ID
     */
    private Long snapshotId;
    
    /**
     * 回滚原因
     */
    private String rollbackReason;
}