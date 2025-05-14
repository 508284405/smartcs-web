package com.leyue.smartcs.knowledge.parser.impl;

import com.leyue.smartcs.knowledge.parser.SegmentStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 按段落分段策略
 * 将文本按照段落分隔符（如连续换行符）进行分段
 */
@Component
@Slf4j
public class ParagraphSegmentStrategy implements SegmentStrategy {
    
    /**
     * 段落分隔符正则表达式
     * 匹配连续两个或更多换行符，或者换行符加缩进等段落间常见模式
     */
    private static final Pattern PARAGRAPH_DELIMITER = Pattern.compile("\\n\\s*\\n|\\r\\n\\s*\\r\\n");
    
    /**
     * 最大段落长度，超过此长度的段落会被按字符数再次分段
     */
    private static final int MAX_PARAGRAPH_LENGTH = 2000;
    
    /**
     * 最小段落长度，低于此长度的段落会与相邻段落合并
     */
    private static final int MIN_PARAGRAPH_LENGTH = 50;
    
    @Override
    public List<String> segment(String text) {
        log.debug("按段落分段，文本长度: {}", text != null ? text.length() : 0);
        
        if (text == null || text.isEmpty()) {
            log.warn("分段文本为空");
            return new ArrayList<>();
        }
        
        // 按段落分隔符分段
        String[] paragraphs = PARAGRAPH_DELIMITER.split(text);
        
        // 初步分段结果
        List<String> result = new ArrayList<>(Arrays.asList(paragraphs));
        
        // 处理过长或过短的段落
        result = processParagraphSize(result);
        
        log.debug("按段落分段完成，共 {} 段", result.size());
        return result;
    }
    
    /**
     * 处理段落大小，确保段落长度适中
     * 1. 过长的段落会被拆分
     * 2. 过短的段落会被合并
     */
    private List<String> processParagraphSize(List<String> paragraphs) {
        List<String> processed = new ArrayList<>();
        StringBuilder currentParagraph = new StringBuilder();
        
        for (String paragraph : paragraphs) {
            String trimmed = paragraph.trim();
            
            // 跳过空段落
            if (trimmed.isEmpty()) {
                continue;
            }
            
            // 处理过长的段落，按句子或字符数再次分段
            if (trimmed.length() > MAX_PARAGRAPH_LENGTH) {
                // 确保当前积累的短段落先处理完
                if (currentParagraph.length() > 0) {
                    processed.add(currentParagraph.toString().trim());
                    currentParagraph.setLength(0);
                }
                
                // 长段落拆分
                List<String> subParagraphs = splitLongParagraph(trimmed);
                processed.addAll(subParagraphs);
            } 
            // 处理过短的段落，与相邻段落合并
            else if (trimmed.length() < MIN_PARAGRAPH_LENGTH) {
                if (currentParagraph.length() + trimmed.length() > MAX_PARAGRAPH_LENGTH) {
                    processed.add(currentParagraph.toString().trim());
                    currentParagraph.setLength(0);
                }
                
                if (currentParagraph.length() > 0) {
                    currentParagraph.append(" ");
                }
                currentParagraph.append(trimmed);
            }
            // 正常大小的段落
            else {
                if (currentParagraph.length() > 0) {
                    processed.add(currentParagraph.toString().trim());
                    currentParagraph.setLength(0);
                }
                processed.add(trimmed);
            }
        }
        
        // 处理最后可能剩余的短段落
        if (currentParagraph.length() > 0) {
            processed.add(currentParagraph.toString().trim());
        }
        
        return processed;
    }
    
    /**
     * 拆分过长的段落
     * 尝试按句子分割，如果句子仍然过长，则按字符数分割
     */
    private List<String> splitLongParagraph(String paragraph) {
        // 句子分隔符正则表达式：句号、问号、感叹号后跟空格或结束
        Pattern sentenceDelimiter = Pattern.compile("(?<=[.!?。！？])[\\s]");
        
        // 先按句子分割
        String[] sentences = sentenceDelimiter.split(paragraph);
        
        List<String> result = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();
        
        for (String sentence : sentences) {
            // 如果当前句子加上已累积的内容超过最大长度，则先保存当前块
            if (currentChunk.length() + sentence.length() > MAX_PARAGRAPH_LENGTH) {
                if (currentChunk.length() > 0) {
                    result.add(currentChunk.toString().trim());
                    currentChunk.setLength(0);
                }
                
                // 如果单个句子超过最大长度，需要按字符数分割
                if (sentence.length() > MAX_PARAGRAPH_LENGTH) {
                    int start = 0;
                    while (start < sentence.length()) {
                        int end = Math.min(start + MAX_PARAGRAPH_LENGTH, sentence.length());
                        result.add(sentence.substring(start, end).trim());
                        start = end;
                    }
                } else {
                    currentChunk.append(sentence);
                }
            } else {
                if (currentChunk.length() > 0) {
                    currentChunk.append(" ");
                }
                currentChunk.append(sentence);
            }
        }
        
        // 添加最后一个块
        if (currentChunk.length() > 0) {
            result.add(currentChunk.toString().trim());
        }
        
        return result;
    }
    
    @Override
    public String getStrategyName() {
        return "Paragraph";
    }
} 