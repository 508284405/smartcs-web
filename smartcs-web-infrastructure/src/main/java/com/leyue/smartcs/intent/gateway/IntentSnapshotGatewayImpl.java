package com.leyue.smartcs.intent.gateway;

import com.alibaba.cola.dto.PageResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyue.smartcs.domain.intent.entity.IntentSnapshot;
import com.leyue.smartcs.domain.intent.enums.SnapshotStatus;
import com.leyue.smartcs.domain.intent.gateway.IntentSnapshotGateway;
import com.leyue.smartcs.intent.convertor.IntentSnapshotConvertor;
import com.leyue.smartcs.intent.dataobject.IntentSnapshotDO;
import com.leyue.smartcs.intent.mapper.IntentSnapshotMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 意图快照Gateway实现
 * 
 * @author Claude
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IntentSnapshotGatewayImpl implements IntentSnapshotGateway {
    
    private final IntentSnapshotMapper intentSnapshotMapper;
    private final IntentSnapshotConvertor intentSnapshotConvertor;
    
    @Override
    public IntentSnapshot save(IntentSnapshot snapshot) {
        log.debug("保存意图快照: {}", snapshot.getName());
        
        IntentSnapshotDO snapshotDO = intentSnapshotConvertor.toDataObject(snapshot);
        intentSnapshotMapper.insert(snapshotDO);
        
        return intentSnapshotConvertor.toEntity(snapshotDO);
    }
    
    @Override
    public void update(IntentSnapshot snapshot) {
        log.debug("更新意图快照: {}", snapshot.getId());
        
        IntentSnapshotDO snapshotDO = intentSnapshotConvertor.toDataObject(snapshot);
        intentSnapshotMapper.updateById(snapshotDO);
    }
    
    @Override
    public IntentSnapshot findById(Long id) {
        log.debug("根据ID查找意图快照: {}", id);
        
        IntentSnapshotDO snapshotDO = intentSnapshotMapper.selectById(id);
        return snapshotDO != null ? intentSnapshotConvertor.toEntity(snapshotDO) : null;
    }
    
    @Override
    public IntentSnapshot findByCode(String code) {
        log.debug("根据编码查找意图快照: {}", code);
        
        LambdaQueryWrapper<IntentSnapshotDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IntentSnapshotDO::getCode, code)
               .eq(IntentSnapshotDO::getIsDeleted, 0);
        
        IntentSnapshotDO snapshotDO = intentSnapshotMapper.selectOne(wrapper);
        return snapshotDO != null ? intentSnapshotConvertor.toEntity(snapshotDO) : null;
    }
    
    @Override
    public IntentSnapshot getCurrentActiveSnapshot() {
        log.debug("获取当前激活的快照");
        
        LambdaQueryWrapper<IntentSnapshotDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IntentSnapshotDO::getStatus, SnapshotStatus.ACTIVE.getCode())
               .eq(IntentSnapshotDO::getIsDeleted, 0)
               .orderByDesc(IntentSnapshotDO::getPublishedAt)
               .last("LIMIT 1");
        
        IntentSnapshotDO snapshotDO = intentSnapshotMapper.selectOne(wrapper);
        return snapshotDO != null ? intentSnapshotConvertor.toEntity(snapshotDO) : null;
    }
    
    @Override
    public List<IntentSnapshot> findByStatus(SnapshotStatus status) {
        log.debug("根据状态查找快照列表: {}", status);
        
        LambdaQueryWrapper<IntentSnapshotDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IntentSnapshotDO::getStatus, status.getCode())
               .eq(IntentSnapshotDO::getIsDeleted, 0)
               .orderByDesc(IntentSnapshotDO::getCreatedAt);
        
        List<IntentSnapshotDO> snapshotDOList = intentSnapshotMapper.selectList(wrapper);
        return snapshotDOList.stream()
                .map(intentSnapshotConvertor::toEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public PageResponse<IntentSnapshot> findByPage(SnapshotStatus status, String keyword, int pageNum, int pageSize) {
        log.debug("分页查询意图快照: status={}, keyword={}, pageNum={}, pageSize={}", 
                status, keyword, pageNum, pageSize);
        
        Page<IntentSnapshotDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<IntentSnapshotDO> wrapper = new LambdaQueryWrapper<>();
        
        // 状态过滤
        if (status != null) {
            wrapper.eq(IntentSnapshotDO::getStatus, status.getCode());
        }
        
        // 关键词过滤
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(IntentSnapshotDO::getName, keyword)
                           .or().like(IntentSnapshotDO::getCode, keyword)
                           .or().like(IntentSnapshotDO::getName, keyword));
        }
        
        wrapper.eq(IntentSnapshotDO::getIsDeleted, 0)
               .orderByDesc(IntentSnapshotDO::getCreatedAt);
        
        IPage<IntentSnapshotDO> pageResult = intentSnapshotMapper.selectPage(page, wrapper);
        
        // 转换为响应对象
        List<IntentSnapshot> snapshots = pageResult.getRecords().stream()
                .map(intentSnapshotConvertor::toEntity)
                .collect(Collectors.toList());
        
        return PageResponse.of(snapshots, (int) pageResult.getTotal(), pageNum, pageSize);
    }
    
    @Override
    public void deleteById(Long id) {
        log.debug("删除意图快照: {}", id);
        
        // 逻辑删除
        IntentSnapshotDO snapshotDO = new IntentSnapshotDO();
        snapshotDO.setId(id);
        snapshotDO.setIsDeleted(1);
        snapshotDO.setUpdatedAt(System.currentTimeMillis());
        
        intentSnapshotMapper.updateById(snapshotDO);
    }
    
    /**
     * 根据渠道和租户获取激活快照
     * 
     * @param channel 渠道
     * @param tenant 租户
     * @return 激活快照
     */
    public IntentSnapshot getActiveSnapshotByScope(String channel, String tenant) {
        log.debug("根据作用域获取激活快照: channel={}, tenant={}", channel, tenant);
        
        LambdaQueryWrapper<IntentSnapshotDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IntentSnapshotDO::getStatus, SnapshotStatus.ACTIVE.getCode())
               .eq(IntentSnapshotDO::getIsDeleted, 0);
        
        // 添加作用域过滤条件
        if (StringUtils.hasText(channel)) {
            wrapper.and(w -> w.like(IntentSnapshotDO::getScopeSelector, "\"channel\":\"" + channel + "\"")
                           .or().isNull(IntentSnapshotDO::getScopeSelector)
                           .or().eq(IntentSnapshotDO::getScopeSelector, ""));
        }
        
        if (StringUtils.hasText(tenant)) {
            wrapper.and(w -> w.like(IntentSnapshotDO::getScopeSelector, "\"tenant\":\"" + tenant + "\"")
                           .or().isNull(IntentSnapshotDO::getScopeSelector)
                           .or().eq(IntentSnapshotDO::getScopeSelector, ""));
        }
        
        wrapper.orderByDesc(IntentSnapshotDO::getPublishedAt)
               .last("LIMIT 1");
        
        IntentSnapshotDO snapshotDO = intentSnapshotMapper.selectOne(wrapper);
        return snapshotDO != null ? intentSnapshotConvertor.toEntity(snapshotDO) : null;
    }
    
    /**
     * 批量更新快照状态
     * 
     * @param ids 快照ID列表
     * @param status 目标状态
     */
    public void batchUpdateStatus(List<Long> ids, SnapshotStatus status) {
        log.debug("批量更新快照状态: ids={}, status={}", ids, status);
        
        if (ids == null || ids.isEmpty()) {
            return;
        }
        
        LambdaQueryWrapper<IntentSnapshotDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(IntentSnapshotDO::getId, ids);
        
        IntentSnapshotDO updateDO = new IntentSnapshotDO();
        updateDO.setStatus(status.getCode());
        updateDO.setUpdatedAt(System.currentTimeMillis());
        
        intentSnapshotMapper.update(updateDO, wrapper);
    }
}