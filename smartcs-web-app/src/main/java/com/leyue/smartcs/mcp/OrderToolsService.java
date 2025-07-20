package com.leyue.smartcs.mcp;

import dev.langchain4j.agent.tool.Tool;
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

    @Tool("查询订单")
    public PageResponse<OrderDTO> queryOrder(OrderQueryDTO qry) {
        return orderListQryExe.execute(qry);
    }
    
    @Tool("查询订单详情")
    public SingleResponse<OrderDTO> getOrder(String orderNumber) {
        log.info("工具名称{}，查询订单详情，订单编号: {}", "getOrder",orderNumber);
        return orderGetQryExe.execute(orderNumber);
    }
    
    @Tool("确认收货")
    public Response confirmReceipt(String orderNumber) {
        return orderConfirmReceiptCmdExe.execute(orderNumber);
    }
    
    @Tool("取消订单")
    public Response cancelOrder(String orderNumber, OrderCancelRequestDTO dto) {
        return orderCancelCmdExe.execute(orderNumber, dto);
    }
    
    @Tool("更新订单收货地址")
    public Response updateOrderAddress(String orderNumber, AddressDTO addressDTO) {
        return orderAddressUpdateCmdExe.execute(orderNumber, addressDTO);
    }
}
