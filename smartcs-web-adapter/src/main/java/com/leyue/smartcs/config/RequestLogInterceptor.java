package com.leyue.smartcs.config;

import com.leyue.smartcs.common.secret.LogDesensitizationUtil;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Map;

/**
 * 统一请求日志拦截器，打印请求参数、header、body及耗时
 */
@Component
public class RequestLogInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(RequestLogInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        long startTime = System.currentTimeMillis();
        request.setAttribute("_request_log_start_time", startTime);
        StringBuilder sb = new StringBuilder();
        sb.append("[Request] ")
          .append(request.getMethod()).append(" ")
          .append(request.getRequestURI());
        if (request.getQueryString() != null) {
            sb.append("?").append(request.getQueryString());
        }
        sb.append("\nHeaders: ");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            sb.append(name).append(": ").append(request.getHeader(name)).append(", ");
        }
        sb.append("\nParams: ");
        Map<String, String[]> params = request.getParameterMap();
        for (String key : params.keySet()) {
            sb.append(key).append("=");
            String[] values = params.get(key);
            if (values != null) {
                for (String v : values) {
                    sb.append(v).append(",");
                }
            }
        }
        // 打印body（如果是可读类型）
        if (request instanceof CachedBodyHttpServletRequest) {
            String body = ((CachedBodyHttpServletRequest) request).getBody();
            sb.append("\nBody: ").append(LogDesensitizationUtil.desensitizeString(body));
        }
        
        // 脱敏整个日志信息
        String logMessage = LogDesensitizationUtil.desensitizeString(sb.toString());
        log.info(logMessage);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        long startTime = (Long) request.getAttribute("_request_log_start_time");
        long duration = System.currentTimeMillis() - startTime;
        log.info("[Request End] {} {} cost={}ms status={}", request.getMethod(), request.getRequestURI(), duration, response.getStatus());
    }

    /**
     * 包装HttpServletRequest，缓存body内容，便于多次读取
     */
    public static class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {
        private final byte[] cachedBody;

        public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
            super(request);
            ServletInputStream inputStream = request.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            cachedBody = baos.toByteArray();
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream bais = new ByteArrayInputStream(cachedBody);
            return new ServletInputStream() {
                @Override
                public int read() {
                    return bais.read();
                }
                @Override
                public boolean isFinished() {
                    return bais.available() == 0;
                }
                @Override
                public boolean isReady() {
                    return true;
                }
                @Override
                public void setReadListener(ReadListener readListener) {}
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }

        public String getBody() {
            return new String(cachedBody, StandardCharsets.UTF_8);
        }
    }
} 