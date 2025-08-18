package com.leyue.smartcs.dto.intent;

import lombok.Data;
import java.util.List;

/**
 * 意图边界更新命令
 * 
 * @author Claude
 */
@Data
public class IntentBoundariesUpdateCmd {
    
    /**
     * 意图ID
     */
    private Long intentId;
    
    /**
     * 边界列表
     */
    private List<String> boundaries;
}