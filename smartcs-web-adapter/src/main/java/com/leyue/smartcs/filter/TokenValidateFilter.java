package com.leyue.smartcs.filter;

import com.alibaba.fastjson2.JSONObject;
import com.leyue.smartcs.common.util.JwtTokenUtil;
import com.leyue.smartcs.config.WhiteListProperties;
import com.leyue.smartcs.config.context.UserContext;
import com.leyue.smartcs.config.context.TraceContextHolder;
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
    private final TraceContextHolder traceContextHolder;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 支持W3C Trace Context协议和自定义协议的traceId透传
        String inboundTraceId = extractTraceId(httpRequest);
        if (StringUtils.hasText(inboundTraceId)) {
            traceContextHolder.setTraceId(inboundTraceId);
        }

        // 初始化追踪上下文，为所有请求生成/确认traceId
        String traceId = traceContextHolder.initTraceContext();
        log.debug("请求开始处理: uri={}, method={}, traceId={}", 
                httpRequest.getRequestURI(), httpRequest.getMethod(), traceId);

        try {
            // 兼容CORS预检请求，直接放行
            if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
                chain.doFilter(request, response);
                return;
            }

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

            log.debug("用户认证成功: userId={}, username={}, traceId={}", userId, username, traceId);

            chain.doFilter(request, response);
            
        } catch (Exception e) {
            log.error("Token validation failed", e);
            handleUnauthorized(httpResponse, "Token validation failed");
        } finally {
            // 清理用户上下文和追踪上下文
            UserContext.clear();
            traceContextHolder.clearTraceContext();
            log.debug("请求处理完成，清理上下文: traceId={}", traceId);
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

    /**
     * 提取traceId，支持多种协议
     * 优先级：W3C traceparent > 自定义 X-Trace-Id > traceId
     * 
     * @param request HTTP请求
     * @return 提取到的traceId，如果没有则返回null
     */
    private String extractTraceId(HttpServletRequest request) {
        // W3C Trace Context: traceparent格式 "00-{trace-id}-{parent-id}-{trace-flags}"
        String traceparent = request.getHeader("traceparent");
        if (StringUtils.hasText(traceparent)) {
            String[] parts = traceparent.split("-");
            if (parts.length >= 2 && parts[1].length() == 32) {
                log.debug("从W3C traceparent提取traceId: {}", parts[1]);
                return parts[1];
            } else {
                log.warn("无效的W3C traceparent格式: {}", traceparent);
            }
        }
        
        // 自定义 X-Trace-Id 头
        String customTraceId = request.getHeader(TraceContextHolder.TRACE_ID_HEADER);
        if (StringUtils.hasText(customTraceId)) {
            log.debug("从X-Trace-Id提取traceId: {}", customTraceId);
            return customTraceId;
        }
        
        // 传统 traceId 头（向下兼容）
        String legacyTraceId = request.getHeader(TraceContextHolder.TRACE_ID_KEY);
        if (StringUtils.hasText(legacyTraceId)) {
            log.debug("从traceId头提取traceId: {}", legacyTraceId);
            return legacyTraceId;
        }
        
        return null;
    }

    @Override
    public void destroy() {
    }
}
