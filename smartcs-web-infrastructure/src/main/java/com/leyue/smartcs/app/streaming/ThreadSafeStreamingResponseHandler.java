package com.leyue.smartcs.app.streaming;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

/**
 * 线程安全的流式响应处理器
 * 解决多个流式响应并发修改的问题
 */
@RequiredArgsConstructor
@Slf4j
public class ThreadSafeStreamingResponseHandler {

    private final String sessionId;
    private final String messageId;
    private final SseEmitter emitter;
    
    // 线程安全的响应内容构建器
    private final StringBuilder responseBuilder = new StringBuilder();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    // 状态控制
    private final AtomicBoolean completed = new AtomicBoolean(false);
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final AtomicLong tokenCount = new AtomicLong(0);
    private final AtomicLong lastUpdateTime = new AtomicLong(System.currentTimeMillis());
    
    // 回调函数
    private volatile Consumer<String> onTokenReceived;
    private volatile Consumer<String> onComplete;
    private volatile Consumer<Throwable> onError;
    
    // 全局处理器缓存，用于管理活跃的流式响应
    private static final ConcurrentHashMap<String, ThreadSafeStreamingResponseHandler> activeHandlers = 
            new ConcurrentHashMap<>();
    
    /**
     * 创建并注册处理器
     */
    public static ThreadSafeStreamingResponseHandler create(String sessionId, String messageId, SseEmitter emitter) {
        String handlerId = sessionId + ":" + messageId;
        ThreadSafeStreamingResponseHandler handler = new ThreadSafeStreamingResponseHandler(sessionId, messageId, emitter);
        
        // 注册到活跃处理器缓存
        activeHandlers.put(handlerId, handler);
        log.debug("创建流式响应处理器: sessionId={}, messageId={}", sessionId, messageId);
        
        return handler;
    }
    
    /**
     * 设置回调函数
     */
    public ThreadSafeStreamingResponseHandler onToken(Consumer<String> callback) {
        this.onTokenReceived = callback;
        return this;
    }
    
    public ThreadSafeStreamingResponseHandler onComplete(Consumer<String> callback) {
        this.onComplete = callback;
        return this;
    }
    
    public ThreadSafeStreamingResponseHandler onError(Consumer<Throwable> callback) {
        this.onError = callback;
        return this;
    }
    
