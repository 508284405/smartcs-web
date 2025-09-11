package com.leyue.smartcs.intent.gateway;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.leyue.smartcs.domain.intent.entity.IntentVersion;
import com.leyue.smartcs.domain.intent.enums.VersionStatus;
import com.leyue.smartcs.domain.intent.gateway.IntentVersionGateway;
import com.leyue.smartcs.intent.convertor.IntentVersionConvertor;
import com.leyue.smartcs.intent.dataobject.IntentVersionDO;
import com.leyue.smartcs.intent.mapper.IntentVersionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 意图版本Gateway实现
 * 
 * @author Claude
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IntentVersionGatewayImpl implements IntentVersionGateway {
    
    private final IntentVersionMapper versionMapper;
    private final IntentVersionConvertor versionConvertor;
    
    @Override
    public IntentVersion save(IntentVersion version) {
        IntentVersionDO versionDO = versionConvertor.toDO(version);
        versionMapper.insert(versionDO);
        return versionConvertor.toDomain(versionDO);
    }
    
    @Override
    public void update(IntentVersion version) {
        versionMapper.updateById(versionConvertor.toDO(version));
    }
    
    @Override
    public IntentVersion findById(Long id) {
        IntentVersionDO versionDO = versionMapper.selectById(id);
        return versionDO != null ? versionConvertor.toDomain(versionDO) : null;
    }
    
    @Override
    public List<IntentVersion> findByIntentId(Long intentId) {
        LambdaQueryWrapper<IntentVersionDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IntentVersionDO::getIntentId, intentId)
                .eq(IntentVersionDO::getIsDeleted, 0)
                .orderByDesc(IntentVersionDO::getCreatedAt);
        List<IntentVersionDO> versionDOList = versionMapper.selectList(wrapper);
        return versionConvertor.toDomainList(versionDOList);
    }
    
    @Override
    public IntentVersion findByIntentIdAndVersionNumber(Long intentId, String versionNumber) {
        LambdaQueryWrapper<IntentVersionDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IntentVersionDO::getIntentId, intentId)
                .eq(IntentVersionDO::getVersionNumber, versionNumber)
                .eq(IntentVersionDO::getIsDeleted, 0);
        IntentVersionDO versionDO = versionMapper.selectOne(wrapper);
        return versionDO != null ? versionConvertor.toDomain(versionDO) : null;
    }
    
    @Override
    public IntentVersion findActiveVersionByIntentId(Long intentId) {
        LambdaQueryWrapper<IntentVersionDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IntentVersionDO::getIntentId, intentId)
                .eq(IntentVersionDO::getStatus, VersionStatus.ACTIVE.getCode())
                .eq(IntentVersionDO::getIsDeleted, 0);
        IntentVersionDO versionDO = versionMapper.selectOne(wrapper);
        return versionDO != null ? versionConvertor.toDomain(versionDO) : null;
    }
    
    @Override
    public List<IntentVersion> findByStatus(VersionStatus status) {
        LambdaQueryWrapper<IntentVersionDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IntentVersionDO::getStatus, status.getCode())
                .eq(IntentVersionDO::getIsDeleted, 0)
                .orderByDesc(IntentVersionDO::getUpdatedAt);
        List<IntentVersionDO> versionDOList = versionMapper.selectList(wrapper);
        return versionConvertor.toDomainList(versionDOList);
    }
    
    @Override
    public List<IntentVersion> findByIntentIdAndStatus(Long intentId, VersionStatus status) {
        LambdaQueryWrapper<IntentVersionDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IntentVersionDO::getIntentId, intentId)
                .eq(IntentVersionDO::getStatus, status.getCode())
                .eq(IntentVersionDO::getIsDeleted, 0)
                .orderByDesc(IntentVersionDO::getUpdatedAt);
        List<IntentVersionDO> versionDOList = versionMapper.selectList(wrapper);
        return versionConvertor.toDomainList(versionDOList);
    }
    
    @Override
    public String getNextVersionNumber(Long intentId) {
        return versionMapper.getNextVersionNumber(intentId);
    }
    
    @Override
    public void deleteById(Long id) {
        versionMapper.deleteById(id);
    }
}