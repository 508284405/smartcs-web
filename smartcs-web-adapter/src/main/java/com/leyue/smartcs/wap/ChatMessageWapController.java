package com.leyue.smartcs.wap;

import com.leyue.smartcs.api.chat.dto.MessageVO;
import com.leyue.smartcs.api.chat.dto.SendMessageRequest;
import com.leyue.smartcs.dto.chat.MessageDTO;
import com.leyue.smartcs.dto.chat.SendMessageCmd;
import com.leyue.smartcs.dto.chat.GetMessagesQry;
import com.leyue.smartcs.chat.service.MessageService;
import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.PageResponse;
import com.leyue.smartcs.chat.convertor.ChatMessageConvertor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * WAP端消息管理控制器
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/wap/chat/messages")
public class ChatMessageWapController {
    
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
     * @param beforeMessageId 消息ID，获取该消息之前的历史，为空则获取最新消息
     * @param limit 限制数量
     * @return 消息视图对象列表
     */
    @GetMapping("/session/{sessionId}")
    public MultiResponse<MessageVO> getSessionMessages(GetMessagesQry qry) {
        List<MessageDTO> messageDTOList = messageService.getSessionMessages(qry);
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
    public PageResponse<MessageVO> getSessionMessagesWithPagination(
            @PathVariable Long sessionId, 
            @RequestParam(defaultValue = "0") int offset, 
            @RequestParam(defaultValue = "20") int limit) {
        List<MessageDTO> messageDTOList = messageService.getSessionMessagesWithPagination(sessionId, offset, limit);
        return PageResponse.of(messageConvertor.toVOList(messageDTOList),0,0,0);
    }
}
