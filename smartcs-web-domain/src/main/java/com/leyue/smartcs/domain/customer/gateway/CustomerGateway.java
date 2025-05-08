package com.leyue.smartcs.domain.customer.gateway;

import com.leyue.smartcs.domain.customer.Customer;

public interface CustomerGateway {
    Customer getByById(String customerId);
}
