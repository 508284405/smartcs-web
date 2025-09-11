package com.leyue.smartcs.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;

/**
 * 熔断器监控配置
 * 配置Micrometer指标收集和Prometheus监控
 */
@Configuration
@Slf4j
public class CircuitBreakerMetricsConfig {

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private RetryRegistry retryRegistry;

    @Autowired
    private BulkheadRegistry bulkheadRegistry;

    @Autowired
    private TimeLimiterRegistry timeLimiterRegistry;

    /**
     * 配置熔断器指标收集
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> circuitBreakerMetrics() {
        return registry -> {
            // 注册熔断器指标
            circuitBreakerRegistry.getAllCircuitBreakers().forEach(circuitBreaker -> {
                CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();
                
                // 熔断器状态指标
                registry.gauge("resilience4j.circuitbreaker.state", 
                    circuitBreaker, 
                    cb -> cb.getState().getOrder());
                
                // 失败率指标
                registry.gauge("resilience4j.circuitbreaker.failure_rate", 
                    circuitBreaker, 
                    cb -> cb.getMetrics().getFailureRate());
                
                // 慢调用率指标
                registry.gauge("resilience4j.circuitbreaker.slow_call_rate", 
                    circuitBreaker, 
                    cb -> cb.getMetrics().getSlowCallRate());
                
                // 总调用次数（成功+失败）
                registry.gauge("resilience4j.circuitbreaker.total_calls", 
                    circuitBreaker, 
                    cb -> cb.getMetrics().getNumberOfSuccessfulCalls() + cb.getMetrics().getNumberOfFailedCalls());
                
                // 失败调用次数
                registry.gauge("resilience4j.circuitbreaker.failed_calls", 
                    circuitBreaker, 
                    cb -> cb.getMetrics().getNumberOfFailedCalls());
                
                // 成功调用次数
                registry.gauge("resilience4j.circuitbreaker.successful_calls", 
                    circuitBreaker, 
                    cb -> cb.getMetrics().getNumberOfSuccessfulCalls());
                
                // 慢调用次数
                registry.gauge("resilience4j.circuitbreaker.slow_calls", 
                    circuitBreaker, 
                    cb -> cb.getMetrics().getNumberOfSlowCalls());
                
                // 被拒绝的调用次数
                registry.gauge("resilience4j.circuitbreaker.not_permitted_calls", 
                    circuitBreaker, 
                    cb -> cb.getMetrics().getNumberOfNotPermittedCalls());
            });

            // 注册重试指标
            retryRegistry.getAllRetries().forEach(retry -> {
                Retry.Metrics metrics = retry.getMetrics();
                
                // 重试次数
                registry.gauge("resilience4j.retry.calls", 
                    retry, 
                    r -> r.getMetrics().getNumberOfSuccessfulCallsWithoutRetryAttempt());
                
                // 重试失败次数
                registry.gauge("resilience4j.retry.failed_calls", 
                    retry, 
                    r -> r.getMetrics().getNumberOfFailedCallsWithRetryAttempt());
                
                // 重试成功次数
                registry.gauge("resilience4j.retry.successful_calls", 
                    retry, 
                    r -> r.getMetrics().getNumberOfSuccessfulCallsWithRetryAttempt());
            });

            // 注册限流指标
            bulkheadRegistry.getAllBulkheads().forEach(bulkhead -> {
                Bulkhead.Metrics metrics = bulkhead.getMetrics();
                
                // 可用并发数
                registry.gauge("resilience4j.bulkhead.available_concurrent_calls", 
                    bulkhead, 
                    b -> b.getMetrics().getAvailableConcurrentCalls());
                
                // 最大并发数
                registry.gauge("resilience4j.bulkhead.max_allowed_concurrent_calls", 
                    bulkhead, 
                    b -> b.getMetrics().getMaxAllowedConcurrentCalls());
            });

            // TimeLimiter没有Metrics接口，移除相关监控代码
            // 如果需要监控TimeLimiter，可以通过其他方式实现

            log.info("熔断器监控指标配置完成");
        };
    }

    /**
     * 配置应用标签
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
                .commonTags("application", "smartcs-web")
                .commonTags("component", "circuit-breaker");
    }
} 