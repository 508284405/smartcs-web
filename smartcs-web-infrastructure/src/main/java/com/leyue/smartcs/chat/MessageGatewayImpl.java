package com.leyue.smartcs.chat;

import com.leyue.smartcs.chat.convertor.MessageConvertor;
import com.leyue.smartcs.chat.dataobject.CsMessageDO;
import com.leyue.smartcs.chat.mapper.CsMessageMapper;
import com.leyue.smartcs.domain.chat.Message;
import com.leyue.smartcs.domain.chat.MessageSearchQuery;
import com.leyue.smartcs.domain.chat.MessageSearchResult;
import com.leyue.smartcs.domain.chat.PageResult;
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
    
    @Override
    public boolean updateMessageRecallStatus(String msgId, String recalledBy, String recallReason) {
        return messageMapper.updateRecallStatus(msgId, recalledBy, recallReason, System.currentTimeMillis()) > 0;
    }

    @Override
    public Message findByMsgId(String msgId) {
        CsMessageDO csMessageDO = messageMapper.selectByMessageId(msgId);
        return messageConverter.toDomain(csMessageDO);
    }

    @Override
    public boolean updateMessage(Message message) {
        CsMessageDO csMessageDO = messageConverter.toDataObject(message);
        return messageMapper.updateByMessageId(csMessageDO) > 0;
    }

    @Override
    public PageResult<MessageSearchResult> searchMessages(MessageSearchQuery query) {
        // 执行搜索查询
        List<CsMessageDO> messageDOList = messageMapper.searchMessages(
                query.getKeyword(),
                query.getUserId(),
                query.getSessionId(),
                query.getMessageType(),
                query.getStartTime(),
                query.getEndTime(),
                query.getSortBy(),
                query.getOffset(),
                query.getLimit()
        );
        
        // 统计总数量
        long totalCount = messageMapper.countSearchMessages(
                query.getKeyword(),
                query.getUserId(),
                query.getSessionId(),
                query.getMessageType(),
                query.getStartTime(),
                query.getEndTime()
        );
        
        // 转换为领域对象
        List<MessageSearchResult> results = messageDOList.stream()
                .map(messageDO -> {
                    Message message = messageConverter.toDomain(messageDO);
                    return MessageSearchResult.builder()
                            .message(message)
                            .score(1.0) // 默认得分，可以根据实际需求计算相关性得分
                            .contextMsgId(message.getMsgId())
                            .build();
                })
                .collect(Collectors.toList());
        
        // 计算分页信息
        int pageIndex = (query.getOffset() / query.getLimit()) + 1;
        
        return PageResult.<MessageSearchResult>builder()
                .data(results)
                .totalCount(totalCount)
                .pageIndex(pageIndex)
                .pageSize(query.getLimit())
                .build();
    }
}
