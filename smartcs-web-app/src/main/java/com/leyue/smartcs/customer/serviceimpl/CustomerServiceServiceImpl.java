package com.leyue.smartcs.customer.serviceimpl;

import com.alibaba.cola.dto.MultiResponse;
import com.leyue.smartcs.api.AgentServic;
import com.leyue.smartcs.domain.customer.CustomerService;
import com.leyue.smartcs.domain.customer.gateway.AgentGateway;
import com.leyue.smartcs.domain.customer.gateway.AgentGateway.CustomerServiceStatistics;
import com.leyue.smartcs.dto.AgentServiceListQry;
import com.leyue.smartcs.dto.data.AgentServiceDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 客服管理服务实现
 */
@Service
public class CustomerServiceServiceImpl implements AgentServic {
    
    @Autowired
    private AgentGateway agentGateway;
    
    @Override
    public MultiResponse<AgentServiceDTO> getAgentList(AgentServiceListQry qry) {
        // 1. 获取所有客服ID
        List<String> allServiceIds = agentGateway.getAllAgentIds();
        
        // 2. 从用户中心获取客服基础信息
        List<CustomerService> customerServices = agentGateway.getAgentFromUserCenter(allServiceIds);
        
        // 3. 获取客服统计数据
        Map<String, CustomerServiceStatistics> statisticsMap = agentGateway.getAgentStatistics(allServiceIds);
        
        // 4. 组装数据
        List<AgentServiceDTO> resultList = customerServices.stream()
                .map(cs -> {
                    AgentServiceDTO dto = new AgentServiceDTO();
                    dto.setServiceId(cs.getServiceId());
                    dto.setServiceName(cs.getServiceName());
                    dto.setStatus(cs.getStatus());
                    
                    // 设置统计数据
                    CustomerServiceStatistics statistics = statisticsMap.get(cs.getServiceId());
                    if (statistics != null) {
                        dto.setActiveSessions(statistics.getActiveSessions());
                        dto.setTotalSessions(statistics.getTotalSessions());
                        dto.setLastActiveTime(statistics.getLastActiveTime());
                    } else {
                        dto.setActiveSessions(0);
                        dto.setTotalSessions(0);
                        dto.setLastActiveTime(null);
                    }
                    
                    dto.setOperation("查看详情");
                    return dto;
                })
                .collect(Collectors.toList());
        
        return MultiResponse.of(resultList);
    }
} 