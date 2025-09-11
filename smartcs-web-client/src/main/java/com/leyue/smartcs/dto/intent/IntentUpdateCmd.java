package com.leyue.smartcs.dto.intent;

import lombok.Data;

/**
 * 意图更新命令
 * 
 * @author Claude
 */
@Data
public class IntentUpdateCmd {
    
    private Long id;
    private Long intentId;
    private String name;
    private String description;
}