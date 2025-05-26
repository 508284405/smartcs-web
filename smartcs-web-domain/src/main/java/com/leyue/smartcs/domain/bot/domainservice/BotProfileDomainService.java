package com.leyue.smartcs.domain.bot.domainservice;

import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.bot.BotProfile;
import com.leyue.smartcs.domain.bot.gateway.BotProfileGateway;
import com.leyue.smartcs.domain.common.gateway.IdGeneratorGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * 机器人配置领域服务
 */
@Service
@RequiredArgsConstructor
public class BotProfileDomainService {
    
    private final BotProfileGateway botProfileGateway;
    private final IdGeneratorGateway idGeneratorGateway;
    
    /**
     * 创建机器人配置
     * @param botProfile 机器人配置
     * @return 创建后的机器人配置
     */
    public BotProfile createBotProfile(BotProfile botProfile) {
        // 业务验证
        validateBotProfile(botProfile);
        
        // 检查机器人名称是否已存在
        if (botProfileGateway.existsByBotName(botProfile.getBotName(), null)) {
            throw new BizException("机器人名称已存在: " + botProfile.getBotName());
        }
        
        // 生成ID和设置默认值
        botProfile.setBotId(idGeneratorGateway.generateId());
        botProfile.setIsDeleted(0);
        
        // 设置默认值
        if (botProfile.getMaxQps() == null) {
            botProfile.setMaxQps(10);
        }
        if (botProfile.getTemperature() == null) {
            botProfile.setTemperature(new BigDecimal("0.7"));
        }
        
        long currentTime = System.currentTimeMillis();
        botProfile.setCreatedAt(currentTime);
        botProfile.setUpdatedAt(currentTime);
        
        // 保存
        Long botId = botProfileGateway.createBotProfile(botProfile);
        botProfile.setBotId(botId);
        
        return botProfile;
    }
    
    /**
     * 更新机器人配置
     * @param botProfile 机器人配置
     * @return 更新后的机器人配置
     */
    public BotProfile updateBotProfile(BotProfile botProfile) {
        // 检查机器人是否存在
        Optional<BotProfile> existingOpt = botProfileGateway.findById(botProfile.getBotId());
        if (!existingOpt.isPresent()) {
            throw new BizException("机器人配置不存在: " + botProfile.getBotId());
        }
        
        BotProfile existing = existingOpt.get();
        if (existing.isDeleted()) {
            throw new BizException("机器人配置已删除，无法更新: " + botProfile.getBotId());
        }
        
        // 业务验证
        validateBotProfile(botProfile);
        
        // 检查机器人名称是否已存在（排除自己）
        if (botProfileGateway.existsByBotName(botProfile.getBotName(), botProfile.getBotId())) {
            throw new BizException("机器人名称已存在: " + botProfile.getBotName());
        }
        
        // 更新时间
        botProfile.setUpdatedAt(System.currentTimeMillis());
        
        // 保持创建信息不变
        botProfile.setCreatedAt(existing.getCreatedAt());
        botProfile.setCreatedBy(existing.getCreatedBy());
        botProfile.setIsDeleted(existing.getIsDeleted());
        
        // 更新
        boolean success = botProfileGateway.updateBotProfile(botProfile);
        if (!success) {
            throw new BizException("更新机器人配置失败");
        }
        
        return botProfile;
    }
    
    /**
     * 删除机器人配置
     * @param botId 机器人ID
     */
    public void deleteBotProfile(Long botId) {
        // 检查机器人是否存在
        Optional<BotProfile> existingOpt = botProfileGateway.findById(botId);
        if (!existingOpt.isPresent()) {
            throw new BizException("机器人配置不存在: " + botId);
        }
        
        BotProfile existing = existingOpt.get();
        if (existing.isDeleted()) {
            throw new BizException("机器人配置已删除: " + botId);
        }
        
        // 执行逻辑删除
        boolean success = botProfileGateway.deleteById(botId);
        if (!success) {
            throw new BizException("删除机器人配置失败");
        }
    }
    
    /**
     * 验证机器人配置
     * @param botProfile 机器人配置
     */
    private void validateBotProfile(BotProfile botProfile) {
        if (!botProfile.isValid()) {
            throw new BizException("机器人配置参数无效");
        }
        
        // 验证机器人名称长度
        if (botProfile.getBotName().length() > 128) {
            throw new BizException("机器人名称长度不能超过128字符");
        }
        
        // 验证模型名称长度
        if (botProfile.getModelName().length() > 128) {
            throw new BizException("模型名称长度不能超过128字符");
        }
        
        // 验证Prompt Key长度
        if (botProfile.getPromptKey().length() > 64) {
            throw new BizException("Prompt Key长度不能超过64字符");
        }
        
        // 验证QPS范围
        if (botProfile.getMaxQps() <= 0 || botProfile.getMaxQps() > 10000) {
            throw new BizException("最大QPS必须在1-10000之间");
        }
        
        // 验证温度范围
        if (botProfile.getTemperature().compareTo(BigDecimal.ZERO) < 0 || 
            botProfile.getTemperature().compareTo(BigDecimal.ONE) > 0) {
            throw new BizException("温度值必须在0.0-1.0之间");
        }
    }
} 