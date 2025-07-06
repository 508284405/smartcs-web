package com.leyue.smartcs.web.agent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.cola.dto.MultiResponse;
import com.leyue.smartcs.api.AgentServic;
import com.leyue.smartcs.dto.AgentServiceListQry;
import com.leyue.smartcs.dto.data.AgentServiceDTO;

/**
 * 管理端客服控制器
 */
@RestController
@RequestMapping("/admin/agent")
public class AdminAgentController {
    
    @Autowired
    private AgentServic agentServic;
    
    /**
     * 获取客服列表
     * @param qry 查询条件
     * @return 客服列表
     */
    @GetMapping("/list")
    public MultiResponse<AgentServiceDTO> getAgentList(AgentServiceListQry qry) {
        return agentServic.getAgentList(qry);
    }
}