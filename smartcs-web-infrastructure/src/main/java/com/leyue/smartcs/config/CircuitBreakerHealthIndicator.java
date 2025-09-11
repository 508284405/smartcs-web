package com.leyue.smartcs.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 熔断器健康检查
 * 提供熔断器状态的健康检查
 */
@Component
@Slf4j
public class CircuitBreakerHealthIndicator implements HealthIndicator {

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private RetryRegistry retryRegistry;

    @Autowired
    private BulkheadRegistry bulkheadRegistry;

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();
        boolean isHealthy = true;

        try {
            // 检查熔断器状态
            Map<String, Object> circuitBreakerDetails = new HashMap<>();
            for (CircuitBreaker circuitBreaker : circuitBreakerRegistry.getAllCircuitBreakers()) {
                CircuitBreaker.State state = circuitBreaker.getState();
                CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();
                
                Map<String, Object> cbDetails = new HashMap<>();
                cbDetails.put("state", state.name());
                cbDetails.put("failureRate", metrics.getFailureRate());
                cbDetails.put("slowCallRate", metrics.getSlowCallRate());
                cbDetails.put("totalCalls", metrics.getNumberOfSuccessfulCalls() + metrics.getNumberOfFailedCalls());
                cbDetails.put("failedCalls", metrics.getNumberOfFailedCalls());
                cbDetails.put("successfulCalls", metrics.getNumberOfSuccessfulCalls());
                cbDetails.put("slowCalls", metrics.getNumberOfSlowCalls());
                cbDetails.put("notPermittedCalls", metrics.getNumberOfNotPermittedCalls());
                
                circuitBreakerDetails.put(circuitBreaker.getName(), cbDetails);
                
                // 如果熔断器处于OPEN状态，标记为不健康
                if (state == CircuitBreaker.State.OPEN) {
                    isHealthy = false;
                }
            }
            details.put("circuitBreakers", circuitBreakerDetails);

            // 检查重试状态
            Map<String, Object> retryDetails = new HashMap<>();
            for (Retry retry : retryRegistry.getAllRetries()) {
                Retry.Metrics metrics = retry.getMetrics();
                
                Map<String, Object> retryDetail = new HashMap<>();
                retryDetail.put("successfulCallsWithoutRetry", metrics.getNumberOfSuccessfulCallsWithoutRetryAttempt());
                retryDetail.put("failedCallsWithRetry", metrics.getNumberOfFailedCallsWithRetryAttempt());
                retryDetail.put("successfulCallsWithRetry", metrics.getNumberOfSuccessfulCallsWithRetryAttempt());
                
                retryDetails.put(retry.getName(), retryDetail);
            }
            details.put("retries", retryDetails);

            // 检查限流状态
            Map<String, Object> bulkheadDetails = new HashMap<>();
            for (Bulkhead bulkhead : bulkheadRegistry.getAllBulkheads()) {
                Bulkhead.Metrics metrics = bulkhead.getMetrics();
                
                Map<String, Object> bhDetails = new HashMap<>();
                bhDetails.put("availableConcurrentCalls", metrics.getAvailableConcurrentCalls());
                bhDetails.put("maxAllowedConcurrentCalls", metrics.getMaxAllowedConcurrentCalls());
                
                bulkheadDetails.put(bulkhead.getName(), bhDetails);
            }
            details.put("bulkheads", bulkheadDetails);

            log.debug("熔断器健康检查完成，状态: {}", isHealthy ? "UP" : "DOWN");
            
            return isHealthy ? Health.up().withDetails(details).build() 
                           : Health.down().withDetails(details).build();

        } catch (Exception e) {
            log.error("熔断器健康检查失败", e);
            details.put("error", e.getMessage());
            return Health.down().withDetails(details).build();
        }
    }
} 