package com.leyue.smartcs.dto.sse;

import com.alibaba.cola.dto.PageQuery;
import lombok.Data;

/**
 * 订单查询的请求DTO
 */
@Data
public class OrderQueryDTO extends PageQuery {
    /**
     * 订单状态
     */
    private String status;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;
}