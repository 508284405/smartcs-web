package com.leyue.smartcs.wap;

import com.leyue.smartcs.api.chat.dto.MessageVO;
import com.leyue.smartcs.api.chat.dto.SendMessageRequest;
import com.leyue.smartcs.dto.chat.MessageDTO;
import com.leyue.smartcs.dto.chat.SendMessageCmd;
import com.leyue.smartcs.chat.service.MessageService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * WAP端消息管理控制器
 */
@RestController
@RequestMapping("/api/wap/chat/messages")
public class ChatMessageWapController {
    
    @Autowired
    private MessageService messageService;

    /**
     * 发送消息
     *
     * @param request 发送消息请求
     * @return 消息视图对象
     */
    @PostMapping
    public MessageVO sendMessage(@RequestBody SendMessageRequest request) {
        SendMessageCmd cmd = new SendMessageCmd();
        BeanUtils.copyProperties(request, cmd);
        
        MessageDTO messageDTO = messageService.sendMessage(cmd);
        return convertToVO(messageDTO);
    }

    /**
     * 获取会话消息历史
     *
     * @param sessionId 会话ID
     * @param limit 限制数量
     * @return 消息视图对象列表
     */
    @GetMapping("/session/{sessionId}")
    public List<MessageVO> getSessionMessages(@PathVariable Long sessionId, @RequestParam(defaultValue = "20") int limit) {
        List<MessageDTO> messageDTOList = messageService.getSessionMessages(sessionId, limit);
        return messageDTOList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 分页获取会话消息历史
     *
     * @param sessionId 会话ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 消息视图对象列表
     */
    @GetMapping("/session/{sessionId}/page")
    public List<MessageVO> getSessionMessagesWithPagination(
            @PathVariable Long sessionId, 
            @RequestParam(defaultValue = "0") int offset, 
            @RequestParam(defaultValue = "20") int limit) {
        List<MessageDTO> messageDTOList = messageService.getSessionMessagesWithPagination(sessionId, offset, limit);
        return messageDTOList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    /**
     * 将消息DTO转换为消息视图对象
     *
     * @param messageDTO 消息DTO
     * @return 消息视图对象
     */
    private MessageVO convertToVO(MessageDTO messageDTO) {
        if (messageDTO == null) {
            return null;
        }
        
        MessageVO messageVO = new MessageVO();
        BeanUtils.copyProperties(messageDTO, messageVO);
        
        // 处理时间转换
        if (messageDTO.getCreatedAt() != null) {
            messageVO.setCreatedAt(Date.from(messageDTO.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant()));
        }
        
        return messageVO;
    }
}
