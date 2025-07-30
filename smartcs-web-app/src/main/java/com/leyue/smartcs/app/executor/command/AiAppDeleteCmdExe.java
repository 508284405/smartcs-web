package com.leyue.smartcs.app.executor.command;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.app.AiApp;
import com.leyue.smartcs.domain.app.gateway.AiAppGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * AI应用删除命令执行器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiAppDeleteCmdExe {

    private final AiAppGateway aiAppGateway;
    
    /**
     * 执行AI应用删除
     */
    public Response execute(Long id) {
        log.info("执行AI应用删除, ID: {}", id);
        
        try {
            // 查找现有应用
            AiApp existingApp = aiAppGateway.getById(id);
            if (existingApp == null) {
                throw new BizException("APP_NOT_FOUND", "应用不存在");
            }
            
            // 检查应用是否可以删除（只有草稿状态的应用可以删除）
            if (!existingApp.isEditable()) {
                throw new BizException("APP_NOT_DELETABLE", 
                    "只有草稿状态的应用可以删除，当前状态: " + existingApp.getStatus().getName());
            }
            
            // 执行删除
            boolean deleted = aiAppGateway.delete(id);
            if (!deleted) {
                throw new BizException("APP_DELETE_FAILED", "应用删除失败");
            }
            
            log.info("AI应用删除成功, ID: {}, 名称: {}", id, existingApp.getName());
            
            return Response.buildSuccess();
            
        } catch (BizException e) {
            log.warn("AI应用删除失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("AI应用删除失败", e);
            throw new BizException("APP_DELETE_ERROR", "AI应用删除失败: " + e.getMessage());
        }
    }
}