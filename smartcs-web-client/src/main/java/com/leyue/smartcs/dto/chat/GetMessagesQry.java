package com.leyue.smartcs.dto.chat;

import lombok.Data;

/**
 * 获取消息列表的查询参数
 */
@Data
public class GetMessagesQry {

    /**
     * 会话ID
     */
    private final Long sessionId;

    /**
     * 消息ID，获取该消息之前的历史，为空则获取最新消息
     */
    private final String beforeMessageId;

    /**
     * 限制数量
     */
    private final Integer limit = 20;
} 