package com.leyue.smartcs.domain.intent.domainservice;

import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.intent.entity.Intent;
import com.leyue.smartcs.domain.intent.entity.IntentVersion;
import com.leyue.smartcs.domain.intent.enums.IntentStatus;
import com.leyue.smartcs.domain.intent.enums.VersionStatus;
import com.leyue.smartcs.domain.intent.gateway.IntentGateway;
import com.leyue.smartcs.domain.intent.gateway.IntentVersionGateway;
import com.leyue.smartcs.domain.common.gateway.IdGeneratorGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 意图领域服务
 * 
 * @author Claude
 */
@Service
@RequiredArgsConstructor
public class IntentDomainService {
    
    private final IntentGateway intentGateway;
    private final IntentVersionGateway versionGateway;
    private final IdGeneratorGateway idGeneratorGateway;
    
    /**
     * 创建意图
     * @param catalogId 目录ID
     * @param name 意图名称
     * @param code 意图编码
     * @param description 描述
     * @param creatorId 创建者ID
     * @return 创建的意图对象
     */
    public Intent createIntent(Long catalogId, String name, String code, String description, Long creatorId) {
        if (!StringUtils.hasText(name)) {
            throw new BizException("意图名称不能为空");
        }
        if (!StringUtils.hasText(code)) {
            throw new BizException("意图编码不能为空");
        }
        
        // 检查编码唯一性
        Intent existingIntent = intentGateway.findByCode(code);
        if (existingIntent != null) {
            throw new BizException("意图编码已存在: " + code);
        }
        
        long currentTime = System.currentTimeMillis();
        Intent intent = Intent.builder()
                .catalogId(catalogId)
                .name(name)
                .code(code)
                .description(description)
                .status(IntentStatus.DRAFT)
                .creatorId(creatorId)
                .isDeleted(false)
                .createdAt(currentTime)
                .updatedAt(currentTime)
                .build();
        
        return intentGateway.save(intent);
    }
    
    /**
     * 激活意图
     * @param intentId 意图ID
     * @param versionId 版本ID
     */
    public void activateIntent(Long intentId, Long versionId) {
        Intent intent = intentGateway.findById(intentId);
        if (intent == null) {
            throw new BizException("意图不存在");
        }
        
        IntentVersion version = versionGateway.findById(versionId);
        if (version == null || !version.getIntentId().equals(intentId)) {
            throw new BizException("版本不存在或不属于该意图");
        }
        
        if (version.getStatus() != VersionStatus.ACTIVE) {
            throw new BizException("只有激活状态的版本才能激活意图");
        }
        
        intent.setCurrentVersionId(versionId);
        intent.setStatus(IntentStatus.ACTIVE);
        intent.setUpdatedAt(System.currentTimeMillis());
        
        intentGateway.update(intent);
    }
    
    /**
     * 废弃意图
     * @param intentId 意图ID
     */
    public void deprecateIntent(Long intentId) {
        Intent intent = intentGateway.findById(intentId);
        if (intent == null) {
            throw new BizException("意图不存在");
        }
        
        intent.setStatus(IntentStatus.DEPRECATED);
        intent.setUpdatedAt(System.currentTimeMillis());
        
        intentGateway.update(intent);
    }
    
    /**
     * 验证意图编码格式
     * @param code 意图编码
     * @return 是否有效
     */
    public boolean isValidIntentCode(String code) {
        if (!StringUtils.hasText(code)) {
            return false;
        }
        // 编码格式：字母开头，只能包含字母、数字和下划线
        return code.matches("^[a-zA-Z][a-zA-Z0-9_]*$");
    }
}