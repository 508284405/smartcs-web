package com.leyue.smartcs.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.UrlCleaner;
import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import com.alibaba.fastjson2.JSON;
import com.leyue.smartcs.dto.common.ApiResponse;

/**
 * Sentinel 通用配置。
 */
@Configuration
@Slf4j
public class SentinelConfig {

    private static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();

    /**
     * 使 @SentinelResource 注解生效。
     */
    @Bean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect();
    }

    /**
     * 统一的降级/限流响应。
     */
    @Bean
    public BlockExceptionHandler sentinelBlockExceptionHandler() {
        return new BlockExceptionHandler() {
            @Override
            public void handle(HttpServletRequest request, HttpServletResponse response,
                               com.alibaba.csp.sentinel.slots.block.BlockException e) throws Exception {
                writeBlockResponse(request, response, e);
            }

            private void writeBlockResponse(HttpServletRequest request, HttpServletResponse response,
                                             com.alibaba.csp.sentinel.slots.block.BlockException ex) throws IOException {
                log.warn("Sentinel block triggered: uri={}, rule={}, message={}",
                        request.getRequestURI(), ex.getRule(), ex.getMessage());
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                ApiResponse<Void> body = ApiResponse.failure("SENTINEL_BLOCK",
                        Optional.ofNullable(ex.getRule()).map(Object::toString).orElse("Too many requests"));
                response.getWriter().write(JSON.toJSONString(body));
            }
        };
    }

    /**
     * 清洗 URL，减少 Sentinel 中的资源粒度，将 path 变量归一化。
     */
    @Bean
    public UrlCleaner sentinelUrlCleaner() {
        return url -> {
            if (url == null) {
                return "";
            }
            // 统一管理类接口前缀，降低粒度
            if (ANT_PATH_MATCHER.match("/api/admin/**", url)) {
                return "/api/admin/**";
            }
            if (ANT_PATH_MATCHER.match("/api/chat/messages/**", url)) {
                return "/api/chat/messages/**";
            }
            if (ANT_PATH_MATCHER.match("/api/app/**", url)) {
                return "/api/app/**";
            }
            if (ANT_PATH_MATCHER.match("/api/sse/**", url)) {
                return "/api/sse/**";
            }
            return url;
        };
    }
}
