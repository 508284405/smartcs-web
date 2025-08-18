package com.leyue.smartcs.intent.gateway;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.leyue.smartcs.domain.intent.entity.IntentPolicy;
import com.leyue.smartcs.domain.intent.gateway.IntentPolicyGateway;
import com.leyue.smartcs.intent.convertor.IntentPolicyConvertor;
import com.leyue.smartcs.intent.dataobject.IntentPolicyDO;
import com.leyue.smartcs.intent.mapper.IntentPolicyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 意图策略Gateway实现
 * 
 * @author Claude
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IntentPolicyGatewayImpl implements IntentPolicyGateway {
    
    private final IntentPolicyMapper policyMapper;
    private final IntentPolicyConvertor policyConvertor;
    
    @Override
    @Transactional
    public IntentPolicy save(IntentPolicy policy) {
        log.debug("保存意图策略: versionId={}", policy.getVersionId());
        
        IntentPolicyDO policyDO = policyConvertor.toDO(policy);
        policyMapper.insert(policyDO);
        
        return policyConvertor.toDomain(policyDO);
    }
    
    @Override
    @Transactional
    public void update(IntentPolicy policy) {
        log.debug("更新意图策略: id={}, versionId={}", policy.getId(), policy.getVersionId());
        
        IntentPolicyDO policyDO = policyConvertor.toDO(policy);
        policyMapper.updateById(policyDO);
    }
    
    @Override
    public IntentPolicy findByVersionId(Long versionId) {
        log.debug("根据版本ID查找意图策略: versionId={}", versionId);
        
        LambdaQueryWrapper<IntentPolicyDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IntentPolicyDO::getVersionId, versionId);
        
        IntentPolicyDO policyDO = policyMapper.selectOne(wrapper);
        return policyDO != null ? policyConvertor.toDomain(policyDO) : null;
    }
    
    @Override
    @Transactional
    public void deleteByVersionId(Long versionId) {
        log.debug("根据版本ID删除意图策略: versionId={}", versionId);
        
        LambdaQueryWrapper<IntentPolicyDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IntentPolicyDO::getVersionId, versionId);
        
        policyMapper.delete(wrapper);
    }
}