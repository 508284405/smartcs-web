package com.leyue.smartcs.chat;

import com.leyue.smartcs.chat.convertor.MessageConvertor;
import com.leyue.smartcs.chat.dataobject.CsMessageDO;
import com.leyue.smartcs.chat.mapper.CsMessageMapper;
import com.leyue.smartcs.domain.chat.Message;
import com.leyue.smartcs.domain.chat.gateway.MessageGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 消息网关接口实现
 */
@Component
@RequiredArgsConstructor
public class MessageGatewayImpl implements MessageGateway {
    
    private final CsMessageMapper messageMapper;
    private final MessageConvertor messageConverter;
    
    @Override
    public String sendMessage(Message message) {
        CsMessageDO csMessageDO = messageConverter.toDataObject(message);
        messageMapper.insert(csMessageDO);
        return csMessageDO.getMsgId();
    }
    
    @Override
    public Optional<Message> findById(String msgId) {
        CsMessageDO csMessageDO = messageMapper.selectByMessageId(msgId);
        return Optional.ofNullable(messageConverter.toDomain(csMessageDO));
    }
    
    @Override
    public List<Message> findMessagesBySessionId(Long sessionId, int limit) {
        List<CsMessageDO> csMessageDOList = messageMapper.findMessagesBySessionId(sessionId, limit);
        return csMessageDOList.stream()
                .map(messageConverter::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Message> findMessagesBySessionIdBeforeMessageId(Long sessionId, String beforeMessageId, int limit) {
        List<CsMessageDO> csMessageDOList = messageMapper.findMessagesBySessionIdBeforeMessageId(sessionId, beforeMessageId, limit);
        return csMessageDOList.stream()
                .map(messageConverter::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Message> findMessagesBySessionIdWithPagination(Long sessionId, int offset, int limit) {
        // 将Long类型的sessionId转换为String类型
        String sessionIdStr = sessionId != null ? sessionId.toString() : null;
        
        // 使用现有的selectBySessionId方法
        List<CsMessageDO> csMessageDOList = messageMapper.selectBySessionId(sessionIdStr, offset, limit);
        return csMessageDOList.stream()
                .map(messageConverter::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean batchSaveMessages(List<Message> messages) {
        List<CsMessageDO> csMessageDOList = messages.stream()
                .map(messageConverter::toDataObject)
                .collect(Collectors.toList());
        
        if (csMessageDOList.isEmpty()) {
            return true;
        }
        
        return messageMapper.batchInsert(csMessageDOList) > 0;
    }
}
