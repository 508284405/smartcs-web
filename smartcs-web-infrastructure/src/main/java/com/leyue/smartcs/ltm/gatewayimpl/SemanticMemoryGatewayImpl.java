package com.leyue.smartcs.ltm.gatewayimpl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.leyue.smartcs.domain.ltm.entity.SemanticMemory;
import com.leyue.smartcs.domain.ltm.gateway.SemanticMemoryGateway;
import com.leyue.smartcs.ltm.dataobject.SemanticMemoryDO;
import com.leyue.smartcs.ltm.mapper.SemanticMemoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class SemanticMemoryGatewayImpl implements SemanticMemoryGateway {

    private final SemanticMemoryMapper mapper;

    private SemanticMemoryDO toDO(SemanticMemory e){
        SemanticMemoryDO d = new SemanticMemoryDO();
        d.setId(e.getId());
        d.setUserId(e.getUserId());
        d.setConcept(e.getConcept());
        d.setKnowledge(e.getKnowledge());
        d.setEmbeddingVector(e.getEmbeddingVector());
        d.setConfidence(e.getConfidence());
        d.setSourceEpisodesJson(e.getSourceEpisodes()!=null? JSON.toJSONString(e.getSourceEpisodes()):null);
        d.setEvidenceCount(e.getEvidenceCount());
        d.setContradictionCount(e.getContradictionCount());
        d.setLastReinforcedAt(e.getLastReinforcedAt());
        d.setDecayRate(e.getDecayRate());
        d.setCreatedAt(e.getCreatedAt());
        d.setUpdatedAt(e.getUpdatedAt());
        return d;
    }

    @SuppressWarnings("unchecked")
    private SemanticMemory toEntity(SemanticMemoryDO d){
        if (d==null) return null;
        List<String> src = null;
        if (d.getSourceEpisodesJson()!=null && !d.getSourceEpisodesJson().isBlank()){
            try { src = JSON.parseArray(d.getSourceEpisodesJson(), String.class); } catch (Exception ignore) {}
        }
        return SemanticMemory.builder()
                .id(d.getId())
                .userId(d.getUserId())
                .concept(d.getConcept())
                .knowledge(d.getKnowledge())
                .embeddingVector(d.getEmbeddingVector())
                .confidence(d.getConfidence())
                .sourceEpisodes(src)
                .evidenceCount(d.getEvidenceCount())
                .contradictionCount(d.getContradictionCount())
                .lastReinforcedAt(d.getLastReinforcedAt())
                .decayRate(d.getDecayRate())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }

    @Override
    public void save(SemanticMemory semanticMemory) {
        mapper.insert(toDO(semanticMemory));
    }

    @Override
    public void batchSave(List<SemanticMemory> memories) {
        if (memories==null || memories.isEmpty()) return;
        for (SemanticMemory m: memories){ mapper.insert(toDO(m)); }
    }

    @Override
    public Optional<SemanticMemory> findById(Long id) {
        return Optional.ofNullable(toEntity(mapper.selectById(id)));
    }

    @Override
    public Optional<SemanticMemory> findByUserIdAndConcept(Long userId, String concept) {
        SemanticMemoryDO d = mapper.selectOne(new LambdaQueryWrapper<SemanticMemoryDO>()
                .eq(SemanticMemoryDO::getUserId, userId)
                .eq(SemanticMemoryDO::getConcept, concept)
                .last("limit 1"));
        return Optional.ofNullable(toEntity(d));
    }

    @Override
    public List<SemanticMemory> findByUserId(Long userId, int page, int size) {
        return mapper.selectList(new LambdaQueryWrapper<SemanticMemoryDO>()
                        .eq(SemanticMemoryDO::getUserId, userId)
                        .orderByDesc(SemanticMemoryDO::getLastReinforcedAt)
                        .last("limit "+size+" offset "+(page*size)))
                .stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<SemanticMemory> findByConfidenceRange(Long userId, Double minConfidence, Double maxConfidence, int limit) {
        return mapper.selectList(new LambdaQueryWrapper<SemanticMemoryDO>()
                        .eq(SemanticMemoryDO::getUserId, userId)
                        .ge(SemanticMemoryDO::getConfidence, minConfidence)
                        .le(SemanticMemoryDO::getConfidence, maxConfidence)
                        .orderByDesc(SemanticMemoryDO::getConfidence)
                        .last("limit "+limit))
                .stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<SemanticMemory> findHighConfidenceMemories(Long userId, Double minConfidence, int limit) {
        return findByConfidenceRange(userId, minConfidence, 1.0, limit);
    }

    @Override
    public List<SemanticMemory> findControversialMemories(Long userId, int limit) {
        return mapper.selectList(new LambdaQueryWrapper<SemanticMemoryDO>()
                        .eq(SemanticMemoryDO::getUserId, userId)
                        .gt(SemanticMemoryDO::getContradictionCount, 0)
                        .orderByDesc(SemanticMemoryDO::getContradictionCount)
                        .last("limit "+limit))
                .stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<SemanticMemory> semanticSearch(Long userId, byte[] queryVector, Double threshold, int limit) {
        // 占位：未接入向量索引时，回退概念like
        log.debug("semanticSearch fallback: userId={}", userId);
        return mapper.selectList(new LambdaQueryWrapper<SemanticMemoryDO>()
                        .eq(SemanticMemoryDO::getUserId, userId)
                        .orderByDesc(SemanticMemoryDO::getConfidence)
                        .last("limit "+limit))
                .stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<SemanticMemory> findByConceptLike(Long userId, String conceptPattern, int limit) {
        return mapper.selectList(new LambdaQueryWrapper<SemanticMemoryDO>()
                        .eq(SemanticMemoryDO::getUserId, userId)
                        .like(SemanticMemoryDO::getConcept, conceptPattern)
                        .orderByDesc(SemanticMemoryDO::getConfidence)
                        .last("limit "+limit))
                .stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<SemanticMemory> findMemoriesNeedingUpdate(Long userId, int limit) {
        long sevenDays = 7L*24*60*60*1000;
        long cutoff = System.currentTimeMillis()-sevenDays;
        return mapper.selectList(new LambdaQueryWrapper<SemanticMemoryDO>()
                        .eq(SemanticMemoryDO::getUserId, userId)
                        .lt(SemanticMemoryDO::getLastReinforcedAt, cutoff)
                        .orderByAsc(SemanticMemoryDO::getLastReinforcedAt)
                        .last("limit "+limit))
                .stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<SemanticMemory> findRecentlyReinforced(Long userId, int limit) {
        return mapper.selectList(new LambdaQueryWrapper<SemanticMemoryDO>()
                        .eq(SemanticMemoryDO::getUserId, userId)
                        .orderByDesc(SemanticMemoryDO::getLastReinforcedAt)
                        .last("limit "+limit))
                .stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<SemanticMemory> findBySourceEpisode(Long userId, String episodeId) {
        // 简化：JSON包含匹配（不同数据库可换为JSON_CONTAINS）
        return mapper.selectList(new LambdaQueryWrapper<SemanticMemoryDO>()
                        .eq(SemanticMemoryDO::getUserId, userId)
                        .like(SemanticMemoryDO::getSourceEpisodesJson, episodeId))
                .stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public void update(SemanticMemory semanticMemory) {
        mapper.updateById(toDO(semanticMemory));
    }

    @Override
    public void batchUpdateConfidence(List<Long> ids, Double confidence) {
        if (ids==null || ids.isEmpty()) return;
        mapper.update(null, new LambdaUpdateWrapper<SemanticMemoryDO>()
                .set(SemanticMemoryDO::getConfidence, confidence)
                .in(SemanticMemoryDO::getId, ids));
    }

    @Override
    public void applyDecayToAll(Long userId) {
        // 简化：降低一定比例
        List<SemanticMemoryDO> list = mapper.selectList(new LambdaQueryWrapper<SemanticMemoryDO>()
                .eq(SemanticMemoryDO::getUserId, userId)
                .select(SemanticMemoryDO::getId, SemanticMemoryDO::getConfidence, SemanticMemoryDO::getDecayRate));
        for (SemanticMemoryDO d: list){
            Double c = d.getConfidence();
            Double r = d.getDecayRate()!=null? d.getDecayRate(): 0.01;
            if (c!=null){
                double nc = Math.max(0.0, c*(1.0-r));
                mapper.update(null, new LambdaUpdateWrapper<SemanticMemoryDO>()
                        .set(SemanticMemoryDO::getConfidence, nc)
                        .eq(SemanticMemoryDO::getId, d.getId()));
            }
        }
    }

    @Override
    public void deleteById(Long id) { mapper.deleteById(id); }

    @Override
    public void deleteByUserId(Long userId) {
        mapper.delete(new LambdaQueryWrapper<SemanticMemoryDO>().eq(SemanticMemoryDO::getUserId, userId));
    }

    @Override
    public void deleteLowConfidenceMemories(Long userId, Double threshold) {
        mapper.delete(new LambdaQueryWrapper<SemanticMemoryDO>()
                .eq(SemanticMemoryDO::getUserId, userId)
                .lt(SemanticMemoryDO::getConfidence, threshold));
    }

    @Override
    public Long countByUserId(Long userId) {
        return mapper.selectCount(new LambdaQueryWrapper<SemanticMemoryDO>().eq(SemanticMemoryDO::getUserId, userId));
    }

    @Override
    public Long countHighConfidenceMemories(Long userId, Double threshold) {
        return mapper.selectCount(new LambdaQueryWrapper<SemanticMemoryDO>()
                .eq(SemanticMemoryDO::getUserId, userId)
                .ge(SemanticMemoryDO::getConfidence, threshold));
    }

    @Override
    public Double getAverageConfidence(Long userId) {
        List<SemanticMemoryDO> list = mapper.selectList(new LambdaQueryWrapper<SemanticMemoryDO>()
                .eq(SemanticMemoryDO::getUserId, userId)
                .select(SemanticMemoryDO::getConfidence)
                .last("limit 1000"));
        if (list.isEmpty()) return 0.0;
        return list.stream().map(SemanticMemoryDO::getConfidence).filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    @Override
    public List<SemanticMemory> findSimilarConcepts(Long userId, String concept, Double similarity, int limit) {
        // 简化：概念like
        return findByConceptLike(userId, concept, limit);
    }

    @Override
    public List<SemanticMemory> findConflictingMemories(Long userId, Long memoryId, int limit) {
        // 简化：返回争议性较高的若干条
        return findControversialMemories(userId, limit);
    }
}

