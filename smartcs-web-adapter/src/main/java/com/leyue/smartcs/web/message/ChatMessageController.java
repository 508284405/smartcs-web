package com.leyue.smartcs.web.message;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.api.MessageService;
import com.leyue.smartcs.chat.convertor.ChatMessageConvertor;
import com.leyue.smartcs.dto.chat.GetMessagesQry;
import com.leyue.smartcs.dto.chat.MessageDTO;
import com.leyue.smartcs.dto.chat.MessageVO;
import com.leyue.smartcs.dto.chat.SendMessageCmd;
import com.leyue.smartcs.dto.chat.SendMessageRequest;

import lombok.RequiredArgsConstructor;

/**
 * 消息管理控制器
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/chat/messages")
public class ChatMessageController {
    
    private final MessageService messageService;
    private final ChatMessageConvertor messageConvertor;

    // /**
    //  * 发送消息
    //  *
    //  * @param request 发送消息请求
    //  * @return 消息视图对象
    //  */
    // @PostMapping
    // public SingleResponse<MessageVO> sendMessage(@RequestBody SendMessageRequest request) {
    //     SendMessageCmd cmd = new SendMessageCmd();
    //     messageConvertor.toCmd(request, cmd);
        
    //     MessageDTO messageDTO = messageService.sendMessage(cmd);
    //     return SingleResponse.of(messageConvertor.toVO(messageDTO));
    // }

    /**
     * 获取会话消息历史
     *
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
    public PageResponse<MessageDTO> getSessionMessagesWithPagination(
            @PathVariable Long sessionId, 
            @RequestParam(defaultValue = "0") int offset, 
            @RequestParam(defaultValue = "20") int limit) {
        List<MessageDTO> messageDTOList = messageService.getSessionMessagesWithPagination(sessionId, offset, limit);
        return PageResponse.of(messageDTOList,0,0,0);
    }
}
