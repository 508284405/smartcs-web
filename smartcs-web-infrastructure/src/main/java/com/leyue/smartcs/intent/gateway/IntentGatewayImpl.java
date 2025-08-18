package com.leyue.smartcs.intent.gateway;

import com.alibaba.cola.dto.PageResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyue.smartcs.domain.intent.entity.Intent;
import com.leyue.smartcs.domain.intent.enums.IntentStatus;
import com.leyue.smartcs.domain.intent.gateway.IntentGateway;
import com.leyue.smartcs.intent.convertor.IntentConvertor;
import com.leyue.smartcs.intent.dataobject.IntentDO;
import com.leyue.smartcs.intent.mapper.IntentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 意图Gateway实现
 * 
 * @author Claude
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IntentGatewayImpl implements IntentGateway {
    
    private final IntentMapper intentMapper;
    private final IntentConvertor intentConvertor;
    
    @Override
    public Intent save(Intent intent) {
        IntentDO intentDO = intentConvertor.toDO(intent);
        intentMapper.insert(intentDO);
        return intentConvertor.toDomain(intentDO);
    }
    
    @Override
    public void update(Intent intent) {
        intentMapper.updateById(intentConvertor.toDO(intent));
    }
    
    @Override
    public Intent findById(Long id) {
        IntentDO intentDO = intentMapper.selectById(id);
        return intentDO != null ? intentConvertor.toDomain(intentDO) : null;
    }
    
    @Override
    public Intent findByCode(String code) {
        LambdaQueryWrapper<IntentDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IntentDO::getCode, code);
        IntentDO intentDO = intentMapper.selectOne(wrapper);
        return intentDO != null ? intentConvertor.toDomain(intentDO) : null;
    }
    
    @Override
    public List<Intent> findByCatalogId(Long catalogId) {
        LambdaQueryWrapper<IntentDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IntentDO::getCatalogId, catalogId)
                .eq(IntentDO::getIsDeleted, 0)
                .orderByDesc(IntentDO::getUpdatedAt);
        List<IntentDO> intentDOList = intentMapper.selectList(wrapper);
        return intentConvertor.toDomainList(intentDOList);
    }
    
    @Override
    public List<Intent> findByStatus(IntentStatus status) {
        LambdaQueryWrapper<IntentDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IntentDO::getStatus, status.getCode())
                .eq(IntentDO::getIsDeleted, 0)
                .orderByDesc(IntentDO::getUpdatedAt);
        List<IntentDO> intentDOList = intentMapper.selectList(wrapper);
        return intentConvertor.toDomainList(intentDOList);
    }
    
    @Override
    public PageResponse<Intent> findByPage(Long catalogId, IntentStatus status, String keyword, int pageNum, int pageSize) {
        log.info("开始执行意图分页查询 - 目录ID: {}, 状态: {}, 关键词: {}, 页码: {}, 页大小: {}", 
                catalogId, status, keyword, pageNum, pageSize);
        
        LambdaQueryWrapper<IntentDO> wrapper = new LambdaQueryWrapper<>();
        
        if (catalogId != null) {
            wrapper.eq(IntentDO::getCatalogId, catalogId);
            log.debug("添加目录ID过滤条件: {}", catalogId);
        }
        if (status != null) {
            wrapper.eq(IntentDO::getStatus, status.getCode());
            log.debug("添加状态过滤条件: {}", status.getCode());
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(IntentDO::getName, keyword)
                    .or().like(IntentDO::getDescription, keyword)
                    .or().like(IntentDO::getCode, keyword));
            log.debug("添加关键词过滤条件: {}", keyword);
        }
        
        wrapper.eq(IntentDO::getIsDeleted, 0)
                .orderByDesc(IntentDO::getUpdatedAt);
        
        log.debug("构建查询条件完成，执行分页查询...");
        
        Page<IntentDO> page = new Page<>(pageNum, pageSize);
        Page<IntentDO> result = intentMapper.selectPage(page, wrapper);
        
        log.info("数据库查询完成 - 总记录数: {}, 当前页记录数: {}, 页码: {}, 页大小: {}", 
                result.getTotal(), result.getRecords().size(), result.getCurrent(), result.getSize());
        
        List<Intent> intentList = intentConvertor.toDomainList(result.getRecords());
        
        PageResponse<Intent> response = PageResponse.of(intentList, (int) result.getTotal(), pageSize, pageNum);
        log.debug("转换为领域对象完成，返回分页响应");
        
        return response;
    }
    
    @Override
    public void deleteById(Long id) {
        intentMapper.deleteById(id);
    }
}