package com.leyue.smartcs.dto.intent;

import lombok.Data;
import java.util.List;

/**
 * 意图样本批量导入命令
 * 
 * @author Claude
 */
@Data
public class IntentSampleBatchImportCmd {
    
    /**
     * 意图ID
     */
    private Long intentId;
    
    /**
     * 样本列表
     */
    private List<IntentSampleCreateCmd> samples;
}