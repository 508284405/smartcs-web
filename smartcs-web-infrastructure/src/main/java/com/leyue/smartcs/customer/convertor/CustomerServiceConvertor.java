package com.leyue.smartcs.customer.convertor;

import com.leyue.smartcs.customer.feign.UserCenterCustomerServiceDTO;
import com.leyue.smartcs.domain.customer.CustomerService;
import com.leyue.smartcs.domain.customer.gateway.AgentGateway.CustomerServiceStatistics;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 客服数据转换器
 */
@Mapper
public interface CustomerServiceConvertor {
    
    CustomerServiceConvertor INSTANCE = Mappers.getMapper(CustomerServiceConvertor.class);
    
    /**
     * UserCenterCustomerServiceDTO转换为CustomerService
     */
    CustomerService toCustomerService(UserCenterCustomerServiceDTO dto);
    
    /**
     * CustomerServiceStatisticsDO转换为CustomerServiceStatistics
     */
    CustomerServiceStatistics toCustomerServiceStatistics(CustomerServiceStatisticsDO statisticsDO);
} 