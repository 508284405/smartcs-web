package com.leyue.smartcs.chat.executor;

import com.leyue.smartcs.context.UserContext;
import com.leyue.smartcs.domain.chat.Message;
import com.leyue.smartcs.domain.chat.MessageType;
import com.leyue.smartcs.domain.chat.SenderRole;
import com.leyue.smartcs.domain.chat.domainservice.MessageDomainService;
import com.leyue.smartcs.dto.chat.MessageDTO;
import com.leyue.smartcs.dto.chat.SendMessageCmd;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 发送消息命令执行器
 */
@Component
@RequiredArgsConstructor
public class SendMessageCmdExe {

    private final MessageDomainService messageDomainService;

    /**
     * 执行发送消息命令
     *
     * @param cmd 发送消息命令
     * @return 消息DTO
     */
    public MessageDTO execute(SendMessageCmd cmd) {
        // 发送消息
        Message message = messageDomainService.sendMessage(
                cmd.getSessionId(),
                UserContext.getCurrentUser().getId(),
                SenderRole.fromCode(cmd.getSenderRole() == null ? 0 : cmd.getSenderRole()),
                MessageType.fromCode(cmd.getMsgType() == null ? 0 : cmd.getMsgType()),
                cmd.getContent(),
                cmd.getAtList()
        );

        // 转换为DTO
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
