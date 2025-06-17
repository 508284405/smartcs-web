package com.leyue.smartcs.mcp;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.config.context.UserContext;
import com.leyue.smartcs.dto.sse.AddressDTO;
import com.leyue.smartcs.dto.sse.OrderCancelRequestDTO;
import com.leyue.smartcs.dto.sse.OrderDTO;
import com.leyue.smartcs.dto.sse.OrderQueryDTO;
import com.leyue.smartcs.mcp.executor.OrderAddressUpdateCmdExe;
import com.leyue.smartcs.mcp.executor.OrderCancelCmdExe;
import com.leyue.smartcs.mcp.executor.OrderConfirmReceiptCmdExe;
import com.leyue.smartcs.mcp.executor.OrderGetQryExe;
import com.leyue.smartcs.mcp.executor.OrderListQryExe;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderToolsService {

    private final OrderListQryExe orderListQryExe;
    private final OrderGetQryExe orderGetQryExe;
    private final OrderConfirmReceiptCmdExe orderConfirmReceiptCmdExe;
    private final OrderCancelCmdExe orderCancelCmdExe;
    private final OrderAddressUpdateCmdExe orderAddressUpdateCmdExe;
//    private final ServletRequest servletRequest;

    @Tool(description = "查询订单")
    public PageResponse<OrderDTO> queryOrder(OrderQueryDTO qry, ToolContext toolContext) {
        return orderListQryExe.execute(qry);
    }
    
    @Tool(description = "查询订单详情")
    public SingleResponse<OrderDTO> getOrder(String orderNumber, ToolContext toolContext) {
        log.info("工具名称{}，查询订单详情，订单编号: {}", "getOrder",orderNumber);
        return orderGetQryExe.execute(orderNumber);
    }
    
    @Tool(description = "确认收货")
    public Response confirmReceipt(String orderNumber, ToolContext toolContext) {
        return orderConfirmReceiptCmdExe.execute(orderNumber);
    }
    
    @Tool(description = "取消订单")
    public Response cancelOrder(String orderNumber, OrderCancelRequestDTO dto, ToolContext toolContext) {
        return orderCancelCmdExe.execute(orderNumber, dto);
    }
    
    @Tool(description = "更新订单收货地址")
    public Response updateOrderAddress(String orderNumber, AddressDTO addressDTO, ToolContext toolContext) {
        return orderAddressUpdateCmdExe.execute(orderNumber, addressDTO);
    }
}
