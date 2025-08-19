package com.leyue.smartcs.dto.intent;

import lombok.Data;
import java.util.List;

/**
 * 意图快照比较结果DTO
 * 
 * @author Claude
 */
@Data
public class IntentSnapshotCompareResultDTO {
    
    /**
     * 基准快照ID
     */
    private Long baseSnapshotId;
    
    /**
     * 目标快照ID
     */
    private Long targetSnapshotId;
    
    /**
     * 新增的意图
     */
    private List<IntentDTO> addedIntents;
    
    /**
     * 删除的意图
     */
    private List<IntentDTO> removedIntents;
    
    /**
     * 修改的意图
     */
    private List<IntentDTO> modifiedIntents;
    
    /**
     * 比较时间
     */
    private Long compareTime;
}