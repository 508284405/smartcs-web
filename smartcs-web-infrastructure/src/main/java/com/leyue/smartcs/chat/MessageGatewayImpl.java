package com.leyue.smartcs.chat;

import com.alibaba.cola.dto.PageResponse;
import com.leyue.smartcs.chat.convertor.MessageConvertor;
import com.leyue.smartcs.chat.dataobject.CsMessageDO;
import com.leyue.smartcs.chat.mapper.CsMessageMapper;
import com.leyue.smartcs.domain.chat.Message;
import com.leyue.smartcs.domain.chat.gateway.MessageGateway;
import com.leyue.smartcs.dto.chat.MessageSearchQry;
import com.leyue.smartcs.dto.chat.MessageSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
    public PageResponse<MessageSearchResult> searchMessages(MessageSearchQry qry) {
        try {
            // 构建查询条件
            String keyword = qry.getKeyword();
            Long sessionId = qry.getSessionId();
            Integer messageType = qry.getMessageType();
            Long startTime = qry.getStartTime();
            Long endTime = qry.getEndTime();
            String sortBy = qry.getSortBy();
            int pageIndex = qry.getPageIndex();
            int pageSize = qry.getPageSize();
            int offset = (pageIndex - 1) * pageSize;
            
            // 执行搜索查询
            List<CsMessageDO> messageList = messageMapper.searchMessages(
                keyword, qry.getUserId(), sessionId, messageType, 
                startTime, endTime, sortBy, offset, pageSize);
            
            // 获取总数
            long totalCount = messageMapper.countSearchMessages(
                keyword, qry.getUserId(), sessionId, messageType, startTime, endTime);
            
            // 转换为搜索结果
            List<MessageSearchResult> searchResults = new ArrayList<>();
            for (CsMessageDO messageDO : messageList) {
                MessageSearchResult result = convertToSearchResult(messageDO, qry);
                searchResults.add(result);
            }
            
            // 构建分页响应
            return PageResponse.of(searchResults, (int) totalCount, pageIndex, pageSize);
            
        } catch (Exception e) {
            throw new RuntimeException("搜索消息失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 转换消息DO为搜索结果
     */
    private MessageSearchResult convertToSearchResult(CsMessageDO messageDO, MessageSearchQry qry) {
        MessageSearchResult result = new MessageSearchResult();
        result.setMsgId(messageDO.getMsgId());
        result.setSessionId(messageDO.getSessionId());
        result.setContent(messageDO.getContent());
        result.setMessageType(messageDO.getMsgType());
        result.setCreatedAt(messageDO.getCreatedAt());
        
        // 从BaseDO获取创建者信息作为发送者
        result.setSenderId(messageDO.getCreatedBy());
        result.setSenderName(""); // 需要通过用户服务获取用户名，这里暂时留空
        
        // 判断是否为自己的消息
        result.setIsMyMessage(qry.getUserId().equals(messageDO.getCreatedBy()));
        
        // 使用发送状态作为消息状态
        result.setStatus(messageDO.getSendStatus());
        
        // 暂时不设置额外数据
        result.setExtraData(null);
        
        // 设置消息类型文本
        result.setMessageTypeText(getMessageTypeText(messageDO.getMsgType()));
        
        return result;
    }
    
    /**
     * 获取消息类型文本描述
     */
    private String getMessageTypeText(Integer messageType) {
        if (messageType == null) {
            return "消息";
        }
        
        switch (messageType) {
            case 1: return "文本";
            case 2: return "图片";
            case 3: return "语音";
            case 4: return "视频";
            case 5: return "文件";
            case 6: return "位置";
            case 7: return "链接";
            case 8: return "系统";
            default: return "消息";
        }
    }
}
