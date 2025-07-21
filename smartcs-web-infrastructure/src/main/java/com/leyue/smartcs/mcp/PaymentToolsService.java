package com.leyue.smartcs.mcp;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

@Component
public class PaymentToolsService {

    @Tool("支付")
    public String pay() {
        return "支付成功";
    }
}
