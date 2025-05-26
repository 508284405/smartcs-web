package com.leyue.smartcs.customer.convertor;

import com.leyue.smartcs.common.dao.UserCenterCustomerServiceDTO;
import com.leyue.smartcs.domain.customer.CustomerService;
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
}