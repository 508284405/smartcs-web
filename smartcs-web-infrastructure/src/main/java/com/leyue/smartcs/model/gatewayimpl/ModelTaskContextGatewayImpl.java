package com.leyue.smartcs.model.gatewayimpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.leyue.smartcs.domain.model.ModelContext;
import com.leyue.smartcs.domain.model.gateway.ModelContextGateway;
import com.leyue.smartcs.model.convertor.ModelTaskContextConvertor;
import com.leyue.smartcs.model.dataobject.ModelTaskContextDO;
import com.leyue.smartcs.model.mapper.ModelTaskContextMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 模型上下文Gateway实现
 */
@Component
@RequiredArgsConstructor
public class ModelTaskContextGatewayImpl implements ModelContextGateway {
    
    private final ModelTaskContextMapper contextMapper;
    private final ModelTaskContextConvertor contextConvertor;
    
    @Override
    public ModelContext save(ModelContext context) {
        ModelTaskContextDO contextDO = contextConvertor.toDO(context);
        
        if (context.getSessionId() == null || findOptionalBySessionId(context.getSessionId()).isEmpty()) {
            // 新增
            contextDO.setCreatedAt(System.currentTimeMillis());
            contextDO.setUpdatedAt(System.currentTimeMillis());
            contextMapper.insert(contextDO);
        } else {
            // 更新
            contextDO.setUpdatedAt(System.currentTimeMillis());
            LambdaUpdateWrapper<ModelTaskContextDO> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(ModelTaskContextDO::getSessionId, context.getSessionId());
            contextMapper.update(contextDO, wrapper);
        }
        
        return context;
    }
    
    @Override
    public ModelContext findBySessionId(String sessionId) {
        Optional<ModelContext> context = findOptionalBySessionId(sessionId);
        return context.orElse(null);
    }
    
    @Override
    public Optional<ModelContext> findOptionalBySessionId(String sessionId) {
        ModelTaskContextDO contextDO = contextMapper.selectBySessionId(sessionId);
        if (contextDO == null) {
            return Optional.empty();
        }
        
        ModelContext context = contextConvertor.toDomain(contextDO);
        return Optional.of(context);
    }
    
    @Override
    public List<ModelContext> findByModelId(Long modelId) {
        List<ModelTaskContextDO> contextDOs = contextMapper.selectByModelId(modelId);
        return contextDOs.stream()
                .map(contextConvertor::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean addMessage(String sessionId, String role, String content) {
        Optional<ModelContext> contextOpt = findOptionalBySessionId(sessionId);
        if (contextOpt.isEmpty()) {
            return false;
        }
        
        ModelContext context = contextOpt.get();
        context.addMessage(role, content);
        save(context);
        return true;
    }
    
    @Override
    public boolean clearBySessionId(String sessionId) {
        Optional<ModelContext> contextOpt = findOptionalBySessionId(sessionId);
        if (contextOpt.isEmpty()) {
            return false;
        }
        
        ModelContext context = contextOpt.get();
        context.clearMessages();
        save(context);
        return true;
    }
    
    @Override
    public int clearByModelId(Long modelId) {
        List<ModelTaskContextDO> contextDOs = contextMapper.selectByModelId(modelId);
        int count = 0;
        
        for (ModelTaskContextDO contextDO : contextDOs) {
            int result = contextMapper.clearMessages(contextDO.getId());
            if (result > 0) {
                count++;
            }
        }
        
        return count;
    }
    
    @Override
    public boolean existsBySessionId(String sessionId) {
        int count = contextMapper.countBySessionId(sessionId, null);
        return count > 0;
    }
    
    @Override
    public int getMessageCount(String sessionId) {
        Optional<ModelContext> contextOpt = findOptionalBySessionId(sessionId);
        if (contextOpt.isEmpty()) {
            return 0;
        }
        
        ModelContext context = contextOpt.get();
        return context.getMessagesList().size();
    }
    
    @Override
    public int getContextLength(String sessionId) {
        Optional<ModelContext> contextOpt = findOptionalBySessionId(sessionId);
        if (contextOpt.isEmpty()) {
            return 0;
        }
        
        ModelContext context = contextOpt.get();
        return context.getCurrentLength() != null ? context.getCurrentLength() : 0;
    }
    
    @Override
    public boolean deleteBySessionId(String sessionId) {
        LambdaUpdateWrapper<ModelTaskContextDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ModelTaskContextDO::getSessionId, sessionId)
               .set(ModelTaskContextDO::getIsDeleted, 1)
               .set(ModelTaskContextDO::getUpdatedAt, System.currentTimeMillis());
        
        int result = contextMapper.update(null, wrapper);
        return result > 0;
    }
    
    @Override
    public int cleanExpiredContexts(long expiredTime) {
        List<ModelTaskContextDO> expiredContexts = contextMapper.selectByUpdatedBefore(expiredTime);
        if (expiredContexts.isEmpty()) {
            return 0;
        }
        
        List<Long> ids = expiredContexts.stream()
                .map(ModelTaskContextDO::getId)
                .collect(Collectors.toList());
        
        LambdaUpdateWrapper<ModelTaskContextDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(ModelTaskContextDO::getId, ids)
               .set(ModelTaskContextDO::getIsDeleted, 1)
               .set(ModelTaskContextDO::getUpdatedAt, System.currentTimeMillis());
        
        return contextMapper.update(null, wrapper);
    }
    
    @Override
    public long getActiveSessionCount(Long modelId) {
        LambdaQueryWrapper<ModelTaskContextDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelTaskContextDO::getIsDeleted, 0);
        
        if (modelId != null) {
            wrapper.eq(ModelTaskContextDO::getModelId, modelId);
        }
        
        return contextMapper.selectCount(wrapper);
    }
}