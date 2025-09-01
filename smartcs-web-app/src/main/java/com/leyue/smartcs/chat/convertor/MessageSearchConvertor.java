package com.leyue.smartcs.chat.convertor;

import com.alibaba.cola.dto.PageResponse;
import com.leyue.smartcs.domain.chat.MessageSearchQuery;
import com.leyue.smartcs.domain.chat.PageResult;
import com.leyue.smartcs.dto.chat.MessageSearchQry;
import com.leyue.smartcs.dto.chat.MessageSearchResult;
import com.leyue.smartcs.domain.chat.enums.MessageType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息搜索转换器
 */
@Component
public class MessageSearchConvertor {
    
    /**
     * 客户端查询转换为领域查询
     */
    public MessageSearchQuery toSearchQuery(MessageSearchQry qry) {
        return MessageSearchQuery.builder()
                .userId(qry.getUserId())
                .keyword(qry.getKeyword())
                .sessionId(qry.getSessionId())
                .messageType(qry.getMessageType())
                .startTime(qry.getStartTime())
                .endTime(qry.getEndTime())
                .sortBy(qry.getSortBy())
                .onlyMyMessages(qry.getOnlyMyMessages())
                .includeSystemMessages(qry.getIncludeSystemMessages())
                .offset((qry.getPageIndex() - 1) * qry.getPageSize())
                .limit(qry.getPageSize())
                .build();
    }
    
    /**
     * 领域搜索结果转换为客户端响应
     */
    public PageResponse<MessageSearchResult> toPageResponse(PageResult<com.leyue.smartcs.domain.chat.MessageSearchResult> domainResult) {
        List<MessageSearchResult> clientResults = convertToClientResults(domainResult.getData());
        PageResponse<MessageSearchResult> response = PageResponse.<MessageSearchResult>of(clientResults, (int) domainResult.getTotalCount());
        response.setPageIndex(domainResult.getPageIndex());
        response.setPageSize(domainResult.getPageSize());
        return response;
    }
    
    /**
     * 转换领域搜索结果为客户端搜索结果列表
     */
    private List<MessageSearchResult> convertToClientResults(List<com.leyue.smartcs.domain.chat.MessageSearchResult> domainResults) {
        return domainResults.stream()
                .map(this::convertToClientResult)
                .collect(Collectors.toList());
    }
    
    /**
     * 转换单个领域搜索结果为客户端搜索结果
     */
    private MessageSearchResult convertToClientResult(com.leyue.smartcs.domain.chat.MessageSearchResult domainResult) {
        MessageSearchResult clientResult = new MessageSearchResult();
        
        // 从领域Message对象复制属性
        com.leyue.smartcs.domain.chat.Message message = domainResult.getMessage();
        if (message != null) {
            clientResult.setMsgId(message.getMsgId());
            clientResult.setSessionId(message.getSessionId());
            clientResult.setContent(message.getContent());
            clientResult.setMessageType(message.getMsgType() != null ? message.getMsgType().getCode() : null);
            clientResult.setMessageTypeText(message.getMsgType() != null ? message.getMsgType().name() : null);
            clientResult.setCreatedAt(message.getCreatedAt());
            clientResult.setStatus(message.getSendStatus() != null ? message.getSendStatus().getCode() : null);
            
            // 检查是否为自己发送的消息需要通过userId对比
            // 这里简化处理，可以在调用层设置
        }
        
        // 设置搜索相关属性
        clientResult.setScore(domainResult.getScore());
        clientResult.setContextMsgId(domainResult.getContextMsgId());
        
        return clientResult;
    }
}