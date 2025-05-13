package com.leyue.smartcs.bot.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.bot.dto.BotContextDTO;
import com.leyue.smartcs.domain.bot.gateway.SessionGateway;
import com.leyue.smartcs.domain.bot.model.Conversation;
import com.leyue.smartcs.domain.bot.model.Message;
import com.leyue.smartcs.dto.common.SingleClientObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 上下文查询执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ContextQryExe {
    
    private final SessionGateway sessionGateway;
    
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
            
            // 查询会话
            Optional<Conversation> conversationOpt = sessionGateway.getConversation(sessionId.getValue());
            
            if (conversationOpt.isEmpty()) {
                log.info("会话不存在: {}", sessionId.getValue());
                return SingleResponse.of(BotContextDTO.builder()
                        .sessionId(sessionId.getValue())
                        .history(new ArrayList<>())
                        .totalMessages(0)
                        .lastUpdatedAt(System.currentTimeMillis())
                        .build());
            }
            
            Conversation conversation = conversationOpt.get();
            
            // 转换DTO
            BotContextDTO contextDTO = convertToDTO(conversation);
            
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
            
            // 删除会话
            boolean result = sessionGateway.deleteConversation(sessionId.getValue());
            
            log.info("上下文删除完成: {}", result);
            return SingleResponse.of(result);
            
        } catch (Exception e) {
            log.error("上下文删除失败: {}", e.getMessage(), e);
            throw new BizException("上下文删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 将会话转换为DTO
     * @param conversation 会话
     * @return DTO
     */
    private BotContextDTO convertToDTO(Conversation conversation) {
        List<BotContextDTO.Message> messageDTOs = new ArrayList<>();
        
        for (Message message : conversation.getMessages()) {
            BotContextDTO.Message messageDTO = BotContextDTO.Message.builder()
                    .id(message.getId())
                    .role(message.getRole())
                    .content(message.getContent())
                    .createdAt(message.getCreatedAt())
                    .build();
            messageDTOs.add(messageDTO);
        }
        
        return BotContextDTO.builder()
                .sessionId(conversation.getSessionId())
                .history(messageDTOs)
                .totalMessages(messageDTOs.size())
                .lastUpdatedAt(conversation.getUpdatedAt())
                .build();
    }
} 