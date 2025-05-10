package com.leyue.smartcs.mobile;

import com.leyue.smartcs.api.chat.dto.CreateSessionRequest;
import com.leyue.smartcs.api.chat.dto.SessionVO;
import com.leyue.smartcs.dto.chat.CreateSessionCmd;
import com.leyue.smartcs.dto.chat.SessionDTO;
import com.leyue.smartcs.chat.service.SessionService;
import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.PageResponse;
import com.leyue.smartcs.convertor.ChatSessionConvertor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 移动端会话管理控制器
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/mobile/chat/sessions")
public class ChatSessionMobileController {
    
    private final SessionService sessionService;
    private final ChatSessionConvertor sessionConvertor;

    /**
     * 创建会话
     *
     * @param request 创建会话请求
     * @return 会话视图对象
     */
    @PostMapping
    public SingleResponse<SessionVO> createSession(@RequestBody CreateSessionRequest request) {
        CreateSessionCmd cmd = new CreateSessionCmd();
        cmd.setCustomerId(request.getCustomerId());
        
        SessionDTO sessionDTO = sessionService.createSession(cmd);
        return SingleResponse.of(sessionConvertor.toVO(sessionDTO));
    }

    /**
     * 分配客服
     *
     * @param sessionId 会话ID
     * @param agentId 客服ID
     * @return 会话视图对象
     */
    @PostMapping("/{sessionId}/assign")
    public SingleResponse<SessionVO> assignAgent(@PathVariable Long sessionId, @RequestParam Long agentId) {
        SessionDTO sessionDTO = sessionService.assignAgent(sessionId, agentId);
        return SingleResponse.of(sessionConvertor.toVO(sessionDTO));
    }

    /**
     * 关闭会话
     *
     * @param sessionId 会话ID
     * @return 会话视图对象
     */
    @PostMapping("/{sessionId}/close")
    public SingleResponse<SessionVO> closeSession(@PathVariable Long sessionId) {
        SessionDTO sessionDTO = sessionService.closeSession(sessionId);
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
}
