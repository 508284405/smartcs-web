package com.leyue.smartcs.filter;

import com.leyue.smartcs.api.UserService;
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

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionInterceptor implements HandlerInterceptor {

    private final UserService userService;
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
        
        // 检查白名单，如果在白名单中则直接放行，无需token和权限校验
        for (String pattern : whiteListProperties.getWhiteList()) {
            if (pathMatcher.match(pattern, requestUri)) {
                return true;
            }
        }

        // 获取token
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        // 验证token并获取用户信息
        boolean isValid = userService.validateUserToken(token);
        if (!isValid) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        // 获取当前用户信息
        UserContext.UserInfo currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        if (currentUser.isAdmin()) {
            return true;
        }

        // 检查用户权限
        List<String> permissions = currentUser.getPermissions();
//        if (permissions == null || permissions.isEmpty()) {
//            response.setStatus(HttpStatus.FORBIDDEN.value());
//            return false;
//        }

        // TODO: 这里可以根据实际需求添加更细粒度的权限校验逻辑
        // 例如：检查特定API的权限码等

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 清理ThreadLocal
        UserContext.clear();
    }
}