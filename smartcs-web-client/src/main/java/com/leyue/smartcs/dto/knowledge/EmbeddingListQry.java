package com.leyue.smartcs.dto.knowledge;

import com.alibaba.cola.dto.PageQuery;
import com.leyue.smartcs.dto.knowledge.enums.StrategyNameEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotNull;

/**
 * 向量数据分页查询
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EmbeddingListQry extends PageQuery {
    
    /**
     * 文档ID（必填）
     */
    @NotNull(message = "文档ID不能为空")
    private Long docId;
    
    /**
     * 策略名称（必填）
     */
    @NotNull(message = "策略名称不能为空")
    private StrategyNameEnum strategyName;
} 