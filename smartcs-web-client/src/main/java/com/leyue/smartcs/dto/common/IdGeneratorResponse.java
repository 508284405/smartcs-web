package com.leyue.smartcs.dto.common;

import lombok.Data;

import java.util.List;

/**
 * ID生成器响应对象
 */
@Data
public class IdGeneratorResponse {
    /**
     * 生成的单个ID
     */
    private Long id;
    
    /**
     * 批量生成的ID列表
     */
    private List<Long> idList;
    
    /**
     * 数据中心ID
     */
    private Integer dataCenterId;
    
    /**
     * 机器ID
     */
    private Integer machineId;
    
    /**
     * 生成时间戳
     */
    private Long timestamp;
} 