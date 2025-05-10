package com.leyue.smartcs.filter;

import com.alibaba.fastjson2.JSONObject;
import com.leyue.smartcs.api.user.UserService;
import com.leyue.smartcs.config.WhiteListProperties;
import com.leyue.smartcs.context.UserContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
@Component
@Order(1)
public class TokenValidateFilter implements Filter {

    @Autowired
    private UserService userService;
    
    @Autowired
    private WhiteListProperties whiteListProperties;
    
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

        String token = httpRequest.getHeader("Authorization");

        if (!StringUtils.hasText(token)) {
            log.error("Token is missing");
            httpRequest.getRequestDispatcher("/error/unauthorized?message=Token is missing").forward(request, response);
            return;
        }

        try {
            if (!userService.validateUserToken(token)) {
                log.error("Invalid token");
                httpRequest.getRequestDispatcher("/error/unauthorized?message=Invalid token").forward(request, response);
                return;
            }
            chain.doFilter(request, response);
        } finally {
            if (UserContext.getCurrentUser() == null) {
                UserContext.clear();
            }
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