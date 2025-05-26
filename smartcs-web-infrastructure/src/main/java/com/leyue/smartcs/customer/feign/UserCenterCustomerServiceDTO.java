package com.leyue.smartcs.customer.feign;

import lombok.Data;

/**
 * 用户中心客服DTO
 */
@Data
public class UserCenterCustomerServiceDTO {
    
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