package com.leyue.smartcs.intent.convertor;

import com.leyue.smartcs.domain.intent.entity.IntentPolicy;
import com.leyue.smartcs.intent.dataobject.IntentPolicyDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 意图策略数据对象转换器
 * 
 * @author Claude
 */
@Mapper(componentModel = "spring")
public interface IntentPolicyConvertor {
    
    /**
     * DO转Domain
     * @param policyDO 数据对象
     * @return 领域对象
     */
    IntentPolicy toDomain(IntentPolicyDO policyDO);
    
    /**
     * Domain转DO
     * @param policy 领域对象
     * @return 数据对象
     */
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    IntentPolicyDO toDO(IntentPolicy policy);
    
    /**
     * DO列表转Domain列表
     * @param policyDOList 数据对象列表
     * @return 领域对象列表
     */
    List<IntentPolicy> toDomainList(List<IntentPolicyDO> policyDOList);
    
    /**
     * Domain列表转DO列表
     * @param policyList 领域对象列表
     * @return 数据对象列表
     */
    List<IntentPolicyDO> toDOList(List<IntentPolicy> policyList);
}