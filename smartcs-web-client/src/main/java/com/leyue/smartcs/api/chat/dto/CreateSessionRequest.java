package com.leyue.smartcs.api.chat.dto;

import lombok.Data;

/**
 * 创建会话请求对象
 */
@Data
public class CreateSessionRequest {
    /**
     * 客户ID
     */
    private Long customerId;
}
