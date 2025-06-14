package com.leyue.smartcs.config;

import com.leyue.smartcs.config.context.UserContext;
import io.micrometer.context.ContextRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;

@Configuration
public class ContextPropConfig {

    /**
     * 配置线程上下文,解决springmvc线程内使用reactor线程时候，上下文传递问题。
     * <dependency>
     *      <groupId>io.micrometer</groupId>
     *      <artifactId>context-propagation</artifactId>
     * </dependency>
     */
    @PostConstruct
    void init() {
        ContextRegistry.getInstance().registerThreadLocalAccessor(
                // 唯一 key
                "SPRING_REQUEST_ATTR",
                // 读取
                RequestContextHolder::getRequestAttributes,
                // 写回
                attr -> RequestContextHolder.setRequestAttributes(attr, true),
                // 清理
                RequestContextHolder::resetRequestAttributes
        ).registerThreadLocalAccessor(// 唯一 key
                "USER_CONTEXT",
                // 读取
                UserContext::getCurrentUser,
                // 写回
                UserContext::setCurrentUser,
                // 清理
                UserContext::clear);
    }

    //((DefaultServerRequest)RequestContextHolder.getRequestAttributes().getAttribute("org.springframework.web.servlet.function.RouterFunctions.request",0)).headers()  authorization
}