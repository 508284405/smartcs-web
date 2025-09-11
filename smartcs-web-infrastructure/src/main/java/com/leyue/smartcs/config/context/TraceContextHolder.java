package com.leyue.smartcs.config.context;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * 追踪上下文持有者
 * 用于管理请求级别的traceId，支持SkyWalking和UUID备用方案
 */
@Slf4j
@Component
public class TraceContextHolder {
    
    /**
     * MDC中存储traceId的key
     */
    public static final String TRACE_ID_KEY = "traceId";
    
    /**
     * 跨服务传递traceId的HTTP请求头
     */
    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    
    /**
     * MDC中存储spanId的key
     */
    public static final String SPAN_ID_KEY = "spanId";
    
    /**
     * SkyWalking TraceContext类名
     */
    private static final String SKYWALKING_TRACE_CONTEXT_CLASS = "org.apache.skywalking.apm.toolkit.trace.TraceContext";
    
    /**
     * SkyWalking是否可用
     */
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
     * 初始化追踪上下文
     * 生成或获取traceId并存储到MDC中
     * 
     * @return 当前请求的traceId
     */
    public String initTraceContext() {
        String traceId = getCurrentTraceId();
        
        if (traceId == null || traceId.isEmpty()) {
            traceId = generateTraceId();
        }
        
        // 存储到MDC
        MDC.put(TRACE_ID_KEY, traceId);
        
        // 如果SkyWalking可用，尝试获取spanId
        if (SKYWALKING_AVAILABLE) {
            String spanId = getSkyWalkingSpanId();
            if (spanId != null && !spanId.isEmpty()) {
                MDC.put(SPAN_ID_KEY, spanId);
            }
        }
        
        log.debug("初始化追踪上下文: traceId={}", traceId);
        return traceId;
    }
    
    /**
     * 获取当前的traceId
     * 优先级：MDC > SkyWalking
     * 
     * @return 当前的traceId，可能为null
     */
    public String getCurrentTraceId() {
        // 从MDC获取
        String traceId = MDC.get(TRACE_ID_KEY);
        if (traceId != null && !traceId.isEmpty()) {
            return traceId;
        }
        
        // 尝试从SkyWalking获取
        if (SKYWALKING_AVAILABLE) {
            try {
                traceId = getSkyWalkingTraceId();
                if (traceId != null && !traceId.isEmpty()) {
                    return traceId;
                }
            } catch (Exception e) {
                log.debug("从SkyWalking获取traceId失败: {}", e.getMessage());
            }
        }
        
        return null;
    }
    
    /**
     * 获取当前的spanId
     * 优先级：MDC > SkyWalking
     * 
     * @return 当前的spanId，可能为null
     */
    public String getCurrentSpanId() {
        // 从MDC获取
        String spanId = MDC.get(SPAN_ID_KEY);
        if (spanId != null && !spanId.isEmpty()) {
            return spanId;
        }
        
        // 尝试从SkyWalking获取
        if (SKYWALKING_AVAILABLE) {
            try {
                return getSkyWalkingSpanId();
            } catch (Exception e) {
                log.debug("从SkyWalking获取spanId失败: {}", e.getMessage());
            }
        }
        
        return null;
    }
    
    /**
     * 设置traceId到MDC
     * 
     * @param traceId 要设置的traceId
     */
    public void setTraceId(String traceId) {
        if (traceId != null && !traceId.isEmpty()) {
            MDC.put(TRACE_ID_KEY, traceId);
            log.debug("设置traceId到MDC: {}", traceId);
        }
    }
    
    /**
     * 设置spanId到MDC
     * 
     * @param spanId 要设置的spanId
     */
    public void setSpanId(String spanId) {
        if (spanId != null && !spanId.isEmpty()) {
            MDC.put(SPAN_ID_KEY, spanId);
            log.debug("设置spanId到MDC: {}", spanId);
        }
    }
    
    /**
     * 清理追踪上下文
     * 从MDC中移除traceId和spanId，防止内存泄漏
     */
    public void clearTraceContext() {
        String traceId = MDC.get(TRACE_ID_KEY);
        
        MDC.remove(TRACE_ID_KEY);
        MDC.remove(SPAN_ID_KEY);
        
        if (traceId != null) {
            log.debug("清理追踪上下文: traceId={}", traceId);
        }
    }
    