    /**
     * 线程安全地追加token
     */
    public void appendToken(String token) {
        if (completed.get() || cancelled.get()) {
            log.warn("流式响应已完成或取消，忽略token: sessionId={}, messageId={}", sessionId, messageId);
            return;
        }
        
        lock.writeLock().lock();
        try {
            responseBuilder.append(token);
            tokenCount.incrementAndGet();
            lastUpdateTime.set(System.currentTimeMillis());
            
            // 调用token回调
            if (onTokenReceived != null) {
                try {
                    onTokenReceived.accept(token);
                } catch (Exception e) {
                    log.error("Token回调执行失败: sessionId={}, messageId={}", sessionId, messageId, e);
                }
            }
            
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 获取当前响应内容（线程安全）
     */
    public String getCurrentResponse() {
        lock.readLock().lock();
        try {
            return responseBuilder.toString();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 获取响应长度
     */
    public int getResponseLength() {
        lock.readLock().lock();
        try {
            return responseBuilder.length();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 完成流式响应
     */
    public void complete() {
        if (!completed.compareAndSet(false, true)) {
            log.warn("流式响应已经完成: sessionId={}, messageId={}", sessionId, messageId);
            return;
        }
        
        String finalResponse = getCurrentResponse();
        log.debug("流式响应完成: sessionId={}, messageId={}, tokenCount={}, responseLength={}", 
                 sessionId, messageId, tokenCount.get(), finalResponse.length());
        
        try {
            // 调用完成回调
            if (onComplete != null) {
                onComplete.accept(finalResponse);
            }
            
            // 完成SSE连接
            emitter.complete();
            
        } catch (Exception e) {
            log.error("完成流式响应时出错: sessionId={}, messageId={}", sessionId, messageId, e);
            handleError(e);
        } finally {
            cleanup();
        }
    }
    
    /**
     * 处理错误
     */
    public void handleError(Throwable error) {
        if (cancelled.compareAndSet(false, true)) {
            log.error("流式响应出错: sessionId={}, messageId={}, error={}", 
                     sessionId, messageId, error.getMessage(), error);
            
            try {
                // 调用错误回调
                if (onError != null) {
                    onError.accept(error);
                }
                
                // 完成SSE连接
                emitter.completeWithError(error);
                
            } catch (Exception e) {
                log.error("处理流式响应错误时出错: sessionId={}, messageId={}", sessionId, messageId, e);
            } finally {
                cleanup();
            }
        }
    }
    
    /**
     * 取消流式响应
     */
    public void cancel() {
        if (cancelled.compareAndSet(false, true)) {
            log.info("取消流式响应: sessionId={}, messageId={}", sessionId, messageId);
            
            try {
                emitter.complete();
            } catch (Exception e) {
                log.error("取消流式响应时出错: sessionId={}, messageId={}", sessionId, messageId, e);
            } finally {
                cleanup();
            }
        }
    }
    
    /**
     * 检查是否已完成
     */
    public boolean isCompleted() {
        return completed.get() || cancelled.get();
    }
    
    /**
     * 获取处理器状态
     */
    public HandlerStatus getStatus() {
        return new HandlerStatus(
            sessionId,
            messageId,
            getResponseLength(),
            tokenCount.get(),
            lastUpdateTime.get(),
            completed.get(),
            cancelled.get()
        );
    }
    
    /**
     * 清理资源
     */
    private void cleanup() {
        String handlerId = sessionId + ":" + messageId;
        activeHandlers.remove(handlerId);
        log.debug("清理流式响应处理器: sessionId={}, messageId={}", sessionId, messageId);
    }
    
    /**
     * 获取所有活跃的处理器
     */
    public static ConcurrentHashMap<String, ThreadSafeStreamingResponseHandler> getActiveHandlers() {
        return new ConcurrentHashMap<>(activeHandlers);
    }
    
    /**
     * 强制清理指定会话的所有处理器
     */
    public static void cleanupSession(String sessionId) {
        activeHandlers.entrySet().removeIf(entry -> {
            if (entry.getKey().startsWith(sessionId + ":")) {
                entry.getValue().cancel();
                log.info("强制清理会话处理器: sessionId={}, handlerId={}", sessionId, entry.getKey());
                return true;
            }
            return false;
        });
    }
    
    /**
     * 获取活跃处理器统计
     */
    public static HandlerStats getStats() {
        int totalActive = activeHandlers.size();
        int completedCount = 0;
        int cancelledCount = 0;
        
        for (ThreadSafeStreamingResponseHandler handler : activeHandlers.values()) {
            if (handler.completed.get()) {
                completedCount++;
            }
            if (handler.cancelled.get()) {
                cancelledCount++;
            }
        }
        
        return new HandlerStats(totalActive, completedCount, cancelledCount);
    }
    
    /**
     * 处理器状态信息
     */
    public static class HandlerStatus {
        public final String sessionId;
        public final String messageId;
        public final int responseLength;
        public final long tokenCount;
        public final long lastUpdateTime;
        public final boolean completed;
        public final boolean cancelled;
        
        public HandlerStatus(String sessionId, String messageId, int responseLength, 
                           long tokenCount, long lastUpdateTime, boolean completed, boolean cancelled) {
            this.sessionId = sessionId;
            this.messageId = messageId;
            this.responseLength = responseLength;
            this.tokenCount = tokenCount;
            this.lastUpdateTime = lastUpdateTime;
            this.completed = completed;
            this.cancelled = cancelled;
        }
        
        @Override
        public String toString() {
            return String.format("HandlerStatus{sessionId='%s', messageId='%s', responseLength=%d, " +
                               "tokenCount=%d, lastUpdateTime=%d, completed=%s, cancelled=%s}",
                               sessionId, messageId, responseLength, tokenCount, lastUpdateTime, completed, cancelled);
        }
    }
    
    /**
     * 处理器统计信息
     */
    public static class HandlerStats {
        public final int totalActive;
        public final int completedCount;
        public final int cancelledCount;
        
        public HandlerStats(int totalActive, int completedCount, int cancelledCount) {
            this.totalActive = totalActive;
            this.completedCount = completedCount;
            this.cancelledCount = cancelledCount;
        }
        
        @Override
        public String toString() {
            return String.format("HandlerStats{totalActive=%d, completed=%d, cancelled=%d}", 
                               totalActive, completedCount, cancelledCount);
        }
    }
}