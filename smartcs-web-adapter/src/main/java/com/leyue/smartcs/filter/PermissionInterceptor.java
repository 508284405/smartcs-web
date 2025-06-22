package com.leyue.smartcs.filter;

import com.leyue.smartcs.config.WhiteListProperties;
import com.leyue.smartcs.config.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionInterceptor implements HandlerInterceptor {

    private final WhiteListProperties whiteListProperties;
    
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果不是处理方法直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        
        // 获取请求路径
        String requestUri = request.getRequestURI();
        
        // 检查白名单，如果在白名单中则直接放行，无需权限校验
        for (String pattern : whiteListProperties.getWhiteList()) {
            if (pathMatcher.match(pattern, requestUri)) {
                return true;
            }
        }

        // 获取当前用户信息（应该已经在TokenValidateFilter中设置）
        UserContext.UserInfo currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            log.warn("用户信息为空，拒绝访问: {}", requestUri);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        // 如果是管理员，直接放行
        if (currentUser.isAdmin()) {
            return true;
        }

        // 检查用户是否有权限访问该URL
        if (!UserContext.hasUrlAccess(requestUri)) {
            log.warn("用户 {} 无权限访问: {}", currentUser.getUsername(), requestUri);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return false;
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 清理ThreadLocal
        UserContext.clear();
    }
}