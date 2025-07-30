package com.leyue.smartcs.app.executor.command;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.app.AiApp;
import com.leyue.smartcs.domain.app.gateway.AiAppGateway;
import com.leyue.smartcs.dto.app.AiAppUpdateCmd;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * AI应用更新命令执行器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiAppUpdateCmdExe {

    private final AiAppGateway aiAppGateway;
    
    /**
     * 执行AI应用更新
     */
    public Response execute(AiAppUpdateCmd cmd) {
        log.info("执行AI应用更新, ID: {}, 名称: {}", cmd.getId(), cmd.getName());
        
        try {
            // 查找现有应用
            AiApp existingApp = aiAppGateway.getById(cmd.getId());
            if (existingApp == null) {
                throw new BizException("APP_NOT_FOUND", "应用不存在");
            }
            
            // 检查是否可编辑
            if (!existingApp.isEditable()) {
                throw new BizException("APP_NOT_EDITABLE", "应用当前状态不可编辑");
            }
            
            // 更新应用信息
            existingApp.setName(cmd.getName());
            existingApp.setDescription(cmd.getDescription());
            existingApp.setConfig(cmd.getConfig());
            existingApp.setIcon(cmd.getIcon());
            existingApp.setTags(cmd.getTags());
            existingApp.setUpdatedAt(System.currentTimeMillis());
            
            // 业务验证
            if (!existingApp.isValidName()) {
                throw new BizException("INVALID_APP_NAME", "应用名称不合法");
            }
            
            // 保存更新
            aiAppGateway.update(existingApp);
            
            log.info("AI应用更新成功, ID: {}, 名称: {}", existingApp.getId(), existingApp.getName());
            
            return Response.buildSuccess();
            
        } catch (BizException e) {
            log.warn("AI应用更新失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("AI应用更新失败", e);
            throw new BizException("APP_UPDATE_ERROR", "AI应用更新失败: " + e.getMessage());
        }
    }
}