package com.leyue.smartcs.model.ai;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.scoring.ScoringModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 基于LLM的文本相关性打分模型实现
 * 
 * <p>该实现类通过调用LLM模型对查询与文本段落的相关性进行评分，返回0-1之间的分数。
 * 支持单个和批量文本打分，适用于RAG场景下的内容重排序。</p>
 * 
 * <h3>主要特性:</h3>
 * <ul>
 *   <li>基于LLM的智能相关性评估</li>
 *   <li>支持批量处理以提升性能</li>
 *   <li>完善的错误处理和降级机制</li>
 *   <li>结构化的评分提示模板</li>
 * </ul>
 * 
 * <h3>评分标准:</h3>
 * <ul>
 *   <li>0.9-1.0: 高度相关，完美匹配查询意图</li>
 *   <li>0.7-0.8: 中度相关，部分匹配查询内容</li>
 *   <li>0.5-0.6: 低度相关，存在一定关联</li>
 *   <li>0.0-0.4: 不相关或几乎无关联</li>
 * </ul>
 */
@Slf4j
@RequiredArgsConstructor
public class LlmBasedScoringModel implements ScoringModel {
    
    private final ChatModel chatModel;
    
    /**
     * 用于解析LLM返回分数的正则表达式
     * 匹配格式：数字.数字 或 纯数字
     */
    private static final Pattern SCORE_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)");
    
    /**
     * 默认降级分数，当LLM调用失败或解析错误时使用
     */
    private static final double DEFAULT_FALLBACK_SCORE = 0.5;
    
    /**
     * 批量打分的最大处理数量
     */
    private static final int MAX_BATCH_SIZE = 10;
    
    @Override
    public Response<Double> score(TextSegment textSegment, String query) {
        log.debug("开始单个文本段落打分: query='{}', textLength={}", 
                query, textSegment.text().length());
        
        List<TextSegment> segments = List.of(textSegment);
        Response<List<Double>> batchResponse = scoreAll(segments, query);
        
        double score = batchResponse.content().isEmpty() ? 
                DEFAULT_FALLBACK_SCORE : batchResponse.content().get(0);
        
        return Response.from(score, batchResponse.tokenUsage(), batchResponse.finishReason());
    }
    
    @Override
    public Response<Double> score(String text, String query) {
        return score(TextSegment.from(text), query);
    }
    
    @Override
    public Response<List<Double>> scoreAll(List<TextSegment> textSegments, String query) {
        log.debug("开始批量文本打分: query='{}', segmentCount={}", query, textSegments.size());
        
        if (textSegments.isEmpty()) {
            log.warn("文本段落列表为空，返回空结果");
            return Response.from(List.of());
        }
        
        // 对于大批量数据，分批处理以避免超出LLM上下文限制
        if (textSegments.size() > MAX_BATCH_SIZE) {
            return processBatchesSequentially(textSegments, query);
        }
        
        return processBatch(textSegments, query);
    }
    
    /**
     * 处理单个批次的文本打分
     */
    private Response<List<Double>> processBatch(List<TextSegment> textSegments, String query) {
        try {
            String prompt = buildScoringPrompt(textSegments, query);
            log.debug("发送打分请求到LLM，段落数量: {}", textSegments.size());
            
            // 使用LangChain4j 1.1.0 API调用ChatModel
            UserMessage userMessage = UserMessage.from(prompt);
            ChatResponse response = chatModel.chat(userMessage);
            String responseContent = response.aiMessage().text();
            List<Double> scores = parseScoresFromResponse(responseContent, textSegments.size());
            
            log.debug("完成批量打分，获得分数: {}", scores);
            return Response.from(scores);
            
        } catch (Exception e) {
            log.error("LLM打分过程发生错误，使用降级分数", e);
            List<Double> fallbackScores = createFallbackScores(textSegments.size());
            return Response.from(fallbackScores);
        }
    }
    
    /**
     * 顺序处理多个批次
     */
    private Response<List<Double>> processBatchesSequentially(List<TextSegment> textSegments, String query) {
        log.info("大批量数据分批处理，总数量: {}, 每批最大: {}", textSegments.size(), MAX_BATCH_SIZE);
        
        List<Double> allScores = new ArrayList<>();
        int totalTokens = 0;
        
        for (int i = 0; i < textSegments.size(); i += MAX_BATCH_SIZE) {
            int endIndex = Math.min(i + MAX_BATCH_SIZE, textSegments.size());
            List<TextSegment> batch = textSegments.subList(i, endIndex);
            
            Response<List<Double>> batchResponse = processBatch(batch, query);
            allScores.addAll(batchResponse.content());
            
            if (batchResponse.tokenUsage() != null) {
                totalTokens += batchResponse.tokenUsage().totalTokenCount();
            }
        }
        
        log.info("完成分批处理，总分数数量: {}, 总Token消耗: {}", allScores.size(), totalTokens);
        return Response.from(allScores);
    }
    
    /**
     * 构建用于打分的提示模板
     */
    private String buildScoringPrompt(List<TextSegment> textSegments, String query) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("你是一个专业的文本相关性评估专家。请对以下文本段落与给定查询的相关性进行打分。\n\n");
        prompt.append("查询: ").append(query).append("\n\n");
        prompt.append("评分标准:\n");
        prompt.append("- 0.9-1.0: 高度相关，完美匹配查询意图\n");
        prompt.append("- 0.7-0.8: 中度相关，部分匹配查询内容\n");
        prompt.append("- 0.5-0.6: 低度相关，存在一定关联\n");
        prompt.append("- 0.0-0.4: 不相关或几乎无关联\n\n");
        
        prompt.append("请为以下").append(textSegments.size()).append("个文本段落评分（只返回数字分数，用空格分隔）:\n\n");
        
        for (int i = 0; i < textSegments.size(); i++) {
            prompt.append("段落").append(i + 1).append(": ")
                  .append(textSegments.get(i).text())
                  .append("\n\n");
        }
        
        prompt.append("请按顺序返回").append(textSegments.size()).append("个分数（0.0-1.0之间的小数，用空格分隔）:");
        
        return prompt.toString();
    }
    
    /**
     * 从LLM响应中解析分数列表
     */
    private List<Double> parseScoresFromResponse(String response, int expectedCount) {
        List<Double> scores = new ArrayList<>();
        
        if (response == null || response.trim().isEmpty()) {
            log.warn("LLM响应为空，使用降级分数");
            return createFallbackScores(expectedCount);
        }
        
        Matcher matcher = SCORE_PATTERN.matcher(response);
        
        while (matcher.find() && scores.size() < expectedCount) {
            try {
                double score = Double.parseDouble(matcher.group(1));
                // 确保分数在合理范围内
                score = Math.max(0.0, Math.min(1.0, score));
                scores.add(score);
            } catch (NumberFormatException e) {
                log.warn("解析分数失败: {}", matcher.group(1));
            }
        }
        
        // 如果解析的分数不够，用降级分数补足
        while (scores.size() < expectedCount) {
            log.warn("解析分数不足，使用降级分数补足。预期: {}, 实际: {}", expectedCount, scores.size());
            scores.add(DEFAULT_FALLBACK_SCORE);
        }
        
        log.debug("成功解析分数: {}", scores);
        return scores;
    }
    
    /**
     * 创建降级分数列表
     */
    private List<Double> createFallbackScores(int count) {
        List<Double> scores = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            scores.add(DEFAULT_FALLBACK_SCORE);
        }
        return scores;
    }
}