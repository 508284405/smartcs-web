package com.leyue.smartcs.web;

import com.leyue.smartcs.api.chat.dto.CreateSessionRequest;
import com.leyue.smartcs.api.chat.dto.SessionVO;
import com.leyue.smartcs.dto.chat.CreateSessionCmd;
import com.leyue.smartcs.dto.chat.SessionDTO;
import com.leyue.smartcs.chat.service.SessionService;
import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.dto.MultiResponse;
import com.leyue.smartcs.convertor.ChatSessionConvertor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 会话管理控制器
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/chat/sessions")
public class ChatSessionController {
    
    private final SessionService sessionService;
    private final ChatSessionConvertor sessionConvertor;

    /**
     * 创建会话
     *
     * @param cmd 创建会话请求
     * @return 会话视图对象
     */
    @PostMapping
    public SingleResponse<SessionDTO> createSession(@RequestBody CreateSessionCmd cmd) {
        SessionDTO sessionDTO = sessionService.createSession(cmd);
        return SingleResponse.of(sessionDTO);
    }

    /**
     * 分配客服
     *
     * @param sessionId 会话ID
     * @param agentId 客服ID
     * @param agentName 客服名称 (可选)
     * @return 会话视图对象
     */
    @PostMapping("/{sessionId}/assign")
    public SingleResponse<SessionVO> assignAgent(
            @PathVariable Long sessionId, 
            @RequestParam Long agentId,
            @RequestParam(required = false) String agentName) {
        SessionDTO sessionDTO = sessionService.assignAgent(sessionId, agentId, agentName);
        return SingleResponse.of(sessionConvertor.toVO(sessionDTO));
    }

    /**
     * 关闭会话
     *
     * @param sessionId 会话ID
     * @param reason 关闭原因 (可选)
     * @return 会话视图对象
     */
    @PostMapping("/{sessionId}/close")
    public SingleResponse<SessionVO> closeSession(
            @PathVariable Long sessionId,
            @RequestParam(required = false) String reason) {
        SessionDTO sessionDTO = sessionService.closeSession(sessionId, reason);
        return SingleResponse.of(sessionConvertor.toVO(sessionDTO));
    }

    /**
     * 获取会话详情
     *
     * @param sessionId 会话ID
     * @return 会话视图对象
     */
    @GetMapping("/{sessionId}")
    public SingleResponse<SessionVO> getSessionDetail(@PathVariable Long sessionId) {
        SessionDTO sessionDTO = sessionService.getSessionDetail(sessionId);
        return SingleResponse.of(sessionConvertor.toVO(sessionDTO));
    }

    /**
     * 获取客户的会话列表
     *
     * @param customerId 客户ID
     * @param limit 限制数量
     * @return 会话视图对象列表
     */
    @GetMapping("/customer/{customerId}")
    public MultiResponse<SessionVO> getCustomerSessions(@PathVariable Long customerId, @RequestParam(defaultValue = "10") int limit) {
        List<SessionDTO> sessionDTOList = sessionService.getCustomerSessions(customerId, limit);
        return MultiResponse.of(sessionConvertor.toVOList(sessionDTOList));
    }

    /**
     * 获取客服的活跃会话列表
     *
     * @param agentId 客服ID
     * @return 会话视图对象列表
     */
    @GetMapping("/agent/{agentId}")
    public MultiResponse<SessionVO> getAgentActiveSessions(@PathVariable Long agentId) {
        List<SessionDTO> sessionDTOList = sessionService.getAgentActiveSessions(agentId);
        return MultiResponse.of(sessionConvertor.toVOList(sessionDTOList));
    }

    /**
     * 获取客户最新一条处理中的会话（排队或进行中）
     *
     * @param customerId 客户ID
     * @return 会话视图对象（如无则返回空）
     */
    @GetMapping("/customer/{customerId}/active")
    public SingleResponse<SessionDTO> getCustomerActiveSession(@PathVariable Long customerId) {
        SessionDTO sessionDTO = sessionService.getCustomerActiveSession(customerId);
        return SingleResponse.of(sessionDTO);
    }
}
