package com.leyue.smartcs.dto.sse;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 订单DTO
 */
@Data
public class OrderDTO {
    /**
     * 订单ID
     */
    private Long id;

    /**
     * 订单编号
     */
    private String orderNumber;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 总金额
     */
    private BigDecimal totalAmount;

    /**
     * 订单状态
     */
    private String orderStatus;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 支付时间
     */
    private Long payTime;

    /**
     * 发货时间
     */
    private Long shipTime;

    /**
     * 完成时间
     */
    private Long completeTime;

    /**
     * 过期时间
     */
    private Long expireTime;

    /**
     * 支付方式
     */
    private String paymentMethod;

    /**
     * 跟踪号码
     */
    private String trackingNumber;

    /**
     * 承运人
     */
    private String carrier;

    /**
     * 配送地址
     */
    private AddressDTO address;

    /**
     * 订单项列表
     */
    private List<OrderItemDTO> items;

    /**
     * 退款原因
     */
    private String refundReason;

    /**
     * 退款状态
     */
    private String refundStatus;

    /**
     * 支付表单数据，包含了前端唤起支付所需的HTML或其他数据
     */
    private String paymentForm;

    /**
     * 支付交易ID，支付成功后由支付网关返回
     */
    private String transactionId;
}