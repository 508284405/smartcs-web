package com.leyue.smartcs.intent.gateway;

import com.alibaba.cola.dto.PageResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyue.smartcs.domain.intent.entity.IntentSample;
import com.leyue.smartcs.domain.intent.enums.SampleType;
import com.leyue.smartcs.domain.intent.gateway.IntentSampleGateway;
import com.leyue.smartcs.intent.convertor.IntentSampleConvertor;
import com.leyue.smartcs.intent.dataobject.IntentSampleDO;
import com.leyue.smartcs.intent.mapper.IntentSampleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 意图样本Gateway实现
 * 
 * @author Claude
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IntentSampleGatewayImpl implements IntentSampleGateway {
    
    private final IntentSampleMapper sampleMapper;
    private final IntentSampleConvertor sampleConvertor;
    
    @Override
    @Transactional
    public IntentSample save(IntentSample sample) {
        log.debug("保存意图样本: versionId={}, type={}", 
                sample.getVersionId(), sample.getType());
        
        IntentSampleDO sampleDO = sampleConvertor.toDO(sample);
        sampleMapper.insert(sampleDO);
        
        return sampleConvertor.toDomain(sampleDO);
    }
    
    @Override
    @Transactional
    public void saveBatch(List<IntentSample> samples) {
        log.debug("批量保存意图样本: count={}", samples.size());
        
        List<IntentSampleDO> sampleDOList = sampleConvertor.toDOList(samples);
        
        // 使用MyBatis-Plus的批量插入功能
        for (IntentSampleDO sampleDO : sampleDOList) {
            sampleMapper.insert(sampleDO);
        }
    }
    
    @Override
    @Transactional
    public void update(IntentSample sample) {
        log.debug("更新意图样本: id={}, versionId={}", 
                sample.getId(), sample.getVersionId());
        
        IntentSampleDO sampleDO = sampleConvertor.toDO(sample);
        sampleMapper.updateById(sampleDO);
    }
    
    @Override
    public IntentSample findById(Long id) {
        log.debug("根据ID查找意图样本: id={}", id);
        
        IntentSampleDO sampleDO = sampleMapper.selectById(id);
        return sampleDO != null ? sampleConvertor.toDomain(sampleDO) : null;
    }
    
    @Override
    public List<IntentSample> findByVersionId(Long versionId) {
        log.debug("根据版本ID查找样本列表: versionId={}", versionId);
        
        LambdaQueryWrapper<IntentSampleDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IntentSampleDO::getVersionId, versionId)
                .eq(IntentSampleDO::getIsDeleted, 0)
                .orderByDesc(IntentSampleDO::getCreatedAt);
        
        List<IntentSampleDO> sampleDOList = sampleMapper.selectList(wrapper);
        return sampleConvertor.toDomainList(sampleDOList);
    }
    
    @Override
    public List<IntentSample> findByVersionIdAndType(Long versionId, SampleType type) {
        log.debug("根据版本ID和类型查找样本列表: versionId={}, type={}", versionId, type);
        
        LambdaQueryWrapper<IntentSampleDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IntentSampleDO::getVersionId, versionId)
                .eq(IntentSampleDO::getType, type.getCode())
                .eq(IntentSampleDO::getIsDeleted, 0)
                .orderByDesc(IntentSampleDO::getCreatedAt);
        
        List<IntentSampleDO> sampleDOList = sampleMapper.selectList(wrapper);
        return sampleConvertor.toDomainList(sampleDOList);
    }
    
    @Override
    public PageResponse<IntentSample> findByPage(Long versionId, SampleType type, String keyword, 
                                               int pageNum, int pageSize) {
        log.debug("分页查询意图样本: versionId={}, type={}, keyword={}, pageNum={}, pageSize={}", 
                versionId, type, keyword, pageNum, pageSize);
        
        LambdaQueryWrapper<IntentSampleDO> wrapper = new LambdaQueryWrapper<>();
        
        if (versionId != null) {
            wrapper.eq(IntentSampleDO::getVersionId, versionId);
        }
        if (type != null) {
            wrapper.eq(IntentSampleDO::getType, type.getCode());
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(IntentSampleDO::getText, keyword);
        }
        
        wrapper.eq(IntentSampleDO::getIsDeleted, 0)
                .orderByDesc(IntentSampleDO::getCreatedAt);
        
        Page<IntentSampleDO> page = new Page<>(pageNum, pageSize);
        Page<IntentSampleDO> result = sampleMapper.selectPage(page, wrapper);
        
        List<IntentSample> sampleList = sampleConvertor.toDomainList(result.getRecords());
        
        return PageResponse.of(sampleList, (int) result.getTotal(), pageSize, pageNum);
    }
    
    @Override
    public int countByVersionId(Long versionId) {
        log.debug("统计版本样本数量: versionId={}", versionId);
        
        LambdaQueryWrapper<IntentSampleDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IntentSampleDO::getVersionId, versionId)
                .eq(IntentSampleDO::getIsDeleted, 0);
        
        return Math.toIntExact(sampleMapper.selectCount(wrapper));
    }
    
    @Override
    public int countByVersionIdAndType(Long versionId, SampleType type) {
        log.debug("统计版本指定类型样本数量: versionId={}, type={}", versionId, type);
        
        LambdaQueryWrapper<IntentSampleDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IntentSampleDO::getVersionId, versionId)
                .eq(IntentSampleDO::getType, type.getCode())
                .eq(IntentSampleDO::getIsDeleted, 0);
        
        return Math.toIntExact(sampleMapper.selectCount(wrapper));
    }
    
    @Override
    @Transactional
    public void deleteById(Long id) {
        log.debug("删除意图样本: id={}", id);
        
        // 逻辑删除
        IntentSampleDO sampleDO = new IntentSampleDO();
        sampleDO.setId(id);
        sampleDO.setIsDeleted(1);
        sampleDO.setUpdatedAt(System.currentTimeMillis());
        
        sampleMapper.updateById(sampleDO);
    }
    
    @Override
    @Transactional
    public void deleteByVersionId(Long versionId) {
        log.debug("根据版本ID删除所有样本: versionId={}", versionId);
        
        LambdaQueryWrapper<IntentSampleDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IntentSampleDO::getVersionId, versionId);
        
        IntentSampleDO updateDO = new IntentSampleDO();
        updateDO.setIsDeleted(1);
        updateDO.setUpdatedAt(System.currentTimeMillis());
        
        sampleMapper.update(updateDO, wrapper);
    }
}