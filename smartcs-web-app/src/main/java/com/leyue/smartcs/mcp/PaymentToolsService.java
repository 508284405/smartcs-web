package com.leyue.smartcs.mcp;

import org.springframework.stereotype.Service;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;

@Service
public class PaymentToolsService {

    @Tool(description = "支付")
    public String pay(ToolContext toolContext) {
        return "支付成功";
    }
}
