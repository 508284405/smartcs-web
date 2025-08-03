package com.leyue.smartcs.config.feign;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Feign;
import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.codec.Encoder;
import jakarta.servlet.http.HttpServletRequest;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
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