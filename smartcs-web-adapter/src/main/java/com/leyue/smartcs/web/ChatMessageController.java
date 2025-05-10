package com.leyue.smartcs.web;

import com.leyue.smartcs.api.chat.dto.MessageVO;
import com.leyue.smartcs.api.chat.dto.SendMessageRequest;
import com.leyue.smartcs.dto.chat.MessageDTO;
import com.leyue.smartcs.dto.chat.SendMessageCmd;
import com.leyue.smartcs.chat.service.MessageService;
import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.PageResponse;
import com.leyue.smartcs.convertor.ChatMessageConvertor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息管理控制器
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/chat/messages")
public class ChatMessageController {
    
    private final MessageService messageService;
    private final ChatMessageConvertor messageConvertor;

    /**
     * 发送消息
     *
     * @param request 发送消息请求
     * @return 消息视图对象
     */
    @PostMapping
    public SingleResponse<MessageVO> sendMessage(@RequestBody SendMessageRequest request) {
        SendMessageCmd cmd = new SendMessageCmd();
        messageConvertor.copyToCmd(request, cmd);
        
        MessageDTO messageDTO = messageService.sendMessage(cmd);
        return SingleResponse.of(messageConvertor.toVO(messageDTO));
    }

    /**
     * 获取会话消息历史
     *
     * @param sessionId 会话ID
     * @param limit 限制数量
     * @return 消息视图对象列表
     */
    @GetMapping("/session/{sessionId}")
    public MultiResponse<MessageVO> getSessionMessages(@PathVariable Long sessionId, @RequestParam(defaultValue = "20") int limit) {
        List<MessageDTO> messageDTOList = messageService.getSessionMessages(sessionId, limit);
        return MultiResponse.of(messageConvertor.toVOList(messageDTOList));
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
    public PageResponse<MessageDTO> getSessionMessagesWithPagination(
            @PathVariable Long sessionId, 
            @RequestParam(defaultValue = "0") int offset, 
            @RequestParam(defaultValue = "20") int limit) {
        List<MessageDTO> messageDTOList = messageService.getSessionMessagesWithPagination(sessionId, offset, limit);
        return PageResponse.of(messageDTOList,0,0,0);
    }
}
