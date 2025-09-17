package com.leyue.smartcs.ltm.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 配置项：长期记忆巩固任务
 */
@Data
@Component
@ConfigurationProperties(prefix = "smartcs.ai.ltm.consolidation")
public class MemoryConsolidationProperties {

    /**
     * 每个用户单次巩固的最大记忆数量
     */
    private int batchSize = 100;

    /**
     * 是否启用巩固任务
     */
    private boolean enabled = true;

    /**
     * 参与巩固的记忆重要性阈值
     */
    private double importanceThreshold = 0.7;

    /**
     * 语义记忆巩固开关
     */
    private Toggle semantic = new Toggle();

    /**
     * 程序性记忆巩固开关
     */
    private Toggle procedural = new Toggle();

    /**
     * 生成语义记忆时允许参与的最大情景记忆数量
     */
    private int maxEpisodesPerConcept = 5;

    /**
     * 单次从数据库获取的用户数量
     */
    private int userFetchSize = 200;

    /**
     * Spring Batch chunk 大小
     */
    private int chunkSize = 10;

    /**
     * Batch Step的重试次数
     */
    private int retryLimit = 3;

    public boolean isSemanticEnabled() {
        return semantic.isEnabled();
    }

    public boolean isProceduralEnabled() {
        return procedural.isEnabled();
    }

    @Data
    public static class Toggle {
        private boolean enabled = true;
    }
}
