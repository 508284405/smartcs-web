package com.leyue.smartcs.dto.sse;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 订单项DTO
 */
@Data
public class OrderItemDTO {
    /**
     * 订单项ID
     */
    private Long id;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品图片
     */
    private String productImage;

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * SKU 属性
     */
    private Map<String, String> skuProperties;

    /**
     * SKU 图片
     */
    private String skuImage;

    /**
     * 数量
     */
    private int quantity;

    /**
     * 单价
     */
    private BigDecimal unitPrice;

    /**
     * 总价
     */
    private BigDecimal totalPrice;

    /**
     * SKU 描述
     */
    private String skuDescription;

    /**
     * 售后单号
     */
    private String afterSaleNo;

    /**
     * 售后单状态
     */
    private String afterSaleStatus;
}