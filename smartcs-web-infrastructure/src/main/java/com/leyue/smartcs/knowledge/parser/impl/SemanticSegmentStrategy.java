package com.leyue.smartcs.knowledge.parser.impl;

import com.leyue.smartcs.knowledge.parser.SegmentStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 语义分段策略
 * 基于文本的主题变化和语义相似度进行智能分段
 * 注意：完整实现需要依赖NLP库，此处提供简化实现
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SemanticSegmentStrategy implements SegmentStrategy {
    
    /**
     * 句子分隔符正则表达式
     */
    private static final Pattern SENTENCE_DELIMITER = Pattern.compile("(?<=[.!?。！？])[\\s]");
    
    /**
     * 段落最大长度
     */
    private static final int MAX_SEGMENT_LENGTH = 1500;
    
    /**
     * 关键词匹配模式，用于识别可能的主题变化
     * 如 "首先"、"其次"、"此外"、"总结"等过渡词
     */
    private static final Pattern TOPIC_TRANSITION = Pattern.compile(
            "(?i)(首先|其次|再者|然后|此外|接着|最后|总之|总结|另外|值得注意的是|需要强调的是|简而言之)");
    
    /**
     * 依赖注入其他策略用于后处理
     */
    private final SentenceSegmentStrategy sentenceStrategy;
    
    @Override
    public List<String> segment(String text) {
        log.debug("使用语义分段策略处理文本，长度: {}", text != null ? text.length() : 0);
        
        if (text == null || text.isEmpty()) {
            log.warn("分段文本为空");
            return new ArrayList<>();
        }
        
        // 步骤1: 先按句子分割文本
        String[] sentences = SENTENCE_DELIMITER.split(text);
        List<String> sentenceList = new ArrayList<>(Arrays.asList(sentences));
        
        // 步骤2: 基于语义特征检测主题变化点
        List<Integer> breakPoints = detectTopicChangePoints(sentenceList);
        
        // 步骤3: 根据主题变化点进行分段
        List<String> segments = createSegmentsFromBreakPoints(sentenceList, breakPoints);
        
        log.debug("语义分段完成，共 {} 段", segments.size());
        return segments;
    }
    
    /**
     * 检测主题变化点，简化版实现
     * 实际应用中可使用更复杂的NLP技术如嵌入向量相似度、关键词提取等
     */
    private List<Integer> detectTopicChangePoints(List<String> sentences) {
        List<Integer> breakPoints = new ArrayList<>();
        StringBuilder currentSegment = new StringBuilder();
        
        for (int i = 0; i < sentences.size(); i++) {
            String sentence = sentences.get(i);
            
            // 检测是否包含主题转换指示词
            boolean hasTransitionWord = TOPIC_TRANSITION.matcher(sentence).find();
            
            // 当前段落太长或存在转换词时创建断点
            if (hasTransitionWord || 
                (currentSegment.length() > 0 && 
                 currentSegment.length() + sentence.length() > MAX_SEGMENT_LENGTH)) {
                breakPoints.add(i);
                currentSegment.setLength(0); // 重置当前段落
            }
            
            currentSegment.append(sentence).append(" ");
        }
        
        return breakPoints;
    }
    
    /**
     * 根据断点创建段落
     */
    private List<String> createSegmentsFromBreakPoints(List<String> sentences, List<Integer> breakPoints) {
        List<String> segments = new ArrayList<>();
        
        if (breakPoints.isEmpty() || breakPoints.get(0) != 0) {
            breakPoints.add(0, 0); // 添加起始点
        }
        
        if (breakPoints.isEmpty() || breakPoints.get(breakPoints.size() - 1) != sentences.size()) {
            breakPoints.add(sentences.size()); // 添加结束点
        }
        
        // 根据断点创建段落
        for (int i = 0; i < breakPoints.size() - 1; i++) {
            int start = breakPoints.get(i);
            int end = breakPoints.get(i + 1);
            
            StringBuilder segmentBuilder = new StringBuilder();
            for (int j = start; j < end; j++) {
                segmentBuilder.append(sentences.get(j)).append(" ");
            }
            
            String segment = segmentBuilder.toString().trim();
            
            // 过长段落进一步处理
            if (segment.length() > MAX_SEGMENT_LENGTH) {
                // 使用句子策略进一步分段
                segments.addAll(sentenceStrategy.segment(segment));
            } else if (!segment.isEmpty()) {
                segments.add(segment);
            }
        }
        
        return segments;
    }
    
    @Override
    public String getStrategyName() {
        return "Semantic";
    }
} 