package com.leyue.smartcs.dto.intent;

import lombok.Data;

/**
 * 意图服务信息DTO
 * 
 * @author Claude
 */
@Data
public class IntentServiceInfoDTO {
    
    /**
     * 服务名称
     */
    private String serviceName;
    
    /**
     * 版本号
     */
    private String version;
    
    /**
     * 时间戳
     */
    private Long timestamp;
    
    /**
     * 服务状态
     */
    private String status;
}