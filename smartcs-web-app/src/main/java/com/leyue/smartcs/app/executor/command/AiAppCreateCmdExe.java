package com.leyue.smartcs.app.executor.command;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.app.convertor.AiAppAppConvertor;
import com.leyue.smartcs.domain.app.AiApp;
import com.leyue.smartcs.domain.app.enums.AppStatus;
import com.leyue.smartcs.domain.app.enums.AppType;
import com.leyue.smartcs.domain.app.gateway.AiAppGateway;
import com.leyue.smartcs.dto.app.AiAppCreateCmd;
import com.leyue.smartcs.dto.app.AiAppDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * AI应用创建命令执行器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiAppCreateCmdExe {

    private final AiAppGateway aiAppGateway;
    
    /**
     * 执行AI应用创建
     */
    public SingleResponse<AiAppDTO> execute(AiAppCreateCmd cmd) {
        log.info("执行AI应用创建, 名称: {}, 编码: {}", cmd.getName(), cmd.getCode());
        
        try {
            // 验证应用类型
            AppType appType;
            try {
                appType = AppType.valueOf(cmd.getType());
            } catch (IllegalArgumentException e) {
                throw new BizException("INVALID_APP_TYPE", "无效的应用类型: " + cmd.getType());
            }
            
            // 检查编码是否已存在
            if (aiAppGateway.existsByCode(cmd.getCode(), null)) {
                throw new BizException("APP_CODE_EXISTS", "应用编码已存在: " + cmd.getCode());
            }
            
            // 构建领域对象
            AiApp aiApp = AiApp.builder()
                    .name(cmd.getName())
                    .code(cmd.getCode())
                    .description(cmd.getDescription())
                    .type(appType)
                    .config(cmd.getConfig())
                    .status(AppStatus.DRAFT)
                    .icon(cmd.getIcon())
                    .tags(cmd.getTags())
                    .createdAt(System.currentTimeMillis())
                    .updatedAt(System.currentTimeMillis())
                    .build();
                    
            // 业务验证
            if (!aiApp.isValidName()) {
                throw new BizException("INVALID_APP_NAME", "应用名称不合法");
            }
            
            if (!aiApp.isValidCode()) {
                throw new BizException("INVALID_APP_CODE", "应用编码不合法");
            }
            
            // 保存到数据库
            AiApp savedApp = aiAppGateway.create(aiApp);
            
            // 转换为DTO返回
            AiAppDTO result = AiAppAppConvertor.INSTANCE.domainToDto(savedApp);
            
            log.info("AI应用创建成功, ID: {}, 名称: {}", savedApp.getId(), savedApp.getName());
            
            return SingleResponse.of(result);
            
        } catch (BizException e) {
            log.warn("AI应用创建失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("AI应用创建失败", e);
            throw new BizException("APP_CREATE_ERROR", "AI应用创建失败: " + e.getMessage());
        }
    }
}