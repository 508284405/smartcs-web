package com.leyue.smartcs.dto.intent;

import lombok.Data;
import java.util.Map;

/**
 * 意图困难样本上报命令
 * 
 * @author Claude
 */
@Data
public class IntentHardSampleReportCmd {
    
    /**
     * 原始文本
     */
    private String text;
    
    /**
     * 预测意图
     */
    private String predictedIntent;
    
    /**
     * 置信度
     */
    private Double confidence;
    
    /**
     * 实际意图
     */
    private String actualIntent;
    
    /**
     * 期望意图编码
     */
    private String expectedIntentCode;
    
    /**
     * 渠道
     */
    private String channel;
    
    /**
     * 租户
     */
    private String tenant;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 元数据
     */
    private Map<String, Object> metadata;
}