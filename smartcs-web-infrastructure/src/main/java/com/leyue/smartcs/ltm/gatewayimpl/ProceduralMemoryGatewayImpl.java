package com.leyue.smartcs.ltm.gatewayimpl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.leyue.smartcs.domain.ltm.entity.ProceduralMemory;
import com.leyue.smartcs.domain.ltm.gateway.ProceduralMemoryGateway;
import com.leyue.smartcs.ltm.dataobject.ProceduralMemoryDO;
import com.leyue.smartcs.ltm.mapper.ProceduralMemoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ProceduralMemoryGatewayImpl implements ProceduralMemoryGateway {

    private final ProceduralMemoryMapper mapper;

    private ProceduralMemoryDO toDO(ProceduralMemory e){
        ProceduralMemoryDO d = new ProceduralMemoryDO();
        d.setId(e.getId());
        d.setUserId(e.getUserId());
        d.setPatternType(e.getPatternType());
        d.setPatternName(e.getPatternName());
        d.setPatternDescription(e.getPatternDescription());
        d.setTriggerConditionsJson(e.getTriggerConditions()!=null? JSON.toJSONString(e.getTriggerConditions()):null);
        d.setActionTemplate(e.getActionTemplate());
        d.setSuccessCount(e.getSuccessCount());
        d.setFailureCount(e.getFailureCount());
        d.setSuccessRate(e.getSuccessRate());
        d.setLastTriggeredAt(e.getLastTriggeredAt());
        d.setLearningRate(e.getLearningRate());
        d.setIsActive(e.getIsActive());
        d.setCreatedAt(e.getCreatedAt());
        d.setUpdatedAt(e.getUpdatedAt());
        return d;
    }

    @SuppressWarnings("unchecked")
    private ProceduralMemory toEntity(ProceduralMemoryDO d){
        if (d==null) return null;
        Map<String,Object> triggers = null;
        if (d.getTriggerConditionsJson()!=null && !d.getTriggerConditionsJson().isBlank()){
            try { triggers = JSON.parseObject(d.getTriggerConditionsJson(), Map.class); } catch (Exception ignore) {}
        }
        return ProceduralMemory.builder()
                .id(d.getId())
                .userId(d.getUserId())
                .patternType(d.getPatternType())
                .patternName(d.getPatternName())
                .patternDescription(d.getPatternDescription())
                .triggerConditions(triggers)
                .actionTemplate(d.getActionTemplate())
                .successCount(d.getSuccessCount())
                .failureCount(d.getFailureCount())
                .successRate(d.getSuccessRate())
                .lastTriggeredAt(d.getLastTriggeredAt())
                .learningRate(d.getLearningRate())
                .isActive(d.getIsActive())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }

    @Override
    public void save(ProceduralMemory proceduralMemory) { mapper.insert(toDO(proceduralMemory)); }

    @Override
    public void batchSave(List<ProceduralMemory> memories) { if (memories!=null) memories.forEach(m -> mapper.insert(toDO(m))); }

    @Override
    public Optional<ProceduralMemory> findById(Long id) { return Optional.ofNullable(toEntity(mapper.selectById(id))); }

    @Override
    public Optional<ProceduralMemory> findByUserIdAndPatternTypeAndName(Long userId, String patternType, String patternName) {
        ProceduralMemoryDO d = mapper.selectOne(new LambdaQueryWrapper<ProceduralMemoryDO>()
                .eq(ProceduralMemoryDO::getUserId, userId)
                .eq(ProceduralMemoryDO::getPatternType, patternType)
                .eq(ProceduralMemoryDO::getPatternName, patternName)
                .last("limit 1"));
        return Optional.ofNullable(toEntity(d));
    }

    @Override
    public List<ProceduralMemory> findByUserId(Long userId, int page, int size) {
        return mapper.selectList(new LambdaQueryWrapper<ProceduralMemoryDO>()
                        .eq(ProceduralMemoryDO::getUserId, userId)
                        .orderByDesc(ProceduralMemoryDO::getLastTriggeredAt)
                        .last("limit "+size+" offset "+(page*size)))
                .stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<ProceduralMemory> findByUserIdAndPatternType(Long userId, String patternType) {
        return mapper.selectList(new LambdaQueryWrapper<ProceduralMemoryDO>()
                        .eq(ProceduralMemoryDO::getUserId, userId)
                        .eq(ProceduralMemoryDO::getPatternType, patternType))
                .stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<ProceduralMemory> findActiveMemories(Long userId) {
        return mapper.selectList(new LambdaQueryWrapper<ProceduralMemoryDO>()
                        .eq(ProceduralMemoryDO::getUserId, userId)
                        .eq(ProceduralMemoryDO::getIsActive, true))
                .stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<ProceduralMemory> findHighSuccessRateMemories(Long userId, Double minSuccessRate, int limit) {
        return mapper.selectList(new LambdaQueryWrapper<ProceduralMemoryDO>()
                        .eq(ProceduralMemoryDO::getUserId, userId)
                        .ge(ProceduralMemoryDO::getSuccessRate, minSuccessRate)
                        .orderByDesc(ProceduralMemoryDO::getSuccessRate)
                        .last("limit "+limit))
                .stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<ProceduralMemory> findMemoriesNeedingAdjustment(Long userId, int limit) {
        return mapper.selectList(new LambdaQueryWrapper<ProceduralMemoryDO>()
                        .eq(ProceduralMemoryDO::getUserId, userId)
                        .lt(ProceduralMemoryDO::getSuccessRate, 0.5)
                        .orderByAsc(ProceduralMemoryDO::getSuccessRate)
                        .last("limit "+limit))
                .stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<ProceduralMemory> findRecentlyTriggered(Long userId, int limit) {
        return mapper.selectList(new LambdaQueryWrapper<ProceduralMemoryDO>()
                        .eq(ProceduralMemoryDO::getUserId, userId)
                        .orderByDesc(ProceduralMemoryDO::getLastTriggeredAt)
                        .last("limit "+limit))
                .stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<ProceduralMemory> findMatchingMemories(Long userId, Map<String, Object> context) {
        // 简化：返回活跃的程序性记忆
        return findActiveMemories(userId);
    }

    @Override
    public List<ProceduralMemory> findPreferenceMemories(Long userId) {
        return findByUserIdAndPatternType(userId, ProceduralMemory.PatternType.PREFERENCE);
    }

    @Override
    public List<ProceduralMemory> findResponseStyleMemories(Long userId) {
        return findByUserIdAndPatternType(userId, ProceduralMemory.PatternType.RESPONSE_STYLE);
    }

    @Override
    public List<ProceduralMemory> findHabitMemories(Long userId) {
        return findByUserIdAndPatternType(userId, ProceduralMemory.PatternType.HABIT);
    }

    @Override
    public List<ProceduralMemory> findRuleMemories(Long userId) {
        return findByUserIdAndPatternType(userId, ProceduralMemory.PatternType.RULE);
    }

    @Override
    public void update(ProceduralMemory proceduralMemory) { mapper.updateById(toDO(proceduralMemory)); }

    @Override
    public void batchUpdateActiveStatus(List<Long> ids, Boolean isActive) {
        if (ids==null || ids.isEmpty()) return;
        mapper.update(null, new LambdaUpdateWrapper<ProceduralMemoryDO>()
                .set(ProceduralMemoryDO::getIsActive, isActive)
                .in(ProceduralMemoryDO::getId, ids));
    }

    @Override
    public void batchUpdateSuccessRate(List<Long> ids) {
        // 简化：略，保留占位
        log.debug("batchUpdateSuccessRate not implemented precisely, ids={}", ids);
    }

    @Override
    public void deleteById(Long id) { mapper.deleteById(id); }

    @Override
    public void deleteByUserId(Long userId) {
        mapper.delete(new LambdaQueryWrapper<ProceduralMemoryDO>().eq(ProceduralMemoryDO::getUserId, userId));
    }

    @Override
    public void deleteLowSuccessRateMemories(Long userId, Double threshold) {
        mapper.delete(new LambdaQueryWrapper<ProceduralMemoryDO>()
                .eq(ProceduralMemoryDO::getUserId, userId)
                .lt(ProceduralMemoryDO::getSuccessRate, threshold));
    }

    @Override
    public void deleteInactiveMemories(Long userId) {
        mapper.delete(new LambdaQueryWrapper<ProceduralMemoryDO>()
                .eq(ProceduralMemoryDO::getUserId, userId)
                .eq(ProceduralMemoryDO::getIsActive, false));
    }

    @Override
    public Long countByUserId(Long userId) {
        return mapper.selectCount(new LambdaQueryWrapper<ProceduralMemoryDO>().eq(ProceduralMemoryDO::getUserId, userId));
    }

    @Override
    public Long countActiveMemories(Long userId) {
        return mapper.selectCount(new LambdaQueryWrapper<ProceduralMemoryDO>()
                .eq(ProceduralMemoryDO::getUserId, userId)
                .eq(ProceduralMemoryDO::getIsActive, true));
    }

    @Override
    public Map<String, Long> countByPatternType(Long userId) {
        Map<String, Long> res = new HashMap<>();
        for (String t: List.of(ProceduralMemory.PatternType.PREFERENCE, ProceduralMemory.PatternType.RULE, ProceduralMemory.PatternType.HABIT, ProceduralMemory.PatternType.RESPONSE_STYLE)){
            Long c = mapper.selectCount(new LambdaQueryWrapper<ProceduralMemoryDO>()
                    .eq(ProceduralMemoryDO::getUserId, userId)
                    .eq(ProceduralMemoryDO::getPatternType, t));
            res.put(t, c);
        }
        return res;
    }

    @Override
    public Double getAverageSuccessRate(Long userId) {
        List<ProceduralMemoryDO> list = mapper.selectList(new LambdaQueryWrapper<ProceduralMemoryDO>()
                .eq(ProceduralMemoryDO::getUserId, userId)
                .select(ProceduralMemoryDO::getSuccessRate)
                .last("limit 1000"));
        if (list.isEmpty()) return 0.0;
        return list.stream().map(ProceduralMemoryDO::getSuccessRate).filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    @Override
    public List<ProceduralMemory> findTopSuccessMemories(Long userId, int limit) {
        return mapper.selectList(new LambdaQueryWrapper<ProceduralMemoryDO>()
                        .eq(ProceduralMemoryDO::getUserId, userId)
                        .orderByDesc(ProceduralMemoryDO::getSuccessRate)
                        .last("limit "+limit))
                .stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<ProceduralMemory> findMostTriggeredMemories(Long userId, int limit) {
        return mapper.selectList(new LambdaQueryWrapper<ProceduralMemoryDO>()
                        .eq(ProceduralMemoryDO::getUserId, userId)
                        .orderByDesc(ProceduralMemoryDO::getLastTriggeredAt)
                        .last("limit "+limit))
                .stream().map(this::toEntity).collect(Collectors.toList());
    }
}

