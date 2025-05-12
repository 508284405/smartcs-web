package com.leyue.smartcs.chat.serviceimpl;

import com.leyue.smartcs.chat.executor.SendMessageCmdExe;
import com.leyue.smartcs.dto.chat.MessageDTO;
import com.leyue.smartcs.dto.chat.SendMessageCmd;
import com.leyue.smartcs.dto.chat.GetMessagesQry;
import com.leyue.smartcs.chat.service.MessageService;
import com.leyue.smartcs.domain.chat.Message;
import com.leyue.smartcs.domain.chat.domainservice.MessageDomainService;
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
    
    private final SendMessageCmdExe sendMessageCmdExe;
    private final MessageDomainService messageDomainService;
    
    @Override
    public MessageDTO sendMessage(SendMessageCmd sendMessageCmd) {
        return sendMessageCmdExe.execute(sendMessageCmd);
    }
    
    @Override
    public List<MessageDTO> getSessionMessages(GetMessagesQry query) {
        List<Message> messages = messageDomainService.getSessionMessages(
                query.getSessionId(), 
                query.getBeforeMessageId(), 
                query.getLimit());
        return messages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<MessageDTO> getSessionMessagesWithPagination(Long sessionId, int offset, int limit) {
        List<Message> messages = messageDomainService.getSessionMessagesWithPagination(sessionId, offset, limit);
        return messages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 将领域模型转换为DTO
     *
     * @param message 消息领域模型
     * @return 消息DTO
     */
    private MessageDTO convertToDTO(Message message) {
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setMsgId(message.getMsgId());
        messageDTO.setSessionId(message.getSessionId());
        messageDTO.setSenderId(message.getSenderId());
        messageDTO.setSenderRole(message.getSenderRole().getCode());
        messageDTO.setMsgType(message.getMsgType().getCode());
        messageDTO.setContent(message.getContent());
        messageDTO.setAtList(message.getAtList());
        messageDTO.setCreatedAt(message.getCreatedAt());
        return messageDTO;
    }
}
