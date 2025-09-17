package com.leyue.smartcs.ltm.gatewayimpl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyue.smartcs.domain.ltm.entity.EpisodicMemory;
import com.leyue.smartcs.domain.ltm.gateway.EpisodicMemoryGateway;
import com.leyue.smartcs.ltm.dataobject.EpisodicMemoryDO;
import com.leyue.smartcs.ltm.mapper.EpisodicMemoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class EpisodicMemoryGatewayImpl implements EpisodicMemoryGateway {

    private final EpisodicMemoryMapper mapper;

    private EpisodicMemoryDO toDO(EpisodicMemory e){
        EpisodicMemoryDO d = new EpisodicMemoryDO();
        d.setId(e.getId());
        d.setUserId(e.getUserId());
        d.setSessionId(e.getSessionId());
        d.setEpisodeId(e.getEpisodeId());
        d.setContent(e.getContent());
        d.setEmbeddingVector(e.getEmbeddingVector());
        d.setContextJson(e.getContextMetadata()!=null? JSON.toJSONString(e.getContextMetadata()):null);
        d.setTimestamp(e.getTimestamp());
        d.setImportanceScore(e.getImportanceScore());
        d.setAccessCount(e.getAccessCount());
        d.setLastAccessedAt(e.getLastAccessedAt());
        d.setConsolidationStatus(e.getConsolidationStatus());
        d.setCreatedAt(e.getCreatedAt());
        d.setUpdatedAt(e.getUpdatedAt());
        return d;
    }

    @SuppressWarnings("unchecked")
    private EpisodicMemory toEntity(EpisodicMemoryDO d){
        if (d==null) return null;
        Map<String,Object> ctx = null;
        if (d.getContextJson()!=null && !d.getContextJson().isBlank()){
            try { ctx = JSON.parseObject(d.getContextJson(), Map.class); } catch (Exception ignore) {}
        }
        return EpisodicMemory.builder()
                .id(d.getId())
                .userId(d.getUserId())
                .sessionId(d.getSessionId())
                .episodeId(d.getEpisodeId())
                .content(d.getContent())
                .embeddingVector(d.getEmbeddingVector())
                .contextMetadata(ctx)
                .timestamp(d.getTimestamp())
                .importanceScore(d.getImportanceScore())
                .accessCount(d.getAccessCount())
                .lastAccessedAt(d.getLastAccessedAt())
                .consolidationStatus(d.getConsolidationStatus())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }

    private float[] toFloatArray(byte[] bytes) {
        if (bytes == null || bytes.length == 0 || bytes.length % 4 != 0) {
            return new float[0];
        }
        float[] array = new float[bytes.length / 4];
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        for (int i = 0; i < array.length; i++) {
            array[i] = buffer.getFloat();
        }
        return array;
    }

    private double cosineSimilarity(float[] a, float[] b) {
        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0.0 || normB == 0.0) {
            return Double.NaN;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private record ScoredMemory<T>(T memory, double score) {}

    @Override
    public void save(EpisodicMemory episodicMemory) {
        mapper.insert(toDO(episodicMemory));
    }

    @Override
    public void batchSave(List<EpisodicMemory> memories) {
        if (memories==null || memories.isEmpty()) return;
        for (EpisodicMemory m: memories){ mapper.insert(toDO(m)); }
    }

    @Override
    public Optional<EpisodicMemory> findById(Long id) {
        return Optional.ofNullable(toEntity(mapper.selectById(id)));
    }

    @Override
    public Optional<EpisodicMemory> findByEpisodeId(String episodeId) {
        EpisodicMemoryDO d = mapper.selectOne(new LambdaQueryWrapper<EpisodicMemoryDO>()
                .eq(EpisodicMemoryDO::getEpisodeId, episodeId).last("limit 1"));
        return Optional.ofNullable(toEntity(d));
    }

    @Override
    public List<EpisodicMemory> findByUserIdAndSessionId(Long userId, Long sessionId) {
        return mapper.selectList(new LambdaQueryWrapper<EpisodicMemoryDO>()
                .eq(EpisodicMemoryDO::getUserId, userId)
                .eq(EpisodicMemoryDO::getSessionId, sessionId)
                .orderByDesc(EpisodicMemoryDO::getTimestamp))
                .stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<EpisodicMemory> findByUserId(Long userId, int page, int size) {
        Page<EpisodicMemoryDO> p = mapper.selectPage(new Page<>(page+1, size),
                new LambdaQueryWrapper<EpisodicMemoryDO>()
                        .eq(EpisodicMemoryDO::getUserId, userId)
                        .orderByDesc(EpisodicMemoryDO::getTimestamp));
        return p.getRecords().stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<EpisodicMemory> findByUserIdAndTimeRange(Long userId, Long startTime, Long endTime) {
        return mapper.selectList(new LambdaQueryWrapper<EpisodicMemoryDO>()
                        .eq(EpisodicMemoryDO::getUserId, userId)
                        .between(EpisodicMemoryDO::getTimestamp, startTime, endTime)
                        .orderByDesc(EpisodicMemoryDO::getTimestamp))
                .stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<EpisodicMemory> findByImportanceScore(Long userId, Double minScore, int limit) {
        return mapper.selectList(new LambdaQueryWrapper<EpisodicMemoryDO>()
                        .eq(EpisodicMemoryDO::getUserId, userId)
                        .ge(EpisodicMemoryDO::getImportanceScore, minScore)
                        .orderByDesc(EpisodicMemoryDO::getImportanceScore)
                        .last("limit "+limit))
                .stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<EpisodicMemory> semanticSearch(Long userId, byte[] queryVector, Double threshold, int limit) {
        if (queryVector == null || queryVector.length == 0) {
            log.debug("semanticSearch 缺少查询向量，退化为重要度排序: userId={}", userId);
            return findByImportanceScore(userId, 0.0, limit);
        }

        float[] query = toFloatArray(queryVector);
        if (query.length == 0) {
            log.debug("semanticSearch 查询向量解析失败，退化为重要度排序: userId={}", userId);
            return findByImportanceScore(userId, 0.0, limit);
        }

        int fetchSize = Math.max(limit * 5, 100);
        List<EpisodicMemoryDO> candidates = mapper.selectList(new LambdaQueryWrapper<EpisodicMemoryDO>()
                .eq(EpisodicMemoryDO::getUserId, userId)
                .isNotNull(EpisodicMemoryDO::getEmbeddingVector)
                .orderByDesc(EpisodicMemoryDO::getImportanceScore)
                .last("limit " + fetchSize));

        if (candidates.isEmpty()) {
            return Collections.emptyList();
        }

        List<ScoredMemory<EpisodicMemory>> scored = new ArrayList<>();
        for (EpisodicMemoryDO candidate : candidates) {
            byte[] embeddingBytes = candidate.getEmbeddingVector();
            if (embeddingBytes == null || embeddingBytes.length == 0) {
                continue;
            }
            float[] memoryVector = toFloatArray(embeddingBytes);
            if (memoryVector.length == 0 || memoryVector.length != query.length) {
                continue;
            }
            double score = cosineSimilarity(query, memoryVector);
            if (Double.isNaN(score)) {
                continue;
            }
            if (threshold != null && score < threshold) {
                continue;
            }
            scored.add(new ScoredMemory<>(toEntity(candidate), score));
        }

        if (scored.isEmpty()) {
            log.debug("semanticSearch 未命中相似记忆: userId={}", userId);
            return Collections.emptyList();
        }

        scored.sort(Comparator.comparingDouble(ScoredMemory::score).reversed());
        return scored.stream()
                .limit(limit)
                .map(ScoredMemory::memory)
                .collect(Collectors.toList());
    }

    @Override
    public List<EpisodicMemory> hybridSearch(Long userId, byte[] queryVector, String keywords, Double threshold, int limit) {
        LambdaQueryWrapper<EpisodicMemoryDO> qw = new LambdaQueryWrapper<EpisodicMemoryDO>()
                .eq(EpisodicMemoryDO::getUserId, userId);
        if (keywords!=null && !keywords.isBlank()) {
            qw.like(EpisodicMemoryDO::getContent, keywords);
        }
        qw.orderByDesc(EpisodicMemoryDO::getTimestamp).last("limit "+limit);
        return mapper.selectList(qw).stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<EpisodicMemory> findMemoriesNeedingConsolidation(Long userId, double minImportanceScore, int limit) {
        return mapper.selectList(new LambdaQueryWrapper<EpisodicMemoryDO>()
                        .eq(EpisodicMemoryDO::getUserId, userId)
                        .eq(EpisodicMemoryDO::getConsolidationStatus, 0)
                        .ge(EpisodicMemoryDO::getImportanceScore, minImportanceScore)
                        .orderByDesc(EpisodicMemoryDO::getTimestamp)
                        .last("limit "+limit))
                .stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<Long> findUserIdsNeedingConsolidation(double minImportanceScore, Long startingAfterUserId, int limit) {
        List<Long> userIds = mapper.selectNeedingConsolidationUserIds(minImportanceScore, startingAfterUserId, limit);
        return userIds == null ? Collections.emptyList() : userIds;
    }

    @Override
    public List<EpisodicMemory> findRecentlyAccessed(Long userId, int limit) {
        return mapper.selectList(new LambdaQueryWrapper<EpisodicMemoryDO>()
                        .eq(EpisodicMemoryDO::getUserId, userId)
                        .orderByDesc(EpisodicMemoryDO::getLastAccessedAt)
                        .last("limit "+limit))
                .stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<EpisodicMemory> findHighFrequencyAccessed(Long userId, int minAccessCount, int limit) {
        return mapper.selectList(new LambdaQueryWrapper<EpisodicMemoryDO>()
                        .eq(EpisodicMemoryDO::getUserId, userId)
                        .ge(EpisodicMemoryDO::getAccessCount, minAccessCount)
                        .orderByDesc(EpisodicMemoryDO::getAccessCount)
                        .last("limit "+limit))
                .stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public void update(EpisodicMemory episodicMemory) {
        mapper.updateById(toDO(episodicMemory));
    }

    @Override
    public void batchUpdateConsolidationStatus(List<Long> ids, Integer status) {
        if (ids==null || ids.isEmpty()) return;
        mapper.update(null, new LambdaUpdateWrapper<EpisodicMemoryDO>()
                .set(EpisodicMemoryDO::getConsolidationStatus, status)
                .in(EpisodicMemoryDO::getId, ids));
    }

    @Override
    public void deleteById(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public void deleteByUserId(Long userId) {
        mapper.delete(new LambdaQueryWrapper<EpisodicMemoryDO>().eq(EpisodicMemoryDO::getUserId, userId));
    }

    @Override
    public void deleteByTimeRange(Long userId, Long startTime, Long endTime) {
        mapper.delete(new LambdaQueryWrapper<EpisodicMemoryDO>()
                .eq(EpisodicMemoryDO::getUserId, userId)
                .between(EpisodicMemoryDO::getTimestamp, startTime, endTime));
    }

    @Override
    public Long countByUserId(Long userId) {
        return mapper.selectCount(new LambdaQueryWrapper<EpisodicMemoryDO>().eq(EpisodicMemoryDO::getUserId, userId));
    }

    @Override
    public Long countByUserIdAndTimeRange(Long userId, Long startTime, Long endTime) {
        return mapper.selectCount(new LambdaQueryWrapper<EpisodicMemoryDO>()
                .eq(EpisodicMemoryDO::getUserId, userId)
                .between(EpisodicMemoryDO::getTimestamp, startTime, endTime));
    }

    @Override
    public Double getAverageImportanceScore(Long userId) {
        // MP无内置avg，这里简化为取出部分样本计算
        List<EpisodicMemoryDO> list = mapper.selectList(new LambdaQueryWrapper<EpisodicMemoryDO>()
                .eq(EpisodicMemoryDO::getUserId, userId)
                .select(EpisodicMemoryDO::getImportanceScore)
                .last("limit 1000"));
        if (list.isEmpty()) return 0.0;
        return list.stream().map(EpisodicMemoryDO::getImportanceScore).filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    @Override
    public Optional<Long> findEarliestMemoryTime(Long userId) {
        EpisodicMemoryDO d = mapper.selectOne(new LambdaQueryWrapper<EpisodicMemoryDO>()
                .eq(EpisodicMemoryDO::getUserId, userId)
                .orderByAsc(EpisodicMemoryDO::getTimestamp).last("limit 1"));
        return Optional.ofNullable(d!=null? d.getTimestamp(): null);
    }

    @Override
    public Optional<Long> findLatestMemoryTime(Long userId) {
        EpisodicMemoryDO d = mapper.selectOne(new LambdaQueryWrapper<EpisodicMemoryDO>()
                .eq(EpisodicMemoryDO::getUserId, userId)
                .orderByDesc(EpisodicMemoryDO::getTimestamp).last("limit 1"));
        return Optional.ofNullable(d!=null? d.getTimestamp(): null);
    }
}
