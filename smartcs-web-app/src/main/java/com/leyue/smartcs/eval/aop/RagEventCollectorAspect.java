package com.leyue.smartcs.eval.aop;

import com.leyue.smartcs.dto.eval.event.RagEvent;
import com.leyue.smartcs.eval.producer.RagEventProducer;
import com.leyue.smartcs.eval.sampling.SamplingDecider;
import com.leyue.smartcs.eval.trace.SkyWalkingTraceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * RAG评估事件收集切面
 * 拦截聊天执行流程，收集RAG管道的完整指标数据
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "eval.enabled", havingValue = "true", matchIfMissing = true)
public class RagEventCollectorAspect {
    
    private final SamplingDecider samplingDecider;
    private final RagEventProducer ragEventProducer;
    private final SkyWalkingTraceContext traceContext;
    
    // 存储正在进行的RAG事件上下文
    private final ConcurrentMap<String, RagEventContext> activeContexts = new ConcurrentHashMap<>();
    
    /**
     * 拦截聊天命令执行
     * 主要拦截点：AiAppChatCmdExe.execute方法
     */
    @Around("execution(* com.leyue.smartcs.app.executor.AiAppChatCmdExe.execute(..)) || " +
            "execution(* com.leyue.smartcs.app.executor.AiAppChatCmdExe.processChatStream(..))")
    public Object collectChatExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        // 提取执行上下文信息
        String traceId = getTraceId();
        String userId = extractUserId(joinPoint);
        String question = extractQuestion(joinPoint);
        
        // 采样决策
        boolean shouldSample = samplingDecider.shouldSample(traceId, userId);
        if (!shouldSample) {
            log.debug("跳过RAG事件采集（未命中采样）: traceId={}, userId={}", traceId, userId);
            return joinPoint.proceed();
        }
        
        // 创建事件上下文
        String eventId = generateEventId();
        RagEventContext context = new RagEventContext(eventId, traceId, userId, question);
        activeContexts.put(eventId, context);
        
        // 添加SkyWalking标签
        traceContext.addEvaluationTags(eventId, userId, true);
        
        // 设置到线程上下文
        RagEventContextHolder.setContext(context);
        
        log.debug("开始RAG事件采集: eventId={}, traceId={}, question={}", eventId, traceId, question);
        
