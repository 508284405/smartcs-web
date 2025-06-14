package com.leyue.smartcs.dto.sse;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 地址DTO
 */
@Data
@Accessors(chain = true)
public class AddressDTO {
    /** 地址ID*/
    private Long id;

    /**
     * 收件人
     */
    private String receiverName;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 省份
     */
    private String province;
    
    /**
     * 城市
     */
    private String city;
    
    /**
     * 区县
     */
    private String district;
    
    /**
     * 详细地址
     */
    private String detailedAddr;

    /**
     * 邮编
     */
    private String postalCode;
    
    /**
     * 获取详细地址
     * @return 详细地址
     */
    public String getDetail() {
        return detailedAddr;
    }
    
    /**
     * 设置详细地址
     * @param detail 详细地址
     */
    public AddressDTO setDetail(String detail) {
        this.detailedAddr = detail;
        return this;
    }
}