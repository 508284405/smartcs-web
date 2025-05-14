package com.leyue.smartcs.knowledge.parser.impl;

import com.leyue.smartcs.knowledge.parser.SegmentStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 混合分段策略
 * 结合段落分段和句子分段的优点，先按段落分，再根据段落长度决定是否继续分段
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class HybridSegmentStrategy implements SegmentStrategy {
    
    /**
     * 段落分隔符正则表达式
     */
    private static final Pattern PARAGRAPH_DELIMITER = Pattern.compile("\\n\\s*\\n|\\r\\n\\s*\\r\\n");
    
    /**
     * 每段的最小长度
     */
    private static final int MIN_CHUNK_SIZE = 100;
    
    /**
     * 每段的理想长度
     */
    private static final int IDEAL_CHUNK_SIZE = 500;
    
    /**
     * 每段的最大长度
     */
    private static final int MAX_CHUNK_SIZE = 1000;
    
    /**
     * 使用依赖注入，获取其他分段策略
     */
    private final ParagraphSegmentStrategy paragraphStrategy;
    private final SentenceSegmentStrategy sentenceStrategy;
    private final CharCountSegmentStrategy charCountStrategy;
    
    @Override
    public List<String> segment(String text) {
        log.debug("使用混合分段策略处理文本，长度: {}", text != null ? text.length() : 0);
        
        if (text == null || text.isEmpty()) {
            log.warn("分段文本为空");
            return new ArrayList<>();
        }
        
        // 步骤1: 先按段落分段
        List<String> paragraphs = paragraphStrategy.segment(text);
        
        // 步骤2: 处理每个段落，过长的段落进一步分割
        List<String> result = new ArrayList<>();
        for (String paragraph : paragraphs) {
            if (paragraph.length() <= MAX_CHUNK_SIZE) {
                // 段落长度合适，直接添加
                if (paragraph.length() >= MIN_CHUNK_SIZE) {
                    result.add(paragraph);
                } else {
                    // 过短的段落尝试与相邻段落合并（这里简化处理，直接添加）
                    result.add(paragraph);
                }
            } else {
                // 段落过长，按句子再次分段
                List<String> sentences = sentenceStrategy.segment(paragraph);
                result.addAll(sentences);
            }
        }
        
        // 步骤3: 对结果进行最终处理，确保没有过长或过短的段落
        result = finalizeSegments(result);
        
        log.debug("混合分段完成，共 {} 段", result.size());
        return result;
    }
    
    /**
     * 最终处理分段结果，确保段落长度适中
     */
    private List<String> finalizeSegments(List<String> segments) {
        List<String> finalized = new ArrayList<>();
        StringBuilder currentSegment = new StringBuilder();
        
        for (String segment : segments) {
            // 处理过长的段落，使用字符数分段
            if (segment.length() > MAX_CHUNK_SIZE) {
                // 先处理当前积累的段落
                if (currentSegment.length() > 0) {
                    finalized.add(currentSegment.toString().trim());
                    currentSegment.setLength(0);
                }
                
                // 使用字符数策略处理过长段落
                List<String> charSegments = charCountStrategy.segment(segment);
                finalized.addAll(charSegments);
                continue;
            }
            
            // 处理过短的段落，尝试合并
            if (segment.length() < MIN_CHUNK_SIZE) {
                // 如果当前积累的段落加上新段落仍在理想范围内，则合并
                if (currentSegment.length() + segment.length() + 1 <= IDEAL_CHUNK_SIZE) {
                    if (currentSegment.length() > 0) {
                        currentSegment.append(" ");
                    }
                    currentSegment.append(segment);
                } else {
                    // 当前积累的段落已达到理想长度，保存并重新开始
                    if (currentSegment.length() > 0) {
                        finalized.add(currentSegment.toString().trim());
                        currentSegment.setLength(0);
                        currentSegment.append(segment);
                    } else {
                        // 直接添加短段落，虽然不理想但无法合并
                        finalized.add(segment);
                    }
                }
            } else {
                // 段落长度适中，先处理当前积累的段落
                if (currentSegment.length() > 0) {
                    finalized.add(currentSegment.toString().trim());
                    currentSegment.setLength(0);
                }
                
                // 直接添加适中长度的段落
                finalized.add(segment);
            }
        }
        
        // 处理最后可能积累的段落
        if (currentSegment.length() > 0) {
            finalized.add(currentSegment.toString().trim());
        }
        
        return finalized;
    }
    
    @Override
    public String getStrategyName() {
        return "Hybrid";
    }
} 