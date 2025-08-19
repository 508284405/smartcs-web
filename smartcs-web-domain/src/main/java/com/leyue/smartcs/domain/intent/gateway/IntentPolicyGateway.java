package com.leyue.smartcs.domain.intent.gateway;

import com.leyue.smartcs.domain.intent.entity.IntentPolicy;

/**
 * 意图策略Gateway接口
 * 
 * @author Claude
 */
public interface IntentPolicyGateway {
    
    /**
     * 保存意图策略
     * @param policy 意图策略对象
     * @return 保存后的意图策略对象
     */
    IntentPolicy save(IntentPolicy policy);
    
    /**
     * 更新意图策略
     * @param policy 意图策略对象
     */
    void update(IntentPolicy policy);
    
    /**
     * 根据版本ID查找意图策略
     * @param versionId 版本ID
     * @return 意图策略对象
     */
    IntentPolicy findByVersionId(Long versionId);
    
    /**
     * 删除意图策略
     * @param versionId 版本ID
     */
    void deleteByVersionId(Long versionId);
}