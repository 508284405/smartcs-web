package com.leyue.smartcs.web.chat;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.chat.service.UserStatusService;
import com.leyue.smartcs.domain.chat.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

/**
 * 用户状态控制器
 */
@RestController
@RequestMapping("/api/chat/user-status")
@RequiredArgsConstructor
public class UserStatusController {
    
    private final UserStatusService userStatusService;
    
    /**
     * 获取用户状态
     *
     * @param userId 用户ID
     * @return 用户状态信息
     */
    @GetMapping("/{userId}")
    public SingleResponse<Map<String, Object>> getUserStatus(@PathVariable String userId) {
        Map<String, Object> status = userStatusService.getUserStatus(userId);
        return SingleResponse.of(status);
    }
    
    /**
     * 设置用户状态
     *
     * @param userId 用户ID
     * @param status 状态代码
     * @param statusMessage 状态消息（可选）
     * @return 操作结果
     */
    @PostMapping("/{userId}")
    public Response setUserStatus(@PathVariable String userId,
                                @RequestParam String status,
                                @RequestParam(required = false) String statusMessage) {
        try {
            UserStatus userStatus = UserStatus.fromCode(status);
            userStatusService.setUserStatus(userId, userStatus, statusMessage);
            return Response.buildSuccess();
        } catch (Exception e) {
            return Response.buildFailure("SET_STATUS_FAILED", e.getMessage());
        }
    }
    
    /**
     * 获取所有在线用户
     *
     * @return 在线用户ID列表
     */
    @GetMapping("/online")
    public MultiResponse<String> getOnlineUsers() {
        Set<String> onlineUsers = userStatusService.getOnlineUsers();
        return MultiResponse.of(onlineUsers);
    }
    
    /**
     * 检查用户是否在线
     *
     * @param userId 用户ID
     * @return 是否在线
     */
    @GetMapping("/{userId}/online")
    public SingleResponse<Boolean> isUserOnline(@PathVariable String userId) {
        boolean isOnline = userStatusService.isUserOnline(userId);
        return SingleResponse.of(isOnline);
    }
    
    /**
     * 用户上线
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    @PostMapping("/{userId}/online")
    public Response userOnline(@PathVariable String userId) {
        try {
            userStatusService.userOnline(userId);
            return Response.buildSuccess();
        } catch (Exception e) {
            return Response.buildFailure("USER_ONLINE_FAILED", e.getMessage());
        }
    }
    
    /**
     * 用户离线
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    @PostMapping("/{userId}/offline")
    public Response userOffline(@PathVariable String userId) {
        try {
            userStatusService.userOffline(userId);
            return Response.buildSuccess();
        } catch (Exception e) {
            return Response.buildFailure("USER_OFFLINE_FAILED", e.getMessage());
        }
    }
}