package com.leyue.smartcs.dto.intent;

import lombok.Data;
import java.util.List;

/**
 * 意图标签更新命令
 * 
 * @author Claude
 */
@Data
public class IntentLabelsUpdateCmd {
    
    /**
     * 意图ID
     */
    private Long intentId;
    
    /**
     * 标签列表
     */
    private List<String> labels;
}