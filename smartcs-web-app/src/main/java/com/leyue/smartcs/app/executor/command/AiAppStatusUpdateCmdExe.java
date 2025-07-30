package com.leyue.smartcs.app.executor.command;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.app.AiApp;
import com.leyue.smartcs.domain.app.enums.AppStatus;
import com.leyue.smartcs.domain.app.gateway.AiAppGateway;
import com.leyue.smartcs.dto.app.AiAppStatusUpdateCmd;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * AI应用状态更新命令执行器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiAppStatusUpdateCmdExe {

    private final AiAppGateway aiAppGateway;
    
    /**
     * 执行AI应用状态更新
     */
    public Response execute(AiAppStatusUpdateCmd cmd) {
        log.info("执行AI应用状态更新, ID: {}, 状态: {}", cmd.getId(), cmd.getStatus());
        
        try {
            // 验证状态
            AppStatus newStatus;
            try {
                newStatus = AppStatus.valueOf(cmd.getStatus());
            } catch (IllegalArgumentException e) {
                throw new BizException("INVALID_APP_STATUS", "无效的应用状态: " + cmd.getStatus());
            }
            
            // 查找现有应用
            AiApp existingApp = aiAppGateway.getById(cmd.getId());
            if (existingApp == null) {
                throw new BizException("APP_NOT_FOUND", "应用不存在");
            }
            
            // 检查状态转换是否合法
            AppStatus currentStatus = existingApp.getStatus();
            if (!isValidStatusTransition(currentStatus, newStatus)) {
                throw new BizException("INVALID_STATUS_TRANSITION", 
                    String.format("无效的状态转换: %s -> %s", 
                        currentStatus.getName(), newStatus.getName()));
            }
            
            // 执行状态更新
            switch (newStatus) {
                case PUBLISHED:
                    existingApp.publish();
                    break;
                case DISABLED:
                    existingApp.disable();
                    break;
                case DRAFT:
                    // 从已停用回到草稿状态（如果需要支持的话）
                    existingApp.setStatus(AppStatus.DRAFT);
                    existingApp.setUpdatedAt(System.currentTimeMillis());
                    break;
                default:
                    throw new BizException("UNSUPPORTED_STATUS", "不支持的状态: " + newStatus.getName());
            }
            
            // 保存更新
            aiAppGateway.update(existingApp);
            
            log.info("AI应用状态更新成功, ID: {}, 状态: {} -> {}", 
                existingApp.getId(), currentStatus.getName(), newStatus.getName());
            
            return Response.buildSuccess();
            
        } catch (BizException e) {
            log.warn("AI应用状态更新失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("AI应用状态更新失败", e);
            throw new BizException("APP_STATUS_UPDATE_ERROR", "AI应用状态更新失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查状态转换是否合法
     */
    private boolean isValidStatusTransition(AppStatus from, AppStatus to) {
        if (from == to) {
            return false; // 相同状态不需要转换
        }
        
        switch (from) {
            case DRAFT:
                return to == AppStatus.PUBLISHED;
            case PUBLISHED:
                return to == AppStatus.DISABLED;
            case DISABLED:
                return to == AppStatus.PUBLISHED || to == AppStatus.DRAFT;
            default:
                return false;
        }
    }
}