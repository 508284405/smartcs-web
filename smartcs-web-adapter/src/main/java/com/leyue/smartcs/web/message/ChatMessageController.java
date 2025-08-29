package com.leyue.smartcs.web.message;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.leyue.smartcs.api.MessageService;
import com.leyue.smartcs.chat.convertor.ChatMessageConvertor;
import com.leyue.smartcs.dto.chat.GetMessagesQry;
import com.leyue.smartcs.dto.chat.MessageDTO;
import com.leyue.smartcs.dto.chat.MessageVO;
import com.leyue.smartcs.dto.chat.RecallMessageCmd;
import com.leyue.smartcs.dto.chat.DeleteMessageCmd;
import com.leyue.smartcs.dto.chat.BatchDeleteMessagesCmd;
import com.leyue.smartcs.dto.chat.EditMessageCmd;
import com.leyue.smartcs.dto.chat.MarkMessageReadCmd;
import com.leyue.smartcs.dto.chat.BatchMarkMessagesReadCmd;
import com.leyue.smartcs.dto.chat.RetryMessageSendCmd;
import com.leyue.smartcs.dto.chat.UpdateMessageStatusCmd;

import lombok.RequiredArgsConstructor;

/**
 * 消息管理控制器
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/chat/messages")
public class ChatMessageController {
    
    private final MessageService messageService;
    private final ChatMessageConvertor messageConvertor;

    /**
     * 获取会话消息历史
     *
     * @return 消息视图对象列表
     */
    @GetMapping("/session/{sessionId}")
    public MultiResponse<MessageVO> getSessionMessages(GetMessagesQry qry) {
        List<MessageDTO> messageDTOList = messageService.getSessionMessages(qry);
        return MultiResponse.of(messageConvertor.toVOList(messageDTOList));
    }

    /**
     * 分页获取会话消息历史
     *
     * @param sessionId 会话ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 消息视图对象列表
     */
    @GetMapping("/session/{sessionId}/page")
    public PageResponse<MessageDTO> getSessionMessagesWithPagination(
            @PathVariable Long sessionId, 
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit) {
        List<MessageDTO> messageDTOList = messageService.getSessionMessagesWithPagination(sessionId, offset, limit);
        return PageResponse.of(messageDTOList,0,0,0);
    }
    
    /**
     * 撤回消息
     *
     * @param recallMessageCmd 撤回消息命令
     * @return 撤回结果
     */
    @PostMapping("/recall")
    public Response recallMessage(@RequestBody RecallMessageCmd recallMessageCmd) {
        try {
            MessageDTO recalledMessage = messageService.recallMessage(recallMessageCmd);
            return Response.buildSuccess();
        } catch (Exception e) {
            return Response.buildFailure("RECALL_FAILED", e.getMessage());
        }
    }

    /**
     * 删除消息
     *
     * @param deleteMessageCmd 删除消息命令
     * @return 删除结果
     */
    @PostMapping("/delete")
    public Response deleteMessage(@RequestBody DeleteMessageCmd deleteMessageCmd) {
        return messageService.deleteMessage(deleteMessageCmd);
    }

    /**
     * 批量删除消息
     *
     * @param batchDeleteMessagesCmd 批量删除消息命令
     * @return 删除结果
     */
    @PostMapping("/batch-delete")
    public Response batchDeleteMessages(@RequestBody BatchDeleteMessagesCmd batchDeleteMessagesCmd) {
        return messageService.batchDeleteMessages(batchDeleteMessagesCmd);
    }

    /**
     * 编辑消息
     *
     * @param editMessageCmd 编辑消息命令
     * @return 编辑结果
     */
    @PostMapping("/edit")
    public Response editMessage(@RequestBody EditMessageCmd editMessageCmd) {
        return messageService.editMessage(editMessageCmd);
    }

    /**
     * 标记消息已读
     *
     * @param markMessageReadCmd 标记消息已读命令
     * @return 操作结果
     */
    @PostMapping("/mark-read")
    public Response markMessageAsRead(@RequestBody MarkMessageReadCmd markMessageReadCmd) {
        return messageService.markMessageAsRead(markMessageReadCmd);
    }

    /**
     * 批量标记消息已读
     *
     * @param batchMarkMessagesReadCmd 批量标记消息已读命令
     * @return 操作结果
     */
    @PostMapping("/batch-mark-read")
    public Response batchMarkMessagesAsRead(@RequestBody BatchMarkMessagesReadCmd batchMarkMessagesReadCmd) {
        return messageService.batchMarkMessagesAsRead(batchMarkMessagesReadCmd);
    }

    /**
     * 重试发送消息
     *
     * @param retryMessageSendCmd 重试发送消息命令
     * @return 操作结果
     */
    @PostMapping("/retry-send")
    public Response retryMessageSend(@RequestBody RetryMessageSendCmd retryMessageSendCmd) {
        return messageService.retryMessageSend(retryMessageSendCmd);
    }

    /**
     * 更新消息状态
     *
     * @param updateMessageStatusCmd 更新消息状态命令
     * @return 操作结果
     */
    @PostMapping("/update-status")
    public Response updateMessageStatus(@RequestBody UpdateMessageStatusCmd updateMessageStatusCmd) {
        return messageService.updateMessageStatus(updateMessageStatusCmd);
    }

    private Response createSuccessAck(String msgId, String sessionId) {
        return Response.buildSuccess();
    }

    private Response createErrorAck(String msgId, String sessionId, String errorCode, String errorMessage) {
        return Response.buildFailure(errorCode, errorMessage);
    }
}
