package com.leyue.smartcs.dto.model;

import com.alibaba.cola.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 模型提供商分页查询
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProviderPageQry extends PageQuery {
    
    /**
     * 提供商名称（模糊查询）
     */
    private String label;
}