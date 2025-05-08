package com.leyue.smartcs.customer;

import com.alibaba.cola.catchlog.CatchAndLog;
import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.Response;
import com.leyue.smartcs.api.CustomerServiceI;
import com.leyue.smartcs.customer.executor.CustomerAddCmdExe;
import com.leyue.smartcs.customer.executor.query.CustomerListByNameQryExe;
import com.leyue.smartcs.dto.CustomerAddCmd;
import com.leyue.smartcs.dto.CustomerListByNameQry;
import com.leyue.smartcs.dto.data.CustomerDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;



@Service
@CatchAndLog
public class CustomerServiceImpl implements CustomerServiceI {

    @Resource
    private CustomerAddCmdExe customerAddCmdExe;

    @Resource
    private CustomerListByNameQryExe customerListByNameQryExe;

    public Response addCustomer(CustomerAddCmd customerAddCmd) {
        return customerAddCmdExe.execute(customerAddCmd);
    }

    @Override
    public MultiResponse<CustomerDTO> listByName(CustomerListByNameQry customerListByNameQry) {
        return customerListByNameQryExe.execute(customerListByNameQry);
    }

}