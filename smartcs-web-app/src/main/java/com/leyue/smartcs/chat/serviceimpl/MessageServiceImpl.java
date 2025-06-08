package com.leyue.smartcs.chat.serviceimpl;

import com.leyue.smartcs.api.MessageService;
import com.leyue.smartcs.chat.convertor.ChatMessageConvertor;
import com.leyue.smartcs.domain.chat.Message;
import com.leyue.smartcs.domain.chat.domainservice.MessageDomainService;
import com.leyue.smartcs.dto.chat.GetMessagesQry;
import com.leyue.smartcs.dto.chat.MessageDTO;
import com.leyue.smartcs.dto.chat.SendMessageCmd;
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
}
