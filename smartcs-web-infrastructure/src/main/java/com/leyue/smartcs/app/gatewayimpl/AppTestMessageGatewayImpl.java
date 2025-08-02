package com.leyue.smartcs.app.gatewayimpl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.leyue.smartcs.app.convertor.AppTestMessageConvertor;
import com.leyue.smartcs.app.dataobject.AppTestMessageDO;
import com.leyue.smartcs.app.mapper.AppTestMessageMapper;
import com.leyue.smartcs.domain.app.entity.AppTestMessage;
import com.leyue.smartcs.domain.app.gateway.AppTestMessageGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AI应用测试消息网关实现
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AppTestMessageGatewayImpl implements AppTestMessageGateway {

    private final AppTestMessageMapper appTestMessageMapper;
    private final AppTestMessageConvertor appTestMessageConvertor;

    @Override
    public AppTestMessage save(AppTestMessage message) {
        try {
            AppTestMessageDO messageDO = appTestMessageConvertor.toDataObject(message);
            
            if (message.getId() == null) {
                // 新增
                appTestMessageMapper.insert(messageDO);
                message.setId(messageDO.getId());
                log.info("创建应用测试消息: messageId={}, sessionId={}, type={}", 
                        message.getMessageId(), message.getSessionId(), message.getMessageType());
            } else {
                // 更新
                appTestMessageMapper.updateById(messageDO);
                log.info("更新应用测试消息: messageId={}, sessionId={}", 
                        message.getMessageId(), message.getSessionId());
            }
            
            return message;
        } catch (Exception e) {
            log.error("保存应用测试消息失败: messageId={}, error={}", message.getMessageId(), e.getMessage(), e);
            throw new RuntimeException("保存应用测试消息失败", e);
        }
    }

    @Override
    public int batchSave(List<AppTestMessage> messages) {
        try {
            if (messages == null || messages.isEmpty()) {
                return 0;
            }
            
            List<AppTestMessageDO> messageDOs = messages.stream()
                    .map(appTestMessageConvertor::toDataObject)
                    .collect(Collectors.toList());
            
            int result = appTestMessageMapper.batchInsert(messageDOs);
            log.info("批量保存应用测试消息: count={}, result={}", messages.size(), result);
            return result;
        } catch (Exception e) {
            log.error("批量保存应用测试消息失败: count={}, error={}", messages.size(), e.getMessage(), e);
            throw new RuntimeException("批量保存应用测试消息失败", e);
        }
    }

    @Override
    public AppTestMessage findByMessageId(String messageId) {
        try {
            AppTestMessageDO messageDO = appTestMessageMapper.selectByMessageId(messageId);
            return messageDO != null ? appTestMessageConvertor.toEntity(messageDO) : null;
        } catch (Exception e) {
            log.error("根据消息ID查询消息失败: messageId={}, error={}", messageId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public List<AppTestMessage> findMessagesBySessionId(String sessionId, Integer limit, Integer offset) {
        try {
            List<AppTestMessageDO> messageDOs = appTestMessageMapper.findMessagesBySessionId(sessionId, limit, offset);
            return messageDOs.stream()
                    .map(appTestMessageConvertor::toEntity)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("根据会话ID查询消息失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public Integer countMessagesBySessionId(String sessionId) {
        try {
            return appTestMessageMapper.countMessagesBySessionId(sessionId);
        } catch (Exception e) {
            log.error("根据会话ID统计消息数量失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public AppTestMessage findLatestMessageBySessionId(String sessionId, String messageType) {
        try {
            AppTestMessageDO messageDO = appTestMessageMapper.findLatestMessageBySessionId(sessionId, messageType);
            return messageDO != null ? appTestMessageConvertor.toEntity(messageDO) : null;
        } catch (Exception e) {
            log.error("根据会话ID查询最新消息失败: sessionId={}, messageType={}, error={}", 
                     sessionId, messageType, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean updateMessageStatus(String messageId, String status, String errorMessage) {
        try {
            UpdateWrapper<AppTestMessageDO> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("message_id", messageId)
                    .eq("is_deleted", 0)
                    .set("status", status)
                    .set("updated_at", System.currentTimeMillis());
            
            if (errorMessage != null) {
                updateWrapper.set("error_message", errorMessage);
            }
            
            int result = appTestMessageMapper.update(null, updateWrapper);
            log.info("更新消息状态: messageId={}, status={}, result={}", messageId, status, result);
            return result > 0;
        } catch (Exception e) {
            log.error("更新消息状态失败: messageId={}, status={}, error={}", messageId, status, e.getMessage(), e);
            return false;
        }
    }
}