package com.leyue.smartcs.dto.chat;

import lombok.Data;

/**
 * 创建会话命令
 */
@Data
public class CreateSessionCmd {
    /**
     * 客户ID
     */
    private Long customerId;
}
