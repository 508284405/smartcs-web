package com.leyue.smartcs.bot.gatewayimpl;

import com.alibaba.cola.dto.PageResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyue.smartcs.bot.convertor.BotProfileConvertor;
import com.leyue.smartcs.bot.dataobject.BotProfileDO;
import com.leyue.smartcs.bot.mapper.BotProfileMapper;
import com.leyue.smartcs.domain.bot.BotProfile;
import com.leyue.smartcs.domain.bot.gateway.BotProfileGateway;
import com.leyue.smartcs.domain.bot.enums.ModelTypeEnum;
import com.leyue.smartcs.domain.bot.enums.VendorTypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 机器人配置Gateway实现
 */
@Component
@RequiredArgsConstructor
public class BotProfileGatewayImpl implements BotProfileGateway {
    
    private final BotProfileMapper botProfileMapper;
    private final BotProfileConvertor botProfileConvertor;
    
    @Override
    public Long createBotProfile(BotProfile botProfile) {
        BotProfileDO botProfileDO = botProfileConvertor.toDO(botProfile);
        botProfileMapper.insert(botProfileDO);
        return botProfileDO.getId();
    }
    
    @Override
    public boolean updateBotProfile(BotProfile botProfile) {
        BotProfileDO botProfileDO = botProfileConvertor.toDO(botProfile);
        int result = botProfileMapper.updateById(botProfileDO);
        return result > 0;
    }
    
    @Override
    public Optional<BotProfile> findById(Long botId) {
        LambdaQueryWrapper<BotProfileDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BotProfileDO::getId, botId)
               .eq(BotProfileDO::getIsDeleted, 0);
        
        BotProfileDO botProfileDO = botProfileMapper.selectOne(wrapper);
        if (botProfileDO == null) {
            return Optional.empty();
        }
        
        BotProfile botProfile = botProfileConvertor.toDomain(botProfileDO);
        return Optional.of(botProfile);
    }
    
    @Override
    public boolean deleteById(Long botId) {
        LambdaUpdateWrapper<BotProfileDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(BotProfileDO::getId, botId)
               .set(BotProfileDO::getIsDeleted, 1)
               .set(BotProfileDO::getUpdatedAt, System.currentTimeMillis());
        
        int result = botProfileMapper.update(null, wrapper);
        return result > 0;
    }
    
    @Override
    public PageResponse<BotProfile> pageQuery(int pageIndex, int pageSize, String botName, String modelName) {
        LambdaQueryWrapper<BotProfileDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BotProfileDO::getIsDeleted, 0);
        
        if (StringUtils.hasText(botName)) {
            wrapper.like(BotProfileDO::getBotName, botName);
        }
        
        if (StringUtils.hasText(modelName)) {
            wrapper.like(BotProfileDO::getModelName, modelName);
        }
        
        wrapper.orderByDesc(BotProfileDO::getCreatedAt);
        
        Page<BotProfileDO> page = new Page<>(pageIndex, pageSize);
        Page<BotProfileDO> result = botProfileMapper.selectPage(page, wrapper);
        
        List<BotProfile> botProfiles = result.getRecords().stream()
                .map(botProfileConvertor::toDomain)
                .collect(Collectors.toList());
        
        return PageResponse.of(botProfiles, (int) result.getTotal(), pageSize, pageIndex);
    }
    
    @Override
    public List<BotProfile> findAllActive() {
        LambdaQueryWrapper<BotProfileDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BotProfileDO::getIsDeleted, 0)
               .orderByDesc(BotProfileDO::getCreatedAt);
        
        List<BotProfileDO> botProfileDOs = botProfileMapper.selectList(wrapper);
        return botProfileDOs.stream()
                .map(botProfileConvertor::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<BotProfile> findByBotName(String botName) {
        LambdaQueryWrapper<BotProfileDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BotProfileDO::getBotName, botName)
               .eq(BotProfileDO::getIsDeleted, 0);
        
        BotProfileDO botProfileDO = botProfileMapper.selectOne(wrapper);
        if (botProfileDO == null) {
            return Optional.empty();
        }
        
        BotProfile botProfile = botProfileConvertor.toDomain(botProfileDO);
        return Optional.of(botProfile);
    }
    
    @Override
    public boolean existsByBotName(String botName, Long excludeBotId) {
        LambdaQueryWrapper<BotProfileDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BotProfileDO::getBotName, botName)
               .eq(BotProfileDO::getIsDeleted, 0);
        
        if (excludeBotId != null) {
            wrapper.ne(BotProfileDO::getId, excludeBotId);
        }
        
        Long count = botProfileMapper.selectCount(wrapper);
        return count > 0;
    }
    
    @Override
    public List<BotProfile> findByVendorAndModelType(VendorTypeEnum vendor, ModelTypeEnum modelType) {
        LambdaQueryWrapper<BotProfileDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BotProfileDO::getVendor, vendor.getCode())
               .eq(BotProfileDO::getModelType, modelType.getCode())
               .eq(BotProfileDO::getIsDeleted, 0)
               .orderByDesc(BotProfileDO::getCreatedAt);
        
        List<BotProfileDO> botProfileDOs = botProfileMapper.selectList(wrapper);
        return botProfileDOs.stream()
                .map(botProfileConvertor::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean enableById(Long botId) {
        LambdaUpdateWrapper<BotProfileDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(BotProfileDO::getId, botId)
               .eq(BotProfileDO::getIsDeleted, 0)
               .set(BotProfileDO::getEnabled, true)
               .set(BotProfileDO::getUpdatedAt, System.currentTimeMillis());
        
        int result = botProfileMapper.update(null, wrapper);
        return result > 0;
    }
    
    @Override
    public boolean disableById(Long botId) {
        LambdaUpdateWrapper<BotProfileDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(BotProfileDO::getId, botId)
               .eq(BotProfileDO::getIsDeleted, 0)
               .set(BotProfileDO::getEnabled, false)
               .set(BotProfileDO::getUpdatedAt, System.currentTimeMillis());
        
        int result = botProfileMapper.update(null, wrapper);
        return result > 0;
    }
} 