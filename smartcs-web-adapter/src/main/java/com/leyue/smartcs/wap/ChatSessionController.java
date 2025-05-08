package com.leyue.smartcs.wap;

import com.leyue.smartcs.api.chat.dto.CreateSessionRequest;
import com.leyue.smartcs.api.chat.dto.SessionVO;
import com.leyue.smartcs.dto.chat.CreateSessionCmd;
import com.leyue.smartcs.dto.chat.SessionDTO;
import com.leyue.smartcs.chat.service.SessionService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * WAP端会话管理控制器
 */
@RestController
@RequestMapping("/api/wap/chat/sessions")
public class ChatSessionController {
    
    @Autowired
    private SessionService sessionService;

    /**
     * 创建会话
     *
     * @param request 创建会话请求
     * @return 会话视图对象
     */
    @PostMapping
    public SessionVO createSession(@RequestBody CreateSessionRequest request) {
        CreateSessionCmd cmd = new CreateSessionCmd();
        cmd.setCustomerId(request.getCustomerId());
        
        SessionDTO sessionDTO = sessionService.createSession(cmd);
        return convertToVO(sessionDTO);
    }

    /**
     * 分配客服
     *
     * @param sessionId 会话ID
     * @param agentId 客服ID
     * @return 会话视图对象
     */
    @PostMapping("/{sessionId}/assign")
    public SessionVO assignAgent(@PathVariable Long sessionId, @RequestParam Long agentId) {
        SessionDTO sessionDTO = sessionService.assignAgent(sessionId, agentId);
        return convertToVO(sessionDTO);
    }

    /**
     * 关闭会话
     *
     * @param sessionId 会话ID
     * @return 会话视图对象
     */
    @PostMapping("/{sessionId}/close")
    public SessionVO closeSession(@PathVariable Long sessionId) {
        SessionDTO sessionDTO = sessionService.closeSession(sessionId);
        return convertToVO(sessionDTO);
    }

    /**
     * 获取会话详情
     *
     * @param sessionId 会话ID
     * @return 会话视图对象
     */
    @GetMapping("/{sessionId}")
    public SessionVO getSessionDetail(@PathVariable Long sessionId) {
        SessionDTO sessionDTO = sessionService.getSessionDetail(sessionId);
        return convertToVO(sessionDTO);
    }

    /**
     * 获取客户的会话列表
     *
     * @param customerId 客户ID
     * @param limit 限制数量
     * @return 会话视图对象列表
     */
    @GetMapping("/customer/{customerId}")
    public List<SessionVO> getCustomerSessions(@PathVariable Long customerId, @RequestParam(defaultValue = "10") int limit) {
        List<SessionDTO> sessionDTOList = sessionService.getCustomerSessions(customerId, limit);
        return sessionDTOList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取客服的活跃会话列表
     *
     * @param agentId 客服ID
     * @return 会话视图对象列表
     */
    @GetMapping("/agent/{agentId}")
    public List<SessionVO> getAgentActiveSessions(@PathVariable Long agentId) {
        List<SessionDTO> sessionDTOList = sessionService.getAgentActiveSessions(agentId);
        return sessionDTOList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    /**
     * 将会话DTO转换为会话视图对象
     *
     * @param sessionDTO 会话DTO
     * @return 会话视图对象
     */
    private SessionVO convertToVO(SessionDTO sessionDTO) {
        if (sessionDTO == null) {
            return null;
        }
        
        SessionVO sessionVO = new SessionVO();
        BeanUtils.copyProperties(sessionDTO, sessionVO);
        
        // 处理时间转换
        if (sessionDTO.getLastMsgTime() != null) {
            sessionVO.setLastMsgTime(Date.from(sessionDTO.getLastMsgTime().atZone(java.time.ZoneId.systemDefault()).toInstant()));
        }
        
        if (sessionDTO.getCreatedAt() != null) {
            sessionVO.setCreatedAt(Date.from(sessionDTO.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant()));
        }
        
        return sessionVO;
    }
}
