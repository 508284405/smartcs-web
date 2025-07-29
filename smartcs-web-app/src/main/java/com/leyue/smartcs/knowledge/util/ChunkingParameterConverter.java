package com.leyue.smartcs.knowledge.util;

import com.leyue.smartcs.dto.knowledge.KnowledgeGeneralChunkCmd;
import com.leyue.smartcs.knowledge.model.ChunkingStrategyConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * 分块参数转换工具类
 * 将命令参数转换为策略配置参数
 */
@Slf4j
public class ChunkingParameterConverter {

    /**
     * 将通用分块命令参数转换为策略配置参数
     * 
     * @param cmd 分块命令
     * @return 策略配置参数
     */
    public static ChunkingStrategyConfig convertToStrategyConfig(KnowledgeGeneralChunkCmd cmd) {
        ChunkingStrategyConfig.ChunkingStrategyConfigBuilder builder = ChunkingStrategyConfig.builder();
        
        // 基础分块参数，如果命令中有值则使用，否则使用默认值
        if (cmd.getChunkSize() != null) {
            builder.chunkSize(cmd.getChunkSize());
        }
        if (cmd.getOverlapSize() != null) {
            builder.overlapSize(cmd.getOverlapSize());
        }
        if (cmd.getChunkSeparator() != null) {
            builder.chunkSeparator(cmd.getChunkSeparator());
        }
        if (cmd.getMinChunkSize() != null) {
            builder.minChunkSize(cmd.getMinChunkSize());
        }
        if (cmd.getMaxChunkSize() != null) {
            builder.maxChunkSize(cmd.getMaxChunkSize());
        }
        if (cmd.getKeepSeparator() != null) {
            builder.keepSeparator(cmd.getKeepSeparator());
        }
        if (cmd.getStripWhitespace() != null) {
            builder.stripWhitespace(cmd.getStripWhitespace());
        }
        if (cmd.getRemoveAllUrls() != null) {
            builder.removeAllUrls(cmd.getRemoveAllUrls());
        }
        if (cmd.getUseQASegmentation() != null) {
            builder.useQASegmentation(cmd.getUseQASegmentation());
        }
        if (cmd.getQaLanguage() != null) {
            builder.qaLanguage(cmd.getQaLanguage());
        }
        
        ChunkingStrategyConfig config = builder.build();
        log.debug("转换分块参数: {}", config);
        return config;
    }
    
    
    /**
     * 验证配置参数的有效性
     */
    public static boolean validateConfig(ChunkingStrategyConfig config) {
        if (config == null) {
            log.warn("配置对象为空");
            return false;
        }
        
        try {
            boolean isValid = config.isValid();
            if (!isValid) {
                log.warn("配置参数验证失败: {}", config);
            }
            return isValid;
        } catch (Exception e) {
            log.error("参数验证失败", e);
            return false;
        }
    }
} 