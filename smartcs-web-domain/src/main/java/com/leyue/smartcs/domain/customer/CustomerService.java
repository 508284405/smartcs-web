package com.leyue.smartcs.domain.customer;

import lombok.Data;

/**
 * 客服领域模型
 */
@Data
public class CustomerService {
    
    /**
     * 客服ID
     */
    private String serviceId;
    
    /**
     * 客服姓名
     */
    private String serviceName;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 电话
     */
    private String phone;
    
    /**
     * 部门
     */
    private String department;
} 