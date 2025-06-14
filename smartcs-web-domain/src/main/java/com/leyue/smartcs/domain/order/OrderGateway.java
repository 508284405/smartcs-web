package com.leyue.smartcs.domain.order;

import com.alibaba.cola.dto.PageResponse;
import com.leyue.smartcs.dto.sse.OrderDTO;
import com.leyue.smartcs.dto.sse.OrderQueryDTO;

public interface OrderGateway {
    PageResponse<OrderDTO> listOrders(OrderQueryDTO qry);
}
