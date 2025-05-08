package com.leyue.smartcs.chat.service;

import com.leyue.smartcs.dto.chat.MessageDTO;
import com.leyue.smartcs.dto.chat.SendMessageCmd;

import java.util.List;

/**
 * 消息服务接口
 */
public interface MessageService {
    
    /**
     * 发送消息
     *
     * @param sendMessageCmd 发送消息命令
     * @return 消息DTO
     */
    MessageDTO sendMessage(SendMessageCmd sendMessageCmd);
    
    /**
     * 获取会话消息历史
     *
     * @param sessionId 会话ID
     * @param limit 限制数量
     * @return 消息DTO列表
     */
    List<MessageDTO> getSessionMessages(Long sessionId, int limit);
    
    /**
     * 分页获取会话消息历史
     *
     * @param sessionId 会话ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 消息DTO列表
     */
    List<MessageDTO> getSessionMessagesWithPagination(Long sessionId, int offset, int limit);
}
