package com.leyue.smartcs.domain.intent.gateway;

import com.leyue.smartcs.domain.intent.entity.IntentRoute;

/**
 * 意图路由Gateway接口
 * 
 * @author Claude
 */
public interface IntentRouteGateway {
    
    /**
     * 保存意图路由
     * @param route 意图路由对象
     * @return 保存后的意图路由对象
     */
    IntentRoute save(IntentRoute route);
    
    /**
     * 更新意图路由
     * @param route 意图路由对象
     */
    void update(IntentRoute route);
    
    /**
     * 根据版本ID查找意图路由
     * @param versionId 版本ID
     * @return 意图路由对象
     */
    IntentRoute findByVersionId(Long versionId);
    
    /**
     * 删除意图路由
     * @param versionId 版本ID
     */
    void deleteByVersionId(Long versionId);
}