package com.leyue.smartcs.eval.trace;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * SkyWalking链路追踪上下文集成
 * 提供统一的trace ID获取接口
 */
@Slf4j
@Component
public class SkyWalkingTraceContext {
    
    private static final String SKYWALKING_TRACE_CONTEXT_CLASS = "org.apache.skywalking.apm.toolkit.trace.TraceContext";
    private static final boolean SKYWALKING_AVAILABLE;
    
    static {
        boolean available = false;
        try {
            Class.forName(SKYWALKING_TRACE_CONTEXT_CLASS);
            available = true;
            log.info("SkyWalking TraceContext可用，将使用SkyWalking追踪ID");
        } catch (ClassNotFoundException e) {
            log.info("SkyWalking TraceContext不可用，将使用UUID作为追踪ID");
        }
        SKYWALKING_AVAILABLE = available;
    }
    
    /**
     * 获取当前的trace ID
     * 优先使用SkyWalking的trace ID，如果不可用则生成UUID
     */
    public String getTraceId() {
        if (SKYWALKING_AVAILABLE) {
            try {
                return getSkyWalkingTraceId();
            } catch (Exception e) {
                log.debug("获取SkyWalking trace ID失败，回退到UUID: {}", e.getMessage());
            }
        }
        
        // 回退到UUID
        return generateUuidTraceId();
    }
    
    /**
     * 检查SkyWalking是否可用
     */
    public boolean isSkyWalkingAvailable() {
        return SKYWALKING_AVAILABLE;
    }
    
    /**
     * 获取SkyWalking的trace ID
     */
    private String getSkyWalkingTraceId() {
        try {
            // 使用反射调用SkyWalking的TraceContext.traceId()方法
            Class<?> traceContextClass = Class.forName(SKYWALKING_TRACE_CONTEXT_CLASS);
            java.lang.reflect.Method traceIdMethod = traceContextClass.getMethod("traceId");
            Object traceId = traceIdMethod.invoke(null);
            
            if (traceId != null && !traceId.toString().isEmpty()) {
                return traceId.toString();
            }
            
            log.debug("SkyWalking trace ID为空，使用UUID替代");
            return generateUuidTraceId();
            
        } catch (Exception e) {
            log.debug("反射调用SkyWalking TraceContext失败: {}", e.getMessage());
            throw new RuntimeException("SkyWalking trace ID获取失败", e);
        }
    }
    
    /**
     * 生成UUID作为trace ID
     */
    private String generateUuidTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 获取当前span ID (如果SkyWalking可用)
     */
    public String getSpanId() {
        if (SKYWALKING_AVAILABLE) {
            try {
                Class<?> traceContextClass = Class.forName(SKYWALKING_TRACE_CONTEXT_CLASS);
                java.lang.reflect.Method spanIdMethod = traceContextClass.getMethod("segmentId");
                Object spanId = spanIdMethod.invoke(null);
                
                if (spanId != null && !spanId.toString().isEmpty()) {
                    return spanId.toString();
                }
            } catch (Exception e) {
                log.debug("获取SkyWalking span ID失败: {}", e.getMessage());
            }
        }
        
        return null;
    }
    
    /**
     * 为RAG评估创建自定义span标签
     * 如果SkyWalking可用，添加评估相关的标签
     */
    public void addEvaluationTags(String eventId, String userId, boolean sampled) {
        if (SKYWALKING_AVAILABLE) {
            try {
                Class<?> tagClass = Class.forName("org.apache.skywalking.apm.toolkit.trace.Tag");
                Class<?> tagsClass = Class.forName("org.apache.skywalking.apm.toolkit.trace.Tags");
                
                // 添加评估标签
                java.lang.reflect.Method tagMethod = tagsClass.getMethod("tag", String.class, String.class);
                tagMethod.invoke(null, "rag.eval.event_id", eventId);
                tagMethod.invoke(null, "rag.eval.sampled", String.valueOf(sampled));
                
                if (userId != null && !userId.isEmpty()) {
                    tagMethod.invoke(null, "rag.eval.user_id", userId);
                }
                
                log.debug("添加SkyWalking评估标签: eventId={}, sampled={}", eventId, sampled);
                
            } catch (Exception e) {
                log.debug("添加SkyWalking评估标签失败: {}", e.getMessage());
            }
        }
    }
    
    /**
     * 创建评估相关的自定义span
     */
    public void createEvaluationSpan(String eventId, String operation) {
        if (SKYWALKING_AVAILABLE) {
            try {
                Class<?> traceClass = Class.forName("org.apache.skywalking.apm.toolkit.trace.Trace");
                Class<?> tagClass = Class.forName("org.apache.skywalking.apm.toolkit.trace.Tag");
                
                // 这里需要在方法级别添加@Trace注解，暂时跳过
                log.debug("SkyWalking评估span创建: eventId={}, operation={}", eventId, operation);
                
            } catch (Exception e) {
                log.debug("创建SkyWalking评估span失败: {}", e.getMessage());
            }
        }
    }
}