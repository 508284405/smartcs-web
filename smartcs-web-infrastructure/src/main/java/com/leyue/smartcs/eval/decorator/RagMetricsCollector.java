package com.leyue.smartcs.eval.decorator;

import com.leyue.smartcs.dto.eval.event.RagEvent;
import dev.langchain4j.rag.content.Content;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

/**
 * RAG指标收集器
 * 负责收集RAG管道各个阶段的指标数据
 */
@Slf4j
@Component
public class RagMetricsCollector {
    
    // 存储正在进行的RAG事件，key为traceId或eventId
    private final ConcurrentMap<String, RagEventBuilder> activeEvents = new ConcurrentHashMap<>();
    
    // 计数器
    private final AtomicLong eventIdCounter = new AtomicLong(0);
    
    /**
     * 开始一个新的RAG事件跟踪
     */
    public String startEvent(String traceId, String userId, String question) {
        String eventId = generateEventId();
        String key = traceId != null ? traceId : eventId;
        
        RagEventBuilder builder = new RagEventBuilder()
                .eventId(eventId)
                .traceId(traceId)
                .userId(userId)
                .question(question)
                .timestamp(System.currentTimeMillis());
        
        activeEvents.put(key, builder);
        
        log.debug("开始RAG事件跟踪: eventId={}, traceId={}, key={}", eventId, traceId, key);
        return eventId;
    }
    
    /**
     * 记录检索指标
     */
    public void recordRetrieval(String queryText, List<Content> contents, long durationMs) {
        // 根据当前线程或请求上下文找到对应的事件
        String key = findActiveEventKey();
        if (key == null) {
            log.debug("未找到活跃的RAG事件，跳过检索指标记录");
            return;
        }
        
        RagEventBuilder builder = activeEvents.get(key);
        if (builder != null) {
            // 转换Content列表为RetrievedContext列表
            List<RagEvent.RetrievedContext> contexts = IntStream.range(0, contents.size())
                    .mapToObj(i -> {
                        Content content = contents.get(i);
                        return RagEvent.RetrievedContext.builder()
                                .text(content.textSegment().text())
                                .rank(i + 1)
                                .source(extractSource(content))
                                .docId(extractDocId(content))
                                .chunkId(extractChunkId(content))
                                .score(extractScore(content))
                                .build();
                    })
                    .toList();
            
            builder.addRetrievedContexts(contexts);
            builder.addRetrievalLatency(durationMs);
            
            log.debug("记录检索指标: key={}, contextCount={}, duration={}ms", key, contexts.size(), durationMs);
        }
    }
    
    /**
     * 记录检索错误
     */
    public void recordRetrievalError(String queryText, Exception error, long durationMs) {
        String key = findActiveEventKey();
        if (key == null) {
            return;
        }
        
        RagEventBuilder builder = activeEvents.get(key);
        if (builder != null) {
            builder.addRetrievalLatency(durationMs);
            log.warn("记录检索错误: key={}, duration={}ms, error={}", key, durationMs, error.getMessage());
        }
    }
    
    /**
     * 记录生成指标
     */
    public void recordGeneration(String answer, String modelName, Double temperature, String provider, long durationMs) {
        String key = findActiveEventKey();
        if (key == null) {
            log.debug("未找到活跃的RAG事件，跳过生成指标记录");
            return;
        }
        
        RagEventBuilder builder = activeEvents.get(key);
        if (builder != null) {
            RagEvent.LlmConfig llmConfig = RagEvent.LlmConfig.builder()
                    .model(modelName)
                    .temperature(temperature)
                    .provider(provider)
                    .build();
            
            builder.answer(answer)
                   .llmConfig(llmConfig)
                   .addGenerationLatency(durationMs);
            
            log.debug("记录生成指标: key={}, answerLength={}, duration={}ms", 
                    key, answer != null ? answer.length() : 0, durationMs);
        }
    }
    
    /**
     * 记录生成错误
     */
    public void recordGenerationError(Exception error, long durationMs) {
        String key = findActiveEventKey();
        if (key == null) {
            return;
        }
        
        RagEventBuilder builder = activeEvents.get(key);
        if (builder != null) {
            builder.addGenerationLatency(durationMs);
            log.warn("记录生成错误: key={}, duration={}ms, error={}", key, durationMs, error.getMessage());
        }
    }
    
