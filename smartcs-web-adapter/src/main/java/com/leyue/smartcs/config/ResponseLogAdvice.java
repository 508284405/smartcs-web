package com.leyue.smartcs.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 统一响应日志Advice，打印响应体内容
 */
@ControllerAdvice
public class ResponseLogAdvice implements ResponseBodyAdvice<Object> {
    private static final Logger log = LoggerFactory.getLogger(ResponseLogAdvice.class);

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        // 只对@RestController/@ResponseBody生效
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        log.info("[Response] {} {} body={}", request.getMethod(), request.getURI(), body);
        return body;
    }
} 