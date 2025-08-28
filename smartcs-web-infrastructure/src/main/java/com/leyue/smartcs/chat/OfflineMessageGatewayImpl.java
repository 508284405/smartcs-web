package com.leyue.smartcs.chat;

import com.leyue.smartcs.chat.convertor.OfflineMessageConvertor;
import com.leyue.smartcs.chat.dataobject.OfflineMessageDO;
import com.leyue.smartcs.chat.mapper.OfflineMessageMapper;
import com.leyue.smartcs.domain.chat.OfflineMessage;
import com.leyue.smartcs.domain.chat.gateway.OfflineMessageGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 离线消息网关接口实现
 * 
 * @author Claude
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OfflineMessageGatewayImpl implements OfflineMessageGateway {
    
    private final OfflineMessageMapper offlineMessageMapper;
    private final OfflineMessageConvertor offlineMessageConvertor;
    
    @Override
    public Long save(OfflineMessage offlineMessage) {
        log.debug("保存离线消息: {}", offlineMessage);
        OfflineMessageDO offlineMessageDO = offlineMessageConvertor.toDataObject(offlineMessage);
        offlineMessageMapper.insert(offlineMessageDO);
        return offlineMessageDO.getId();
    }
    
    @Override
    public void batchSave(List<OfflineMessage> offlineMessages) {
        if (offlineMessages == null || offlineMessages.isEmpty()) {
            return;
        }
        
        log.debug("批量保存离线消息: {} 条", offlineMessages.size());
        List<OfflineMessageDO> offlineMessageDOs = offlineMessageConvertor.toDataObjects(offlineMessages);
        offlineMessageMapper.batchInsert(offlineMessageDOs);
    }
    
    @Override
    public List<OfflineMessage> findByReceiverAndConversation(Long receiverId, String conversationId, int limit) {
        log.debug("查询离线消息: receiverId={}, conversationId={}, limit={}", receiverId, conversationId, limit);
        List<OfflineMessageDO> offlineMessageDOs = offlineMessageMapper.findByReceiverAndConversation(receiverId, conversationId, limit);
        return offlineMessageConvertor.toDomains(offlineMessageDOs);
    }
    
    @Override
    public List<OfflineMessage> findUnreadSummaryByReceiver(Long receiverId) {
        log.debug("查询未读离线消息摘要: receiverId={}", receiverId);
        List<OfflineMessageDO> offlineMessageDOs = offlineMessageMapper.findUnreadSummaryByReceiver(receiverId);
        return offlineMessageConvertor.toDomains(offlineMessageDOs);
    }
    
    @Override
    public int deleteByMsgIds(Long receiverId, List<String> msgIds) {
        if (msgIds == null || msgIds.isEmpty()) {
            return 0;
        }
        
        log.debug("删除离线消息: receiverId={}, msgIds={}", receiverId, msgIds);
        return offlineMessageMapper.deleteByMsgIds(receiverId, msgIds);
    }
    
    @Override
    public int clearByReceiverAndConversation(Long receiverId, String conversationId) {
        log.debug("清除离线消息: receiverId={}, conversationId={}", receiverId, conversationId);
        return offlineMessageMapper.clearByReceiverAndConversation(receiverId, conversationId);
    }
    
    @Override
    public int cleanExpiredMessages(long expireTimestamp) {
        log.info("清理过期离线消息: expireTimestamp={}", expireTimestamp);
        return offlineMessageMapper.cleanExpiredMessages(expireTimestamp);
    }
}