    /**
     * 完成事件并返回构建的RagEvent
     */
    public RagEvent finishEvent(String eventId) {
        String key = findEventKeyByEventId(eventId);
        if (key == null) {
            log.warn("未找到事件ID对应的活跃事件: eventId={}", eventId);
            return null;
        }
        
        RagEventBuilder builder = activeEvents.remove(key);
        if (builder == null) {
            log.warn("事件已不存在: eventId={}, key={}", eventId, key);
            return null;
        }
        
        RagEvent event = builder.build();
        log.debug("完成RAG事件: eventId={}, key={}, totalLatency={}ms", 
                eventId, key, event.getLatencyMs());
        return event;
    }
    
    /**
     * 生成事件ID
     */
    private String generateEventId() {
        long counter = eventIdCounter.incrementAndGet();
        return "rag_event_" + System.currentTimeMillis() + "_" + counter;
    }
    
    /**
     * 查找当前活跃事件的key
     * 这里简化实现，在实际使用中可能需要更复杂的线程上下文管理
     */
    private String findActiveEventKey() {
        // 简化实现：返回最近创建的事件key
        // 在实际实现中，应该通过ThreadLocal或请求上下文来管理
        return activeEvents.keySet().stream().findFirst().orElse(null);
    }
    
    /**
     * 根据事件ID查找key
     */
    private String findEventKeyByEventId(String eventId) {
        return activeEvents.entrySet().stream()
                .filter(entry -> eventId.equals(entry.getValue().getEventId()))
                .map(entry -> entry.getKey())
                .findFirst()
                .orElse(null);
    }
    
    // 以下是从Content中提取元数据的辅助方法
    private String extractSource(Content content) {
        // 从Content的metadata中提取source信息
        return content.textSegment().metadata().getString("source");
    }
    
    private String extractDocId(Content content) {
        // 从Content的metadata中提取docId信息
        return content.textSegment().metadata().getString("docId");
    }
    
    private String extractChunkId(Content content) {
        // 从Content的metadata中提取chunkId信息
        return content.textSegment().metadata().getString("chunkId");
    }
    
    private Double extractScore(Content content) {
        // 从Content中提取相似度分数
        try {
            String scoreStr = content.textSegment().metadata().getString("score");
            return scoreStr != null ? Double.parseDouble(scoreStr) : null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * RAG事件构建器
     */
    private static class RagEventBuilder {
        private String eventId;
        private String traceId;
        private String userId;
        private String question;
        private String answer;
        private Long timestamp;
        private RagEvent.LlmConfig llmConfig;
        private List<RagEvent.RetrievedContext> retrievedContexts;
        private long totalRetrievalLatency = 0;
        private long totalGenerationLatency = 0;
        
        public RagEventBuilder eventId(String eventId) {
            this.eventId = eventId;
            return this;
        }
        
        public RagEventBuilder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }
        
        public RagEventBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }
        
        public RagEventBuilder question(String question) {
            this.question = question;
            return this;
        }
        
        public RagEventBuilder answer(String answer) {
            this.answer = answer;
            return this;
        }
        
        public RagEventBuilder timestamp(Long timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public RagEventBuilder llmConfig(RagEvent.LlmConfig llmConfig) {
            this.llmConfig = llmConfig;
            return this;
        }
        
        public RagEventBuilder addRetrievedContexts(List<RagEvent.RetrievedContext> contexts) {
            if (this.retrievedContexts == null) {
                this.retrievedContexts = new java.util.ArrayList<>();
            }
            this.retrievedContexts.addAll(contexts);
            return this;
        }
        
        public RagEventBuilder addRetrievalLatency(long latency) {
            this.totalRetrievalLatency += latency;
            return this;
        }
        
        public RagEventBuilder addGenerationLatency(long latency) {
            this.totalGenerationLatency += latency;
            return this;
        }
        
        public String getEventId() {
            return eventId;
        }
        
        public RagEvent build() {
            return RagEvent.builder()
                    .eventId(eventId)
                    .traceId(traceId)
                    .ts(timestamp)
                    .userId(userId)
                    .question(question)
                    .answer(answer)
                    .retrievedContexts(retrievedContexts != null ? retrievedContexts : List.of())
                    .llm(llmConfig)
                    .latencyMs(totalRetrievalLatency + totalGenerationLatency)
                    .app(RagEvent.AppInfo.builder()
                            .service("smartcs-web")
                            .version("1.0.0-SNAPSHOT")
                            .build())
                    .build();
        }
    }
}