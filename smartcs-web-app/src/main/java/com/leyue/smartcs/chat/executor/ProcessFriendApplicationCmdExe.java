package com.leyue.smartcs.chat.executor;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.chat.Friend;
import com.leyue.smartcs.domain.chat.gateway.FriendGateway;
import com.leyue.smartcs.dto.chat.friend.ProcessFriendApplicationCmd;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 处理好友申请命令执行器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessFriendApplicationCmdExe {
    
    private final FriendGateway friendGateway;
    
    public Response execute(ProcessFriendApplicationCmd cmd) {
        log.info("开始处理好友申请: applicationId={}, action={}, processedBy={}", 
                cmd.getApplicationId(), cmd.getAction(), cmd.getProcessedBy());
        
        try {
            // 验证参数
            validateCommand(cmd);
            
            // 查找申请记录
            Friend application = friendGateway.findById(cmd.getApplicationId());
            if (application == null) {
                throw new BizException("APPLICATION_NOT_FOUND", "好友申请不存在");
            }
            
            // 验证权限
            validatePermission(application, cmd.getProcessedBy());
            
            // 检查申请状态
            if (!application.isPending()) {
                throw new BizException("APPLICATION_ALREADY_PROCESSED", "申请已被处理");
            }
            
            // 检查申请是否过期
            if (application.isExpired()) {
                throw new BizException("APPLICATION_EXPIRED", "申请已过期");
            }
            
            // 根据操作类型处理申请
            processApplication(application, cmd);
            
            // 保存处理结果
            Friend savedApplication = friendGateway.save(application);
            
            log.info("好友申请处理完成: id={}, action={}, status={}", 
                    savedApplication.getId(), cmd.getAction(), savedApplication.getStatus());
            
            // TODO: 发送处理结果通知
            // notificationService.sendFriendApplicationResult(savedApplication);
            
            return Response.buildSuccess();
            
        } catch (BizException e) {
            log.warn("处理好友申请失败: {}", e.getMessage());
            return Response.buildFailure(e.getErrCode(), e.getMessage());
        } catch (Exception e) {
            log.error("处理好友申请异常", e);
            return Response.buildFailure("FRIEND_PROCESS_ERROR", "处理申请失败，请稍后重试");
        }
    }
    
    private void validateCommand(ProcessFriendApplicationCmd cmd) {
        if (cmd.getApplicationId() == null) {
            throw new BizException("INVALID_PARAM", "申请ID不能为空");
        }
        
        if (cmd.getAction() == null || cmd.getAction().trim().isEmpty()) {
            throw new BizException("INVALID_PARAM", "操作类型不能为空");
        }
        
        if (!isValidAction(cmd.getAction())) {
            throw new BizException("INVALID_PARAM", "无效的操作类型");
        }
        
        if (cmd.getProcessedBy() == null || cmd.getProcessedBy().trim().isEmpty()) {
            throw new BizException("INVALID_PARAM", "处理者用户ID不能为空");
        }
        
        if ("reject".equals(cmd.getAction()) && 
            (cmd.getRejectReason() == null || cmd.getRejectReason().trim().isEmpty())) {
            throw new BizException("INVALID_PARAM", "拒绝时必须填写原因");
        }
        
        if (cmd.getRejectReason() != null && cmd.getRejectReason().length() > 200) {
            throw new BizException("INVALID_PARAM", "拒绝原因不能超过200字符");
        }
        
        if (cmd.getRemarkName() != null && cmd.getRemarkName().length() > 50) {
            throw new BizException("INVALID_PARAM", "备注名称不能超过50字符");
        }
    }
    
    private boolean isValidAction(String action) {
        return "accept".equals(action) || "reject".equals(action) || "block".equals(action);
    }
    
    private void validatePermission(Friend application, String processedBy) {
        // 只有申请的接收者才能处理申请
        if (!application.getToUserId().equals(processedBy)) {
            throw new BizException("NO_PERMISSION", "无权限处理此申请");
        }
    }
    
    private void processApplication(Friend application, ProcessFriendApplicationCmd cmd) {
        switch (cmd.getAction()) {
            case "accept":
                application.accept(cmd.getProcessedBy());
                
                // 设置好友信息
                if (cmd.getRemarkName() != null && !cmd.getRemarkName().trim().isEmpty()) {
                    application.setRemark(cmd.getRemarkName(), cmd.getProcessedBy());
                }
                if (cmd.getFriendGroup() != null && !cmd.getFriendGroup().trim().isEmpty()) {
                    application.setGroup(cmd.getFriendGroup(), cmd.getProcessedBy());
                }
                break;
                
            case "reject":
                application.reject(cmd.getProcessedBy(), cmd.getRejectReason());
                break;
                
            case "block":
                application.block(cmd.getProcessedBy());
                break;
                
            default:
                throw new BizException("INVALID_ACTION", "不支持的操作类型");
        }
    }
}