package com.leyue.smartcs.customer.gatewayimpl;

import com.leyue.smartcs.common.dao.UserCenterCustomerServiceDTO;
import com.leyue.smartcs.common.feign.UserCenterClient;
import com.leyue.smartcs.customer.convertor.CustomerServiceConvertor;
import com.leyue.smartcs.domain.customer.CustomerService;
import com.leyue.smartcs.domain.customer.gateway.AgentGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 客服Gateway实现
 */
@Component
public class AgentGatewayImpl implements AgentGateway {
    
    @Autowired
    private UserCenterClient userCenterClient;
    @Override
    public List<CustomerService> getAgentFromUserCenter(List<String> serviceIds) {
        List<UserCenterCustomerServiceDTO> dtoList = userCenterClient.getCustomerServiceByIds(serviceIds);
        return dtoList.stream()
                .map(CustomerServiceConvertor.INSTANCE::toCustomerService)
                .collect(Collectors.toList());
    }
    
    @Override
    public Map<String, CustomerServiceStatistics> getAgentStatistics(List<String> serviceIds) {
        return Collections.emptyMap();
    }
    
    @Override
    public List<String> getAllAgentIds() {
        return userCenterClient.getAllCustomerServiceIds();
    }
} 