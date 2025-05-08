package com.leyue.smartcs.dto.common;

import lombok.Data;

/**
 * ID生成器请求对象
 */
@Data
public class IdGeneratorRequest {
    
    /**
     * 批量大小
     */
    private Integer batchSize;
    
    public IdGeneratorRequest() {
        this.batchSize = 1;
    }
    
    public IdGeneratorRequest(Integer batchSize) {
        this.batchSize = batchSize;
    }
} 