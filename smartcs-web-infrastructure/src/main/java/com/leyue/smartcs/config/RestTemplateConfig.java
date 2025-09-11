package com.leyue.smartcs.config;

import com.leyue.smartcs.config.context.TraceContextHolder;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate配置
 */
@Configuration
public class RestTemplateConfig {
    
    @Bean
    public RestTemplate restTemplate(TraceContextHolder traceContextHolder) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        RestTemplate restTemplate = new RestTemplate(factory);

        // W3C Trace Context和自定义追踪头传播拦截器
        ClientHttpRequestInterceptor traceInterceptor = (request, body, execution) -> {
            propagateTraceContext(request.getHeaders(), traceContextHolder);
            return execution.execute(request, body);
        };
        restTemplate.getInterceptors().add(traceInterceptor);

        return restTemplate;
    }
    
    /**
     * 传播追踪上下文，支持W3C协议和自定义协议
     */
    private void propagateTraceContext(org.springframework.http.HttpHeaders headers, 
                                     TraceContextHolder traceContextHolder) {
        // 获取当前traceId
        String traceId = MDC.get(TraceContextHolder.TRACE_ID_KEY);
        if (!StringUtils.hasText(traceId)) {
            traceId = traceContextHolder.getCurrentTraceId();
        }
        if (!StringUtils.hasText(traceId)) {
            traceId = traceContextHolder.initTraceContext();
        }
        
        // 生成W3C格式的traceparent
        String spanId = Long.toHexString(System.nanoTime() & 0xFFFFFFFFFFFFFFFFL);
        
        // 确保traceId是32位十六进制，spanId是16位十六进制
        String normalizedTraceId = normalizeTraceId(traceId);
        String normalizedSpanId = spanId.length() > 16 ? spanId.substring(0, 16) : String.format("%016s", spanId);
        
        String traceparent = String.format("00-%s-%s-01", normalizedTraceId, normalizedSpanId);
        
        // 设置W3C和自定义追踪头
        headers.set("traceparent", traceparent);
        headers.set(TraceContextHolder.TRACE_ID_HEADER, traceId);
    }
    
    /**
     * 标准化traceId为32位十六进制格式
     */
    private String normalizeTraceId(String traceId) {
        if (traceId == null || traceId.isEmpty()) {
            return String.format("%032d", System.nanoTime());
        }
        
        // 移除非十六进制字符
        String cleanTraceId = traceId.replaceAll("[^0-9a-fA-F]", "");
        
        if (cleanTraceId.length() >= 32) {
            return cleanTraceId.substring(0, 32).toLowerCase();
        } else if (cleanTraceId.length() >= 16) {
            return String.format("%032s", cleanTraceId).replace(' ', '0').toLowerCase();
        } else {
            // 使用hash补齐
            String hash = Integer.toHexString(traceId.hashCode());
            return String.format("%032s", cleanTraceId + hash).replace(' ', '0').toLowerCase();
        }
    }
} 
