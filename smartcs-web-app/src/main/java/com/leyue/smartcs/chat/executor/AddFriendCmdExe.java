package com.leyue.smartcs.chat.executor;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.chat.Friend;
import com.leyue.smartcs.domain.chat.gateway.FriendGateway;
import com.leyue.smartcs.dto.chat.friend.AddFriendCmd;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 添加好友命令执行器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AddFriendCmdExe {
    
    private final FriendGateway friendGateway;
    
    public Response execute(AddFriendCmd cmd) {
        log.info("开始处理添加好友申请: fromUserId={}, toUserId={}", cmd.getFromUserId(), cmd.getToUserId());
        
        try {
            // 验证参数
            validateCommand(cmd);
            
            // 检查是否已存在好友关系或申请
            validateFriendRelation(cmd.getFromUserId(), cmd.getToUserId());
            
            // 创建好友申请
            Friend friendApplication = Friend.createApplication(
                cmd.getFromUserId(), 
                cmd.getToUserId(), 
                cmd.getApplyMessage()
            );
            
            // 保存申请
            Friend savedApplication = friendGateway.save(friendApplication);
            
            log.info("好友申请创建成功: id={}, fromUserId={}, toUserId={}", 
                    savedApplication.getId(), cmd.getFromUserId(), cmd.getToUserId());
            
            // TODO: 发送好友申请通知
            // notificationService.sendFriendApplication(savedApplication);
            
            return Response.buildSuccess();
            
        } catch (BizException e) {
            log.warn("添加好友申请失败: {}", e.getMessage());
            return Response.buildFailure(e.getErrCode(), e.getMessage());
        } catch (Exception e) {
            log.error("添加好友申请异常", e);
            return Response.buildFailure("FRIEND_APPLICATION_ERROR", "申请好友失败，请稍后重试");
        }
    }
    
    private void validateCommand(AddFriendCmd cmd) {
        if (cmd.getFromUserId() == null || cmd.getFromUserId().trim().isEmpty()) {
            throw new BizException("INVALID_PARAM", "发起用户ID不能为空");
        }
        
        if (cmd.getToUserId() == null || cmd.getToUserId().trim().isEmpty()) {
            throw new BizException("INVALID_PARAM", "目标用户ID不能为空");
        }
        
        if (cmd.getFromUserId().equals(cmd.getToUserId())) {
            throw new BizException("INVALID_PARAM", "不能添加自己为好友");
        }
        
        if (cmd.getApplyMessage() != null && cmd.getApplyMessage().length() > 200) {
            throw new BizException("INVALID_PARAM", "申请消息不能超过200字符");
        }
    }
    
    private void validateFriendRelation(String fromUserId, String toUserId) {
        // 检查是否已经是好友
        if (friendGateway.isFriend(fromUserId, toUserId)) {
            throw new BizException("ALREADY_FRIENDS", "你们已经是好友关系");
        }
        
        // 检查是否已有申请
        if (friendGateway.hasApplication(fromUserId, toUserId)) {
            throw new BizException("APPLICATION_EXISTS", "已发送过好友申请，请等待对方处理");
        }
        
        // 检查对方是否已向我发送申请
        if (friendGateway.hasApplication(toUserId, fromUserId)) {
            throw new BizException("REVERSE_APPLICATION_EXISTS", "对方已向您发送好友申请，请前往好友申请页面处理");
        }
        
        // 检查是否被对方拉黑
        Friend existingRelation = friendGateway.findByUsers(fromUserId, toUserId);
        if (existingRelation != null && existingRelation.isBlocked()) {
            // 检查是否是对方拉黑了我
            if (existingRelation.getToUserId().equals(fromUserId)) {
                throw new BizException("USER_BLOCKED", "无法添加该用户为好友");
            }
        }
    }
}