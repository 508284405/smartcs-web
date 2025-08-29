package com.leyue.smartcs.api;

import com.leyue.smartcs.dto.chat.MessageDTO;
import com.leyue.smartcs.dto.chat.SendMessageCmd;
import com.leyue.smartcs.dto.chat.GetMessagesQry;
import com.leyue.smartcs.dto.chat.RecallMessageCmd;
import com.leyue.smartcs.dto.chat.DeleteMessageCmd;
import com.leyue.smartcs.dto.chat.BatchDeleteMessagesCmd;
import com.leyue.smartcs.dto.chat.EditMessageCmd;
import com.leyue.smartcs.dto.chat.MarkMessageReadCmd;
import com.leyue.smartcs.dto.chat.BatchMarkMessagesReadCmd;
import com.leyue.smartcs.dto.chat.RetryMessageSendCmd;
import com.leyue.smartcs.dto.chat.UpdateMessageStatusCmd;
import com.leyue.smartcs.dto.chat.AddReactionCmd;
import com.leyue.smartcs.dto.chat.ReactionDTO;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;

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
     * 获取会话消息历史（使用查询对象）
     *
     * @param query 消息查询对象
     * @return 消息DTO列表
     */
    List<MessageDTO> getSessionMessages(GetMessagesQry query);
    /**
     * 分页获取会话消息历史
     *
     * @param sessionId 会话ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 消息DTO列表
     */
    List<MessageDTO> getSessionMessagesWithPagination(Long sessionId, int offset, int limit);
    
    /**
     * 撤回消息
     *
     * @param recallMessageCmd 撤回消息命令
     * @return 撤回成功后的消息信息
     */
    MessageDTO recallMessage(RecallMessageCmd recallMessageCmd);

    /**
     * 删除消息
     *
     * @param deleteMessageCmd 删除消息命令
     * @return 操作结果
     */
    Response deleteMessage(DeleteMessageCmd deleteMessageCmd);

    /**
     * 批量删除消息
     *
     * @param batchDeleteMessagesCmd 批量删除消息命令
     * @return 操作结果
     */
    Response batchDeleteMessages(BatchDeleteMessagesCmd batchDeleteMessagesCmd);

    /**
     * 编辑消息
     *
     * @param editMessageCmd 编辑消息命令
     * @return 操作结果
     */
    Response editMessage(EditMessageCmd editMessageCmd);

    /**
     * 标记消息已读
     *
     * @param markMessageReadCmd 标记消息已读命令
     * @return 操作结果
     */
    Response markMessageAsRead(MarkMessageReadCmd markMessageReadCmd);

    /**
     * 批量标记消息已读
     *
     * @param batchMarkMessagesReadCmd 批量标记消息已读命令
     * @return 操作结果
     */
    Response batchMarkMessagesAsRead(BatchMarkMessagesReadCmd batchMarkMessagesReadCmd);

    /**
     * 重试发送消息
     *
     * @param retryMessageSendCmd 重试发送消息命令
     * @return 操作结果
     */
    Response retryMessageSend(RetryMessageSendCmd retryMessageSendCmd);

    /**
     * 更新消息状态
     *
     * @param updateMessageStatusCmd 更新消息状态命令
     * @return 操作结果
     */
    Response updateMessageStatus(UpdateMessageStatusCmd updateMessageStatusCmd);

    /**
     * 添加或切换表情反应
     *
     * @param addReactionCmd 添加表情反应命令
     * @return 操作结果
     */
    Response addReaction(AddReactionCmd addReactionCmd);

    /**
     * 获取消息的表情反应统计
     *
     * @param msgId 消息ID
     * @return 表情反应统计列表
     */
    SingleResponse<List<ReactionDTO>> getMessageReactions(String msgId);
}
