package com.leyue.smartcs.api;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.Response;
import com.leyue.smartcs.dto.CustomerAddCmd;
import com.leyue.smartcs.dto.CustomerListByNameQry;
import com.leyue.smartcs.dto.data.CustomerDTO;

public interface CustomerServiceI {

    Response addCustomer(CustomerAddCmd customerAddCmd);

    MultiResponse<CustomerDTO> listByName(CustomerListByNameQry customerListByNameQry);
}
