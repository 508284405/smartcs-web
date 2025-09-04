package com.leyue.smartcs.config.feign;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyue.smartcs.config.context.TraceContextHolder;
import feign.Feign;
import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.codec.Encoder;
import jakarta.servlet.http.HttpServletRequest;
import okhttp3.OkHttpClient;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

@Configuration
@ConditionalOnClass(Feign.class)
public class FeignConfig {

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public Encoder feignEncoder() {
        // 1) 创建只序列化非空字段的 ObjectMapper
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // 2) 将其交给 Spring 的 HttpMessageConverters
        HttpMessageConverter<?> jacksonConverter =
                new MappingJackson2HttpMessageConverter(mapper);
        ObjectFactory<HttpMessageConverters> factory =
                () -> new HttpMessageConverters(jacksonConverter);

        // 3) 用 SpringEncoder 包装，再给 Feign 使用
        return new SpringEncoder(factory);
    }

    @Bean
    public RequestInterceptor authorizationInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    String authorization = request.getHeader("Authorization");
                    if (authorization != null && !authorization.isEmpty()) {
                        template.header("Authorization", authorization);
                    }
                }
            }
        };
    }

    /**
     * W3C Trace Context传播拦截器
     */
    @Bean 
    public RequestInterceptor traceContextPropagationInterceptor(TraceContextHolder traceContextHolder) {
        return template -> {
            propagateTraceContext(template, traceContextHolder);
        };
    }
    
    /**
     * 传播追踪上下文，支持W3C协议和自定义协议
     */
    private void propagateTraceContext(RequestTemplate template, TraceContextHolder traceContextHolder) {
        // 获取当前traceId
        String traceId = MDC.get(TraceContextHolder.TRACE_ID_KEY);
        if (!StringUtils.hasText(traceId)) {
            traceId = traceContextHolder.getCurrentTraceId();
        }
        if (!StringUtils.hasText(traceId)) {
            // 场景：定时任务/非请求线程触发的调用，初始化一个traceId
            traceId = traceContextHolder.initTraceContext();
        }
        
        // 生成W3C格式的traceparent
        String spanId = Long.toHexString(System.nanoTime() & 0xFFFFFFFFFFFFFFFFL);
        
        // 标准化traceId和spanId
        String normalizedTraceId = normalizeTraceId(traceId);
        String normalizedSpanId = spanId.length() > 16 ? spanId.substring(0, 16) : String.format("%016s", spanId);
        
        String traceparent = String.format("00-%s-%s-01", normalizedTraceId, normalizedSpanId);
        
        // 设置W3C和自定义追踪头
        template.header("traceparent", traceparent);
        template.header(TraceContextHolder.TRACE_ID_HEADER, traceId);
    }
    
    /**
     * 标准化traceId为32位十六进制格式
     */
    private String normalizeTraceId(String traceId) {
        if (traceId == null || traceId.isEmpty()) {
            return String.format("%032x", System.nanoTime());
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

    /**
     * Feign错误解码器，用于处理熔断器异常
     */
    @Bean
    public feign.codec.ErrorDecoder feignErrorDecoder() {
        return new feign.codec.ErrorDecoder() {
            @Override
            public Exception decode(String methodKey, feign.Response response) {
                // 根据HTTP状态码返回相应的异常
                switch (response.status()) {
                    case 400:
                        return new RuntimeException("Bad Request");
                    case 401:
                        return new RuntimeException("Unauthorized");
                    case 403:
                        return new RuntimeException("Forbidden");
                    case 404:
                        return new RuntimeException("Not Found");
                    case 500:
                        return new RuntimeException("Internal Server Error");
                    case 502:
                        return new RuntimeException("Bad Gateway");
                    case 503:
                        return new RuntimeException("Service Unavailable");
                    case 504:
                        return new RuntimeException("Gateway Timeout");
                    default:
                        return new RuntimeException("Unknown Error");
                }
            }
        };
    }
} 
