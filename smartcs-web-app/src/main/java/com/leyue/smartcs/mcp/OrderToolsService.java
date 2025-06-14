package com.leyue.smartcs.mcp;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class OrderToolsService {

    @Tool(description = "下单")
    public String order(String orderId,ToolContext toolContext) {
        return orderId + "下单成功";
    }
}
