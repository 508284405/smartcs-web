package com.leyue.smartcs.knowledge.parser.impl;

import com.leyue.smartcs.knowledge.parser.SegmentStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 按句子分段策略
 * 将文本按照句子结束标记（如句号、问号、感叹号等）进行分段
 */
@Component
@Slf4j
public class SentenceSegmentStrategy implements SegmentStrategy {
    
    /**
     * 句子结束标记正则表达式
     * 匹配句号、问号、感叹号等，包括中英文标点
     */
    private static final Pattern SENTENCE_END = Pattern.compile("([.!?。！？；;]+)[\\s]*");
    
    /**
     * 分组长度，即将多少个句子组合为一个段落
     */
    private static final int SENTENCE_GROUP_SIZE = 3;
    
    /**
     * 最大句子长度，超过此长度的句子会被按字符数再次分段
     */
    private static final int MAX_SENTENCE_LENGTH = 1000;
    
    @Override
    public List<String> segment(String text) {
        log.debug("按句子分段，文本长度: {}", text != null ? text.length() : 0);
        
        if (text == null || text.isEmpty()) {
            log.warn("分段文本为空");
            return new ArrayList<>();
        }
        
        // 提取所有句子
        List<String> sentences = extractSentences(text);
        
        // 处理句子，过长的句子会被拆分
        sentences = processSentences(sentences);
        
        // 将句子分组，每N个句子一组
        List<String> paragraphs = groupSentences(sentences, SENTENCE_GROUP_SIZE);
        
        log.debug("按句子分段完成，共 {} 句，分为 {} 组", sentences.size(), paragraphs.size());
        return paragraphs;
    }
    
    /**
     * 提取文本中的所有句子
     */
    private List<String> extractSentences(String text) {
        List<String> sentences = new ArrayList<>();
        
        // 使用正则表达式匹配句子结束标记
        Matcher matcher = SENTENCE_END.matcher(text);
        int lastEnd = 0;
        
        // 遍历所有匹配项
        while (matcher.find()) {
            if (matcher.start() >= lastEnd) {
                // 提取句子，包括结束标记
                String sentence = text.substring(lastEnd, matcher.end()).trim();
                if (!sentence.isEmpty()) {
                    sentences.add(sentence);
                }
                lastEnd = matcher.end();
            }
        }
        
        // 处理最后一句，如果没有结束标记
        if (lastEnd < text.length()) {
            String lastSentence = text.substring(lastEnd).trim();
            if (!lastSentence.isEmpty()) {
                sentences.add(lastSentence);
            }
        }
        
        return sentences;
    }
    
    /**
     * 处理句子，过长的句子会被拆分
     */
    private List<String> processSentences(List<String> sentences) {
        List<String> processed = new ArrayList<>();
        
        for (String sentence : sentences) {
            if (sentence.length() <= MAX_SENTENCE_LENGTH) {
                processed.add(sentence);
            } else {
                // 过长的句子按逗号或分号等次级分隔符拆分
                List<String> subSentences = splitLongSentence(sentence);
                processed.addAll(subSentences);
            }
        }
        
        return processed;
    }
    
    /**
     * 拆分过长的句子
     * 尝试按逗号、分号等次级分隔符拆分，如果仍然过长，则按字符数拆分
     */
    private List<String> splitLongSentence(String sentence) {
        // 次级分隔符正则：逗号、分号等
        Pattern subDelimiter = Pattern.compile("([,，、；:]+)[\\s]*");
        
        List<String> parts = new ArrayList<>();
        Matcher matcher = subDelimiter.matcher(sentence);
        int lastEnd = 0;
        
        // 按次级分隔符拆分
        while (matcher.find()) {
            if (matcher.start() >= lastEnd) {
                String part = sentence.substring(lastEnd, matcher.end()).trim();
                if (!part.isEmpty()) {
                    parts.add(part);
                }
                lastEnd = matcher.end();
            }
        }
        
        // 处理最后一部分
        if (lastEnd < sentence.length()) {
            String lastPart = sentence.substring(lastEnd).trim();
            if (!lastPart.isEmpty()) {
                parts.add(lastPart);
            }
        }
        
        // 如果拆分后仍有过长部分，按字符数再次拆分
        List<String> result = new ArrayList<>();
        for (String part : parts) {
            if (part.length() <= MAX_SENTENCE_LENGTH) {
                result.add(part);
            } else {
                int start = 0;
                while (start < part.length()) {
                    int end = Math.min(start + MAX_SENTENCE_LENGTH, part.length());
                    result.add(part.substring(start, end).trim());
                    start = end;
                }
            }
        }
        
        return result;
    }
    
    /**
     * 将句子按组合并为段落
     * @param sentences 句子列表
     * @param groupSize 每组句子数量
     * @return 段落列表
     */
    private List<String> groupSentences(List<String> sentences, int groupSize) {
        List<String> paragraphs = new ArrayList<>();
        
        if (sentences.isEmpty()) {
            return paragraphs;
        }
        
        StringBuilder currentGroup = new StringBuilder();
        int count = 0;
        
        for (String sentence : sentences) {
            if (count > 0) {
                currentGroup.append(" ");
            }
            
            currentGroup.append(sentence);
            count++;
            
            // 达到分组大小或句子过长，创建新段落
            if (count >= groupSize || currentGroup.length() >= MAX_SENTENCE_LENGTH * 2) {
                paragraphs.add(currentGroup.toString().trim());
                currentGroup.setLength(0);
                count = 0;
            }
        }
        
        // 添加最后一组
        if (currentGroup.length() > 0) {
            paragraphs.add(currentGroup.toString().trim());
        }
        
        return paragraphs;
    }
    
    @Override
    public String getStrategyName() {
        return "Sentence";
    }
} 