        long startTime = System.currentTimeMillis();
        try {
            // 执行原方法
            Object result = joinPoint.proceed();
            
            // 完成事件采集
            long duration = System.currentTimeMillis() - startTime;
            finishEventCollection(eventId, result, duration, null);
            
            return result;
            
        } catch (Throwable ex) {
            // 错误情况下也要完成事件采集
            long duration = System.currentTimeMillis() - startTime;
            finishEventCollection(eventId, null, duration, ex);
            throw ex;
            
        } finally {
            // 清理上下文
            activeContexts.remove(eventId);
            RagEventContextHolder.clear();
        }
    }
    
    /**
     * 拦截检索执行
     * 拦截ContentRetriever的retrieve方法调用
     */
    @Around("execution(* dev.langchain4j.rag.content.retriever.*.retrieve(..))")
    public Object collectRetrieval(ProceedingJoinPoint joinPoint) throws Throwable {
        RagEventContext context = RagEventContextHolder.getContext();
        if (context == null) {
            // 没有活跃的RAG事件上下文，直接执行
            return joinPoint.proceed();
        }
        
        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            // 记录检索指标
            context.addRetrievalMetrics(result, duration);
            
            log.debug("记录检索指标: eventId={}, duration={}ms", context.getEventId(), duration);
            return result;
            
        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - startTime;
            context.addRetrievalError(ex, duration);
            log.warn("记录检索错误: eventId={}, duration={}ms", context.getEventId(), duration);
            throw ex;
        }
    }
    
    /**
     * 完成事件收集并发送
     */
    private void finishEventCollection(String eventId, Object result, long duration, Throwable error) {
        RagEventContext context = activeContexts.get(eventId);
        if (context == null) {
            log.warn("未找到RAG事件上下文: eventId={}", eventId);
            return;
        }
        
        try {
            // 从结果中提取答案
            String answer = extractAnswer(result, error);
            
            // 构建RAG事件
            RagEvent event = context.buildEvent(answer, duration, error);
            
            // 异步发送事件
            ragEventProducer.sendAsync(event);
            
            log.debug("RAG事件采集完成: eventId={}, duration={}ms, hasError={}", 
                    eventId, duration, error != null);
            
        } catch (Exception e) {
            log.error("完成RAG事件采集失败: eventId={}, error={}", eventId, e.getMessage(), e);
        }
    }
    
    /**
     * 获取链路追踪ID
     */
    private String getTraceId() {
        return traceContext.getTraceId();
    }
    
    /**
     * 从连接点参数中提取用户ID
     */
    private String extractUserId(ProceedingJoinPoint joinPoint) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getHeader("user-id");
            }
        } catch (Exception e) {
            log.debug("提取用户ID失败", e);
        }
        return "anonymous";
    }
    
    /**
     * 从连接点参数中提取问题内容
     */
    private String extractQuestion(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args != null && args.length > 0) {
            // 假设第一个参数是命令对象，包含问题信息
            try {
                Object cmd = args[0];
                if (cmd != null) {
                    // 使用反射获取message或question字段
                    java.lang.reflect.Field[] fields = cmd.getClass().getDeclaredFields();
                    for (java.lang.reflect.Field field : fields) {
                        field.setAccessible(true);
                        if ("message".equals(field.getName()) || "question".equals(field.getName()) || "content".equals(field.getName())) {
                            Object value = field.get(cmd);
                            return value != null ? value.toString() : "";
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("提取问题内容失败", e);
            }
        }
        return "";
    }
    
    /**
     * 从执行结果中提取答案
     */
    private String extractAnswer(Object result, Throwable error) {
        if (error != null) {
            return "执行失败: " + error.getMessage();
        }
        
        if (result == null) {
            return "";
        }
        
        // 根据结果类型提取答案内容
        try {
            // 如果是响应对象，尝试获取其中的消息内容
            java.lang.reflect.Method[] methods = result.getClass().getDeclaredMethods();
            for (java.lang.reflect.Method method : methods) {
                if ("getMessage".equals(method.getName()) || "getContent".equals(method.getName()) || "getAnswer".equals(method.getName())) {
                    method.setAccessible(true);
                    Object value = method.invoke(result);
                    return value != null ? value.toString() : "";
                }
            }
        } catch (Exception e) {
            log.debug("提取答案内容失败", e);
        }
        
        return result.toString();
    }
    
    /**
     * 生成事件ID
     */
    private String generateEventId() {
        return "rag_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * RAG事件上下文
     */
    private static class RagEventContext {
        private final String eventId;
        private final String traceId;
        private final String userId;
        private final String question;
        private final long startTime;
        private long totalRetrievalDuration = 0;
        private int retrievalCount = 0;
        private int retrievalErrorCount = 0;
        
        public RagEventContext(String eventId, String traceId, String userId, String question) {
            this.eventId = eventId;
            this.traceId = traceId;
            this.userId = userId;
            this.question = question;
            this.startTime = System.currentTimeMillis();
        }
        
        public String getEventId() {
            return eventId;
        }
        
        public void addRetrievalMetrics(Object result, long duration) {
            totalRetrievalDuration += duration;
            retrievalCount++;
        }
        
        public void addRetrievalError(Throwable error, long duration) {
            totalRetrievalDuration += duration;
            retrievalErrorCount++;
        }
        
        public RagEvent buildEvent(String answer, long totalDuration, Throwable error) {
            return RagEvent.builder()
                    .eventId(eventId)
                    .traceId(traceId)
                    .ts(startTime)
                    .userId(userId)
                    .question(question)
                    .answer(answer != null ? answer : "")
                    .latencyMs(totalDuration)
                    .app(RagEvent.AppInfo.builder()
                            .service("smartcs-web")
                            .version("1.0.0-SNAPSHOT")
                            .build())
                    .build();
        }
    }
    
    /**
     * RAG事件上下文持有者
     */
    public static class RagEventContextHolder {
        private static final ThreadLocal<RagEventContext> contextHolder = new ThreadLocal<>();
        
        public static void setContext(RagEventContext context) {
            contextHolder.set(context);
        }
        
        public static RagEventContext getContext() {
            return contextHolder.get();
        }
        
        public static void clear() {
            contextHolder.remove();
        }
    }
}