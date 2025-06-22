package com.leyue.smartcs.filter;

import com.alibaba.fastjson2.JSONObject;
import com.leyue.smartcs.common.util.JwtTokenUtil;
import com.leyue.smartcs.config.WhiteListProperties;
import com.leyue.smartcs.config.context.UserContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class TokenValidateFilter implements Filter {

    private final JwtTokenUtil jwtTokenUtil;
    private final WhiteListProperties whiteListProperties;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 获取请求路径
        String requestUri = httpRequest.getRequestURI();

        // 检查白名单，如果在白名单中则直接放行，无需token校验
        for (String pattern : whiteListProperties.getWhiteList()) {
            if (pathMatcher.match(pattern, requestUri)) {
                chain.doFilter(request, response);
                return;
            }
        }

        String authHeader = httpRequest.getHeader("Authorization");

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            log.error("Token is missing or invalid format");
            handleUnauthorized(httpResponse, "Token is missing or invalid format");
            return;
        }

        // 提取Token（去掉"Bearer "前缀）
        String token = authHeader.substring(7);

        try {
            if (!jwtTokenUtil.validateToken(token)) {
                log.error("Invalid token");
                handleUnauthorized(httpResponse, "Invalid token");
                return;
            }

            // 从Token中提取用户信息并设置到上下文
            Long userId = jwtTokenUtil.getUserId(token);
            String username = jwtTokenUtil.getUsername(token);
            List<String> roles = jwtTokenUtil.getUserRoles(token);
            List<Object> menus = jwtTokenUtil.getUserMenus(token);

            // 构建用户信息并设置到上下文
            UserContext.UserInfo userInfo = new UserContext.UserInfo();
            userInfo.setId(userId);
            userInfo.setUsername(username);
            
            // 这里需要根据实际需要转换角色和菜单信息
            // 暂时简化处理，可以后续完善
            UserContext.setCurrentUser(userInfo);

            chain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Token validation failed", e);
            handleUnauthorized(httpResponse, "Token validation failed");
        } finally {
            UserContext.clear();
        }
    }

    private void handleUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");

        JSONObject result = new JSONObject();
        result.put("code", HttpStatus.UNAUTHORIZED.value());
        result.put("message", message);

        PrintWriter writer = response.getWriter();
        writer.write(result.toJSONString());
        writer.flush();
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void destroy() {
    }
}