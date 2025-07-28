package com.leyue.smartcs.dto.model;

import com.alibaba.cola.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

/**
 * 模型实例分页查询
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ModelPageQry extends PageQuery {
    
    /**
     * 提供商ID（可选）
     */
    private Long providerId;
    
    /**
     * 模型类型（可选，支持多选）
     */
    private List<String> modelType;
    
    /**
     * 状态（可选）
     */
    private String status;
}