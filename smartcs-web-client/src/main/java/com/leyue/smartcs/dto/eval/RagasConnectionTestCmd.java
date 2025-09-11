package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * RAGAS连接测试命令
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagasConnectionTestCmd {
    
    /**
     * 服务端点URL
     */
    private String serviceEndpoint;
    
    /**
     * 连接超时时间（毫秒）
     */
    private Integer connectionTimeout;
    
    /**
     * 读取超时时间（毫秒）
     */
    private Integer readTimeout;
    
    /**
     * 认证类型：NONE, BASIC, BEARER, API_KEY
     */
    private String authType;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * API密钥
     */
    private String apiKey;
    
    /**
     * Bearer令牌
     */
    private String bearerToken;
    
    /**
     * 请求头
     */
    private Map<String, String> headers;
    
    /**
     * 请求参数
     */
    private Map<String, Object> parameters;
    
    /**
     * 是否验证SSL证书
     */
    private Boolean verifySsl;
    
    /**
     * 是否启用连接池
     */
    private Boolean enableConnectionPool;
    
    /**
     * 最大连接数
     */
    private Integer maxConnections;
    
    /**
     * 连接池超时时间（毫秒）
     */
    private Integer poolTimeout;
    
    /**
     * 测试类型：HEALTH_CHECK, FUNCTIONALITY_TEST, PERFORMANCE_TEST
     */
    private String testType;
    
    /**
     * 是否包含详细诊断
     */
    private Boolean includeDetailedDiagnostics;
    
    /**
     * 是否包含性能测试
     */
    private Boolean includePerformanceTest;
    
    /**
     * 性能测试请求数
     */
    private Integer performanceTestRequests;
}
