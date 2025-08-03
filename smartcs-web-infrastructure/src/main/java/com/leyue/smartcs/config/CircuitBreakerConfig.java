package com.leyue.smartcs.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Resilience4j 熔断器配置
 * 提供熔断器、重试、限流、超时等容错组件的配置
 */
@Configuration
@Slf4j
public class CircuitBreakerConfig {

    /**
     * 熔断器注册表
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        io.github.resilience4j.circuitbreaker.CircuitBreakerConfig config = io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(2)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordExceptions(Exception.class)
                .build();

        return CircuitBreakerRegistry.of(config);
    }

    /**
     * 重试注册表
     */
    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(1))
                .retryExceptions(Exception.class)
                .build();

        return RetryRegistry.of(config);
    }

    /**
     * 限流注册表
     */
    @Bean
    public BulkheadRegistry bulkheadRegistry() {
        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(10)
                .maxWaitDuration(Duration.ofSeconds(3))
                .build();

        return BulkheadRegistry.of(config);
    }

    /**
     * 超时注册表
     */
    @Bean
    public TimeLimiterRegistry timeLimiterRegistry() {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(5))
                .cancelRunningFuture(true)
                .build();

        return TimeLimiterRegistry.of(config);
    }

    /**
     * Redis存储熔断器
     */
    @Bean("redis-memory-store")
    public CircuitBreaker redisMemoryStoreCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("redis-memory-store");
    }

    /**
     * ID生成器Feign熔断器
     */
    @Bean("id-generator-feign")
    public CircuitBreaker idGeneratorFeignCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("id-generator-feign");
    }

    /**
     * 用户中心Feign熔断器
     */
    @Bean("user-center-feign")
    public CircuitBreaker userCenterFeignCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("user-center-feign");
    }

    /**
     * 订单Feign熔断器
     */
    @Bean("order-feign")
    public CircuitBreaker orderFeignCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("order-feign");
    }

    /**
     * Redis存储重试
     */
    @Bean("redis-memory-store")
    public Retry redisMemoryStoreRetry(RetryRegistry registry) {
        return registry.retry("redis-memory-store");
    }

    /**
     * ID生成器Feign重试
     */
    @Bean("id-generator-feign")
    public Retry idGeneratorFeignRetry(RetryRegistry registry) {
        return registry.retry("id-generator-feign");
    }

    /**
     * 用户中心Feign重试
     */
    @Bean("user-center-feign")
    public Retry userCenterFeignRetry(RetryRegistry registry) {
        return registry.retry("user-center-feign");
    }

    /**
     * 订单Feign重试
     */
    @Bean("order-feign")
    public Retry orderFeignRetry(RetryRegistry registry) {
        return registry.retry("order-feign");
    }

    /**
     * Redis存储限流
     */
    @Bean("redis-memory-store")
    public Bulkhead redisMemoryStoreBulkhead(BulkheadRegistry registry) {
        return registry.bulkhead("redis-memory-store");
    }

    /**
     * ID生成器Feign限流
     */
    @Bean("id-generator-feign")
    public Bulkhead idGeneratorFeignBulkhead(BulkheadRegistry registry) {
        return registry.bulkhead("id-generator-feign");
    }

    /**
     * 用户中心Feign限流
     */
    @Bean("user-center-feign")
    public Bulkhead userCenterFeignBulkhead(BulkheadRegistry registry) {
        return registry.bulkhead("user-center-feign");
    }

    /**
     * 订单Feign限流
     */
    @Bean("order-feign")
    public Bulkhead orderFeignBulkhead(BulkheadRegistry registry) {
        return registry.bulkhead("order-feign");
    }

    /**
     * Redis存储超时
     */
    @Bean("redis-memory-store")
    public TimeLimiter redisMemoryStoreTimeLimiter(TimeLimiterRegistry registry) {
        return registry.timeLimiter("redis-memory-store");
    }

    /**
     * ID生成器Feign超时
     */
    @Bean("id-generator-feign")
    public TimeLimiter idGeneratorFeignTimeLimiter(TimeLimiterRegistry registry) {
        return registry.timeLimiter("id-generator-feign");
    }

    /**
     * 用户中心Feign超时
     */
    @Bean("user-center-feign")
    public TimeLimiter userCenterFeignTimeLimiter(TimeLimiterRegistry registry) {
        return registry.timeLimiter("user-center-feign");
    }

    /**
     * 订单Feign超时
     */
    @Bean("order-feign")
    public TimeLimiter orderFeignTimeLimiter(TimeLimiterRegistry registry) {
        return registry.timeLimiter("order-feign");
    }
} 