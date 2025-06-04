package com.leyue.smartcs.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.leyue.smartcs.filter.PermissionInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final PermissionInterceptor permissionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册权限拦截器，拦截所有API请求
        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/api/**")
                // 排除不需要权限验证的路径
                .excludePathPatterns(
                        "/api/users/login",
                        "/api/users/register",
                        "/api/users/verify-code",
                        "/api/users/forgot-password",
                        "/api/users/reset-password",
                        "/error/**"
                );
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizer() {
        return builder -> builder.serializerByType(Long.class, ToStringSerializer.instance)
                .serializerByType(Long.TYPE, ToStringSerializer.instance);
    }
} 