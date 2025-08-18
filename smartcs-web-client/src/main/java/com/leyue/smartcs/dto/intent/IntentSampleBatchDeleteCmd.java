package com.leyue.smartcs.dto.intent;

import lombok.Data;
import java.util.List;

/**
 * 意图样本批量删除命令
 * 
 * @author Claude
 */
@Data
public class IntentSampleBatchDeleteCmd {
    
    /**
     * 样本ID列表
     */
    private List<Long> sampleIds;
}