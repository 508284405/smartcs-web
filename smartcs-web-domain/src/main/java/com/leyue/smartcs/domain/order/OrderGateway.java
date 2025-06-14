package com.leyue.smartcs.domain.order;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.dto.sse.OrderDTO;
import com.leyue.smartcs.dto.sse.OrderQueryDTO;
import com.leyue.smartcs.dto.sse.OrderAddressUpdateDTO;

public interface OrderGateway {
    PageResponse<OrderDTO> listOrders(OrderQueryDTO qry);
    
    SingleResponse<OrderDTO> getOrder(String orderNumber);
    
    Response confirmReceipt(String orderNumber);
    
    Response cancelOrder(String orderNumber, String reason);
    
    Response updateOrderAddress(OrderAddressUpdateDTO dto);
}
