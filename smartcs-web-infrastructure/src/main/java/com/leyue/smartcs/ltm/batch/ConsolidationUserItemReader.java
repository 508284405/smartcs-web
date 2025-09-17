package com.leyue.smartcs.ltm.batch;

import com.leyue.smartcs.domain.ltm.gateway.EpisodicMemoryGateway;
import com.leyue.smartcs.ltm.config.MemoryConsolidationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;

import java.util.Collections;
import java.util.List;

/**
 * ItemReader：按分页获取待巩固记忆的用户ID
 */
@Slf4j
@RequiredArgsConstructor
public class ConsolidationUserItemReader implements ItemStreamReader<Long> {

    private static final String LAST_USER_ID_KEY = "memoryConsolidation.lastUserId";

    private final EpisodicMemoryGateway episodicMemoryGateway;
    private final MemoryConsolidationProperties properties;

    private List<Long> cachedUserIds = Collections.emptyList();
    private int currentIndex = 0;
    private Long lastUserId;

    @Override
    public Long read() {
        if (cachedUserIds == null || currentIndex >= cachedUserIds.size()) {
            fetchNextBatch();
            if (cachedUserIds.isEmpty()) {
                return null;
            }
        }

        Long userId = cachedUserIds.get(currentIndex++);
        lastUserId = userId;
        return userId;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        if (executionContext.containsKey(LAST_USER_ID_KEY)) {
            lastUserId = executionContext.getLong(LAST_USER_ID_KEY);
            log.debug("恢复批处理游标，lastUserId={}", lastUserId);
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        if (lastUserId != null) {
            executionContext.putLong(LAST_USER_ID_KEY, lastUserId);
        }
    }

    @Override
    public void close() throws ItemStreamException {
        cachedUserIds = Collections.emptyList();
        currentIndex = 0;
    }

    private void fetchNextBatch() {
        int fetchSize = Math.max(1, properties.getUserFetchSize());
        double minImportance = properties.getImportanceThreshold();
        cachedUserIds = episodicMemoryGateway.findUserIdsNeedingConsolidation(minImportance, lastUserId, fetchSize);
        currentIndex = 0;

        if (cachedUserIds.isEmpty()) {
            log.debug("没有查询到待巩固的用户ID");
            return;
        }

        Long first = cachedUserIds.get(0);
        Long last = cachedUserIds.get(cachedUserIds.size() - 1);
        log.debug("拉取到{}个用户ID，范围={}~{}", cachedUserIds.size(), first, last);
    }
}
