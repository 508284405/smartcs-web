package com.leyue.smartcs.bot.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.api.MessageService;
import com.leyue.smartcs.dto.bot.BotContextDTO;
import com.leyue.smartcs.dto.chat.MessageDTO;
import com.leyue.smartcs.dto.common.SingleClientObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 上下文查询执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ContextQryExe {
    
    private final MessageService messageService;
    
    /**
     * 执行上下文查询
     * @param sessionId 会话ID
     * @return 上下文
     */
    public SingleResponse<BotContextDTO> execute(SingleClientObject<String> sessionId) {
        log.info("执行上下文查询: {}", sessionId);
        
        try {
            // 参数校验
            if (sessionId.getValue() == null || sessionId.getValue().trim().isEmpty()) {
                throw new BizException("会话ID不能为空");
            }
            
            // 转换sessionId为Long类型
            Long sessionIdLong;
            try {
                sessionIdLong = Long.parseLong(sessionId.getValue());
            } catch (NumberFormatException e) {
                log.warn("无效的会话ID格式: {}", sessionId.getValue());
                return SingleResponse.of(BotContextDTO.builder()
                        .sessionId(sessionId.getValue())
                        .history(new ArrayList<>())
                        .totalMessages(0)
                        .lastUpdatedAt(System.currentTimeMillis())
                        .build());
            }
            
            // 从chat message服务获取消息历史
            List<MessageDTO> messages = messageService.getSessionMessagesWithPagination(sessionIdLong, 0, 100);
            
            if (messages.isEmpty()) {
                log.info("会话不存在或无消息: {}", sessionId.getValue());
                return SingleResponse.of(BotContextDTO.builder()
                        .sessionId(sessionId.getValue())
                        .history(new ArrayList<>())
                        .totalMessages(0)
                        .lastUpdatedAt(System.currentTimeMillis())
                        .build());
            }
            
            // 转换DTO
            BotContextDTO contextDTO = convertToDTO(sessionId.getValue(), messages);
            
            log.info("上下文查询完成，共 {} 条消息", contextDTO.getTotalMessages());
            return SingleResponse.of(contextDTO);
            
        } catch (Exception e) {
            log.error("上下文查询失败: {}", e.getMessage(), e);
            throw new BizException("上下文查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行上下文删除
     * @param sessionId 会话ID
     * @return 是否成功
     */
    public SingleResponse<Boolean> executeDelete(SingleClientObject<String> sessionId) {
        log.info("执行上下文删除: {}", sessionId);
        
        try {
            // 参数校验
            if (sessionId.getValue() == null || sessionId.getValue().trim().isEmpty()) {
                throw new BizException("会话ID不能为空");
            }
            
            // 注意：这里暂时返回true，因为chat message服务通常不提供删除整个会话的功能
            // 如果需要删除功能，需要在MessageService中添加相应的方法
            log.warn("上下文删除功能暂未实现，会话ID: {}", sessionId.getValue());
            
            return SingleResponse.of(true);
            
        } catch (Exception e) {
            log.error("上下文删除失败: {}", e.getMessage(), e);
            throw new BizException("上下文删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 将消息列表转换为DTO
     * @param sessionId 会话ID
     * @param messages 消息列表
     * @return DTO
     */
    private BotContextDTO convertToDTO(String sessionId, List<MessageDTO> messages) {
        List<BotContextDTO.Message> messageDTOs = new ArrayList<>();
        Long lastUpdatedAt = System.currentTimeMillis();
        
        for (MessageDTO message : messages) {
            BotContextDTO.Message messageDTO = BotContextDTO.Message.builder()
                    .id(message.getMsgId())
                    .role(message.getChatType())
                    .content(message.getContent())
                    .createdAt(message.getCreatedAt())
                    .build();
            messageDTOs.add(messageDTO);
            
            // 更新最后更新时间
            if (message.getCreatedAt() != null && message.getCreatedAt() > lastUpdatedAt) {
                lastUpdatedAt = message.getCreatedAt();
            }
        }
        
        return BotContextDTO.builder()
                .sessionId(sessionId)
                .history(messageDTOs)
                .totalMessages(messageDTOs.size())
                .lastUpdatedAt(lastUpdatedAt)
                .build();
    }
} 