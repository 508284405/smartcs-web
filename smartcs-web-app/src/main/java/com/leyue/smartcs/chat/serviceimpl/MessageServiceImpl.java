package com.leyue.smartcs.chat.serviceimpl;

import com.leyue.smartcs.api.MessageService;
import com.leyue.smartcs.chat.convertor.ChatMessageConvertor;
import com.leyue.smartcs.domain.chat.Message;
import com.leyue.smartcs.domain.chat.domainservice.MessageDomainService;
import com.leyue.smartcs.dto.chat.GetMessagesQry;
import com.leyue.smartcs.dto.chat.MessageDTO;
import com.leyue.smartcs.dto.chat.SendMessageCmd;
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
import com.leyue.smartcs.chat.executor.DeleteMessageCmdExe;
import com.leyue.smartcs.chat.executor.BatchDeleteMessagesCmdExe;
import com.leyue.smartcs.chat.executor.EditMessageCmdExe;
import com.leyue.smartcs.chat.executor.MarkMessageReadCmdExe;
import com.leyue.smartcs.chat.executor.BatchMarkMessagesReadCmdExe;
import com.leyue.smartcs.chat.executor.RetryMessageSendCmdExe;
import com.leyue.smartcs.chat.executor.UpdateMessageStatusCmdExe;
import com.leyue.smartcs.chat.executor.AddReactionCmdExe;
import com.leyue.smartcs.chat.executor.GetMessageReactionsQryExe;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息服务实现
 */
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageDomainService messageDomainService;
    private final ChatMessageConvertor messageConvertor;
    private final DeleteMessageCmdExe deleteMessageCmdExe;
    private final BatchDeleteMessagesCmdExe batchDeleteMessagesCmdExe;
    private final EditMessageCmdExe editMessageCmdExe;
    private final MarkMessageReadCmdExe markMessageReadCmdExe;
    private final BatchMarkMessagesReadCmdExe batchMarkMessagesReadCmdExe;
    private final RetryMessageSendCmdExe retryMessageSendCmdExe;
    private final UpdateMessageStatusCmdExe updateMessageStatusCmdExe;
    private final AddReactionCmdExe addReactionCmdExe;
    private final GetMessageReactionsQryExe getMessageReactionsQryExe;

    @Override
    public MessageDTO sendMessage(SendMessageCmd sendMessageCmd) {
//        return sendMessageCmdExe.execute(sendMessageCmd);
        return null;
    }

    @Override
    public List<MessageDTO> getSessionMessages(GetMessagesQry query) {
        List<Message> messages = messageDomainService.getSessionMessages(
                query.getSessionId(),
                query.getBeforeMessageId(),
                query.getLimit());
        return messages.stream()
                .map(messageConvertor::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageDTO> getSessionMessagesWithPagination(Long sessionId, int offset, int limit) {
        List<Message> messages = messageDomainService.getSessionMessagesWithPagination(sessionId, offset, limit);
        return messages.stream()
                .map(messageConvertor::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public MessageDTO recallMessage(RecallMessageCmd recallMessageCmd) {
        Message recalledMessage = messageDomainService.recallMessage(
                recallMessageCmd.getMsgId(),
                recallMessageCmd.getUserId(),
                recallMessageCmd.getReason());
        return messageConvertor.toDTO(recalledMessage);
    }

    @Override
    public Response deleteMessage(DeleteMessageCmd deleteMessageCmd) {
        return deleteMessageCmdExe.execute(deleteMessageCmd);
    }

    @Override
    public Response batchDeleteMessages(BatchDeleteMessagesCmd batchDeleteMessagesCmd) {
        return batchDeleteMessagesCmdExe.execute(batchDeleteMessagesCmd);
    }

    @Override
    public Response editMessage(EditMessageCmd editMessageCmd) {
        return editMessageCmdExe.execute(editMessageCmd);
    }

    @Override
    public Response markMessageAsRead(MarkMessageReadCmd markMessageReadCmd) {
        return markMessageReadCmdExe.execute(markMessageReadCmd);
    }

    @Override
    public Response batchMarkMessagesAsRead(BatchMarkMessagesReadCmd batchMarkMessagesReadCmd) {
        return batchMarkMessagesReadCmdExe.execute(batchMarkMessagesReadCmd);
    }

    @Override
    public Response retryMessageSend(RetryMessageSendCmd retryMessageSendCmd) {
        return retryMessageSendCmdExe.execute(retryMessageSendCmd);
    }

    @Override
    public Response updateMessageStatus(UpdateMessageStatusCmd updateMessageStatusCmd) {
        return updateMessageStatusCmdExe.execute(updateMessageStatusCmd);
    }

    @Override
    public Response addReaction(AddReactionCmd addReactionCmd) {
        return addReactionCmdExe.execute(addReactionCmd);
    }

    @Override
    public SingleResponse<List<ReactionDTO>> getMessageReactions(String msgId) {
        return getMessageReactionsQryExe.execute(msgId);
    }
}
