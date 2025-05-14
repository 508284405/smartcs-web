package com.leyue.smartcs.knowledge.parser.impl;

import com.leyue.smartcs.knowledge.parser.SegmentStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 按字符数分段策略
 * 将文本按照固定字符数量分割成多个段落
 */
@Component
@Slf4j
public class CharCountSegmentStrategy implements SegmentStrategy {
    
    /**
     * 默认分段字符长度
     */
    private static final int DEFAULT_CHUNK_SIZE = 1000;
    
    /**
     * 分段字符长度
     */
    private final int chunkSize;
    
    /**
     * 使用默认分段长度的构造函数
     */
    public CharCountSegmentStrategy() {
        this(DEFAULT_CHUNK_SIZE);
    }
    
    /**
     * 指定分段长度的构造函数
     * @param chunkSize 每段长度
     */
    public CharCountSegmentStrategy(int chunkSize) {
        this.chunkSize = chunkSize;
    }
    
    @Override
    public List<String> segment(String text) {
        log.debug("按字符数分段，文本长度: {}, 分段长度: {}", text.length(), chunkSize);
        
        List<String> chunks = new ArrayList<>();
        
        if (text == null || text.isEmpty()) {
            log.warn("分段文本为空");
            return chunks;
        }
        
        // 简单按字符长度分段
        for (int i = 0; i < text.length(); i += chunkSize) {
            int end = Math.min(i + chunkSize, text.length());
            chunks.add(text.substring(i, end));
        }
        
        log.debug("分段完成，共 {} 段", chunks.size());
        return chunks;
    }
    
    @Override
    public String getStrategyName() {
        return "CharCount-" + chunkSize;
    }
} 