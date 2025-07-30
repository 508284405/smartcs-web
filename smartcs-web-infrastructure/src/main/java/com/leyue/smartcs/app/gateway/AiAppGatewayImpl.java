package com.leyue.smartcs.app.gateway;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyue.smartcs.app.convertor.AiAppConvertor;
import com.leyue.smartcs.app.dao.AiAppDO;
import com.leyue.smartcs.app.dao.AiAppMapper;
import com.leyue.smartcs.domain.app.AiApp;
import com.leyue.smartcs.domain.app.enums.AppStatus;
import com.leyue.smartcs.domain.app.enums.AppType;
import com.leyue.smartcs.domain.app.gateway.AiAppGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AI应用领域网关实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiAppGatewayImpl implements AiAppGateway {
    
    private final AiAppMapper aiAppMapper;
    
    @Override
    public AiApp create(AiApp aiApp) {
        AiAppDO aiAppDO = AiAppConvertor.INSTANCE.domainToDo(aiApp);
        aiAppMapper.insert(aiAppDO);
        return AiAppConvertor.INSTANCE.doToDomain(aiAppDO);
    }
    
    @Override
    public AiApp update(AiApp aiApp) {
        AiAppDO aiAppDO = AiAppConvertor.INSTANCE.domainToDo(aiApp);
        aiAppMapper.updateById(aiAppDO);
        return AiAppConvertor.INSTANCE.doToDomain(aiAppDO);
    }
    
    @Override
    public AiApp getById(Long id) {
        if (id == null) {
            return null;
        }
        AiAppDO aiAppDO = aiAppMapper.selectById(id);
        return aiAppDO != null ? AiAppConvertor.INSTANCE.doToDomain(aiAppDO) : null;
    }
    
    @Override
    public AiApp getByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        LambdaQueryWrapper<AiAppDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiAppDO::getCode, code);
        AiAppDO aiAppDO = aiAppMapper.selectOne(wrapper);
        return aiAppDO != null ? AiAppConvertor.INSTANCE.doToDomain(aiAppDO) : null;
    }
    
    @Override
    public boolean delete(Long id) {
        if (id == null) {
            return false;
        }
        return aiAppMapper.deleteById(id) > 0;
    }
    
    @Override
    public List<AiApp> listByPage(Long creatorId, AppType type, AppStatus status, String keyword, int offset, int limit) {
        Page<AiAppDO> page = new Page<>((offset / limit) + 1, limit);
        String typeStr = type != null ? type.name() : null;
        String statusStr = status != null ? status.name() : null;
        
        IPage<AiAppDO> result = aiAppMapper.selectAppPage(page, creatorId, typeStr, statusStr, keyword);
        
        return result.getRecords().stream()
                .map(AiAppConvertor.INSTANCE::doToDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public long count(Long creatorId, AppType type, AppStatus status, String keyword) {
        String typeStr = type != null ? type.name() : null;
        String statusStr = status != null ? status.name() : null;
        return aiAppMapper.countApps(creatorId, typeStr, statusStr, keyword);
    }
    
    @Override
    public boolean existsByCode(String code, Long excludeId) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }
        AiAppDO result = aiAppMapper.selectByCodeExcludeId(code, excludeId);
        return result != null;
    }
}