package com.leyue.smartcs.intent.executor.command;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.config.context.UserContext;
import com.leyue.smartcs.domain.intent.entity.IntentCatalog;
import com.leyue.smartcs.domain.intent.gateway.IntentCatalogGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 意图目录创建命令执行器
 * 
 * @author Claude
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IntentCatalogCreateCmdExe {
    
    private final IntentCatalogGateway catalogGateway;
    
    /**
     * 执行意图目录创建
     */
    public SingleResponse<Long> execute(String name, String code, String description, Long parentId, Integer sortOrder) {
        // 参数验证
        if (!StringUtils.hasText(name)) {
            throw new BizException("目录名称不能为空");
        }
        if (!StringUtils.hasText(code)) {
            throw new BizException("目录编码不能为空");
        }
        
        // 检查编码唯一性
        IntentCatalog existingCatalog = catalogGateway.findByCode(code);
        if (existingCatalog != null) {
            throw new BizException("目录编码已存在: " + code);
        }
        
        // 构建领域对象
        long currentTime = System.currentTimeMillis();
        IntentCatalog catalog = IntentCatalog.builder()
                .name(name)
                .code(code)
                .description(description)
                .parentId(parentId)
                .sortOrder(sortOrder != null ? sortOrder : 0)
                .creatorId(UserContext.getCurrentUser() != null ? UserContext.getCurrentUser().getId() : 1L)
                .isDeleted(false)
                .createdAt(currentTime)
                .updatedAt(currentTime)
                .build();
        
        // 保存
        IntentCatalog savedCatalog = catalogGateway.save(catalog);
        
        log.info("意图目录创建成功，ID: {}, 名称: {}", savedCatalog.getId(), savedCatalog.getName());
        
        return SingleResponse.of(savedCatalog.getId());
    }
}