    /**
     * 生成新的traceId
     * 
     * @return 新生成的traceId
     */
    private String generateTraceId() {
        // 优先尝试从SkyWalking获取
        if (SKYWALKING_AVAILABLE) {
            try {
                String skyWalkingTraceId = getSkyWalkingTraceId();
                if (skyWalkingTraceId != null && !skyWalkingTraceId.isEmpty()) {
                    return skyWalkingTraceId;
                }
            } catch (Exception e) {
                log.debug("从SkyWalking生成traceId失败，使用UUID替代: {}", e.getMessage());
            }
        }
        
        // 回退到UUID生成
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 检查SkyWalking是否可用
     * 
     * @return true如果SkyWalking可用
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
            
            log.debug("SkyWalking trace ID为空");
            return null;
            
        } catch (Exception e) {
            log.debug("反射调用SkyWalking TraceContext失败: {}", e.getMessage());
            throw new RuntimeException("SkyWalking trace ID获取失败", e);
        }
    }
    
    /**
     * 获取SkyWalking的span ID
     */
    private String getSkyWalkingSpanId() {
        try {
            Class<?> traceContextClass = Class.forName(SKYWALKING_TRACE_CONTEXT_CLASS);
            java.lang.reflect.Method spanIdMethod = traceContextClass.getMethod("segmentId");
            Object spanId = spanIdMethod.invoke(null);
            
            if (spanId != null && !spanId.toString().isEmpty()) {
                return spanId.toString();
            }
            return null;
        } catch (Exception e) {
            log.debug("获取SkyWalking span ID失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 为评估相关操作添加标签
     * 
     * @param eventId 事件ID
     * @param userId 用户ID
     * @param sampled 是否采样
     */
    public void addEvaluationTags(String eventId, String userId, boolean sampled) {
        if (SKYWALKING_AVAILABLE) {
            try {
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
     * 获取完整的追踪信息
     * 
     * @return 追踪信息字符串，格式为 "traceId[,spanId]"
     */
    public String getFullTraceInfo() {
        String traceId = getCurrentTraceId();
        String spanId = getCurrentSpanId();
        
        if (traceId == null) {
            return "N/A";
        }
        
        if (spanId != null && !spanId.isEmpty()) {
            return traceId + "," + spanId;
        }
        
        return traceId;
    }
    
    /**
     * 包装Runnable，确保在子线程中传递MDC上下文
     * 
     * @param runnable 要执行的任务
     * @return 包装后的Runnable
     */
    public static Runnable wrapWithMDC(Runnable runnable) {
        // 获取当前线程的MDC上下文
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        
        return () -> {
            if (contextMap != null) {
                // 在子线程中设置MDC上下文
                MDC.setContextMap(contextMap);
            }
            try {
                runnable.run();
            } finally {
                // 清理子线程的MDC上下文
                MDC.clear();
            }
        };
    }
    
    /**
     * 包装Callable，确保在子线程中传递MDC上下文
     * 
     * @param callable 要执行的任务
     * @return 包装后的Callable
     */
    public static <T> Callable<T> wrapWithMDC(Callable<T> callable) {
        // 获取当前线程的MDC上下文
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        
        return () -> {
            if (contextMap != null) {
                // 在子线程中设置MDC上下文
                MDC.setContextMap(contextMap);
            }
            try {
                return callable.call();
            } finally {
                // 清理子线程的MDC上下文
                MDC.clear();
            }
        };
    }
    
    /**
     * 在子线程中执行任务，自动传递traceId
     * 
     * @param runnable 要执行的任务
     */
    public void executeInNewThread(Runnable runnable) {
        Thread thread = new Thread(wrapWithMDC(runnable));
        thread.setName("TraceTask-" + Thread.currentThread().getName());
        thread.start();
    }
    
    /**
     * 为当前线程手动设置完整的MDC上下文
     * 用于在某些特殊情况下手动传递上下文
     * 
     * @param contextMap MDC上下文映射
     */
    public static void setMDCContext(Map<String, String> contextMap) {
        if (contextMap != null) {
            MDC.setContextMap(contextMap);
        }
    }
    
    /**
     * 获取当前的MDC上下文副本
     * 
     * @return MDC上下文映射的副本
     */
    public static Map<String, String> getMDCContext() {
        return MDC.getCopyOfContextMap();
    }
}
