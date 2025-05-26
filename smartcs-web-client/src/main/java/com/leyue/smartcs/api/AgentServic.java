package com.leyue.smartcs.api;

import com.alibaba.cola.dto.MultiResponse;
import com.leyue.smartcs.dto.AgentServiceListQry;
import com.leyue.smartcs.dto.data.AgentServiceDTO;

/**
 * 客服管理API接口
 */
public interface AgentServic {
    
    /**
     * 获取客服列表
     * @param qry 查询条件
     * @return 客服列表
     */
    MultiResponse<AgentServiceDTO> getAgentList(AgentServiceListQry qry);
} 