package com.leyue.smartcs.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//@Configuration
public class BbbConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 配置允许跨域的路径
                .allowedOrigins("*")  // 允许的前端源
                .allowedMethods("*")  // 允许的请求方法
                .allowedHeaders("*")  // 允许的请求头
                .maxAge(3600);  // 预检请求的有效期
    }
}
