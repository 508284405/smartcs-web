package com.leyue.smartcs.intent.gateway;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.leyue.smartcs.domain.intent.entity.IntentRoute;
import com.leyue.smartcs.domain.intent.gateway.IntentRouteGateway;
import com.leyue.smartcs.intent.convertor.IntentRouteConvertor;
import com.leyue.smartcs.intent.dataobject.IntentRouteDO;
import com.leyue.smartcs.intent.mapper.IntentRouteMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 意图路由Gateway实现
 * 
 * @author Claude
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IntentRouteGatewayImpl implements IntentRouteGateway {
    
    private final IntentRouteMapper routeMapper;
    private final IntentRouteConvertor routeConvertor;
    
    @Override
    @Transactional
    public IntentRoute save(IntentRoute route) {
        log.debug("保存意图路由: versionId={}, routeType={}", 
                route.getVersionId(), route.getRouteType());
        
        IntentRouteDO routeDO = routeConvertor.toDO(route);
        routeMapper.insert(routeDO);
        
        return routeConvertor.toDomain(routeDO);
    }
    
    @Override
    @Transactional
    public void update(IntentRoute route) {
        log.debug("更新意图路由: id={}, versionId={}, routeType={}", 
                route.getId(), route.getVersionId(), route.getRouteType());
        
        IntentRouteDO routeDO = routeConvertor.toDO(route);
        routeMapper.updateById(routeDO);
    }
    
    @Override
    public IntentRoute findByVersionId(Long versionId) {
        log.debug("根据版本ID查找意图路由: versionId={}", versionId);
        
        LambdaQueryWrapper<IntentRouteDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IntentRouteDO::getVersionId, versionId);
        
        IntentRouteDO routeDO = routeMapper.selectOne(wrapper);
        return routeDO != null ? routeConvertor.toDomain(routeDO) : null;
    }
    
    @Override
    @Transactional
    public void deleteByVersionId(Long versionId) {
        log.debug("根据版本ID删除意图路由: versionId={}", versionId);
        
        LambdaQueryWrapper<IntentRouteDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IntentRouteDO::getVersionId, versionId);
        
        routeMapper.delete(wrapper);
    }
}