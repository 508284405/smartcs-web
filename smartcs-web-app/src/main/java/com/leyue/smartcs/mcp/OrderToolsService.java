package com.leyue.smartcs.mcp;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import com.alibaba.cola.dto.PageResponse;
import com.leyue.smartcs.dto.sse.OrderDTO;
import com.leyue.smartcs.dto.sse.OrderQueryDTO;
import com.leyue.smartcs.mcp.executor.OrderListQryExe;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderToolsService {

    private final OrderListQryExe orderListQryExe;

    @Tool(description = "查询订单")
    public PageResponse<OrderDTO> queryOrder(OrderQueryDTO qry,ToolContext toolContext) {
        return orderListQryExe.execute(qry);
    }
}
