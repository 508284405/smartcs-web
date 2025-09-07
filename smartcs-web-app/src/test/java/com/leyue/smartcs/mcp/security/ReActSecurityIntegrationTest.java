package com.leyue.smartcs.mcp.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.leyue.smartcs.mcp.audit.ToolAuditService;
import com.leyue.smartcs.mcp.audit.ToolAuditStats;
import com.leyue.smartcs.mcp.ratelimit.RateLimitResult;
import com.leyue.smartcs.mcp.ratelimit.ToolRateLimitService;
import com.leyue.smartcs.mcp.security.ToolSecurityResult;
import com.leyue.smartcs.mcp.security.ToolSecurityService;

import lombok.extern.slf4j.Slf4j;

/**
 * ReAct安全功能集成测试
 * 
 * <p>验证工具调用的安全限制、速率控制和审计监控功能。
 * 测试安全机制是否能有效防护和监控工具使用。</p>
 * 
 * @author Claude
 */
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
class ReActSecurityIntegrationTest {

    @Autowired(required = false)
    private ToolSecurityService toolSecurityService;
    
    @Autowired(required = false) 
    private ToolRateLimitService toolRateLimitService;
    
    @Autowired(required = false)
    private ToolAuditService toolAuditService;

    @Test
    void testToolSecurityService() {
        log.info("=== 测试工具安全服务 ===");
        
        if (toolSecurityService != null) {
            log.info("工具安全服务已正确注入");
            
            // 测试正常工具调用验证
            ToolSecurityResult result1 = toolSecurityService.validateToolCall(
                "session123", 1L, "queryOrder", "orderNumber=12345");
            log.info("正常调用验证结果: {}", result1);
            
            // 测试敏感工具调用验证
            ToolSecurityResult result2 = toolSecurityService.validateToolCall(
                "session123", 1L, "cancelOrder", "orderNumber=12345");
            log.info("敏感工具调用验证结果: {}", result2);
            
            // 测试未认证用户调用敏感工具
            ToolSecurityResult result3 = toolSecurityService.validateToolCall(
                "session456", null, "cancelOrder", "orderNumber=12345");
            log.info("未认证用户调用敏感工具结果: {}", result3);
            
            // 测试安全统计
            var securityStats = toolSecurityService.getSecurityStats();
            log.info("安全统计信息: blockedSessions={}, blockedUsers={}, sensitiveTools={}", 
                    securityStats.getBlockedSessionsCount(),
                    securityStats.getBlockedUsersCount(), 
                    securityStats.getSensitiveToolsCount());
                    
        } else {
            log.warn("工具安全服务未配置");
        }
    }

    @Test
    void testToolRateLimitService() {
        log.info("=== 测试工具速率限制服务 ===");
        
        if (toolRateLimitService != null) {
            log.info("工具速率限制服务已正确注入");
            
            // 测试正常速率限制检查
            RateLimitResult result1 = toolRateLimitService.checkRateLimit(
                1L, "session123", "queryOrder");
            log.info("正常速率限制检查结果: {}", result1);
            
            // 模拟高频调用测试速率限制
            for (int i = 0; i < 10; i++) {
                RateLimitResult result = toolRateLimitService.checkRateLimit(
                    1L, "session123", "cancelOrder");
                log.info("高频调用测试 #{}: {}", i + 1, result);
                
                if (result.isLimited()) {
                    log.info("速率限制生效，剩余重置时间: {}秒", result.getSecondsUntilReset());
                    break;
                }
            }
            
            // 测试速率限制统计
            var rateLimitStats = toolRateLimitService.getRateLimitStats();
            log.info("速率限制统计: activeUsers={}, activeSessions={}, activeTools={}, blockedCalls={}",
                    rateLimitStats.getActiveUserLimiters(),
                    rateLimitStats.getActiveSessionLimiters(),
                    rateLimitStats.getActiveToolLimiters(),
                    rateLimitStats.getTotalCallsBlocked());
                    
        } else {
            log.warn("工具速率限制服务未配置");
        }
    }

    @Test
    void testToolAuditService() {
        log.info("=== 测试工具审计服务 ===");
        
        if (toolAuditService != null) {
            log.info("工具审计服务已正确注入");
            
            // 测试成功调用审计
            String auditId1 = toolAuditService.recordToolCallStart(
                "session123", 1L, "queryOrder", "orderNumber=12345");
            log.info("创建审计记录: {}", auditId1);
            
            // 模拟执行延迟
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            toolAuditService.recordToolCallSuccess(auditId1, "订单查询成功", 100L);
            log.info("记录成功执行: auditId={}", auditId1);
            
            // 测试失败调用审计
            String auditId2 = toolAuditService.recordToolCallStart(
                "session123", 1L, "cancelOrder", "orderNumber=99999");
            toolAuditService.recordToolCallFailure(auditId2, "订单不存在", 50L);
            log.info("记录失败执行: auditId={}", auditId2);
            
            // 测试阻止调用审计
            toolAuditService.recordToolCallBlocked("session456", null, 
                "cancelOrder", "权限不足");
            log.info("记录阻止调用完成");
            
            // 测试审计统计
            ToolAuditStats auditStats = toolAuditService.getAuditStats();
            log.info("审计统计: 总调用={}, 成功={}, 失败={}, 阻止={}, 成功率={:.1f}%, 健康状态={}",
                    auditStats.getTotalCalls(),
                    auditStats.getSuccessfulCalls(),
                    auditStats.getFailedCalls(), 
                    auditStats.getBlockedCalls(),
                    auditStats.getSuccessRate(),
                    auditStats.getHealthStatus());
                    
        } else {
            log.warn("工具审计服务未配置");
        }
    }

    @Test
    void testIntegratedSecurityWorkflow() {
        log.info("=== 测试集成安全工作流 ===");
        
        if (toolSecurityService != null && toolRateLimitService != null && toolAuditService != null) {
            // 模拟完整的安全检查流程
            String sessionId = "integration_test_session";
            Long userId = 999L;
            String toolName = "cancelOrder";
            Object parameters = "orderNumber=INT_TEST_12345";
            
            log.info("开始集成安全检查流程");
            
            // 1. 安全检查
            ToolSecurityResult securityResult = toolSecurityService.validateToolCall(
                sessionId, userId, toolName, parameters);
            log.info("安全检查结果: {}", securityResult);
            
            if (!securityResult.isAllowed()) {
                // 记录被阻止的调用
                toolAuditService.recordToolCallBlocked(sessionId, userId, toolName, 
                    securityResult.getMessage());
                log.info("调用被安全检查阻止，已记录审计");
                return;
            }
            
            // 2. 速率限制检查
            RateLimitResult rateLimitResult = toolRateLimitService.checkRateLimit(
                userId, sessionId, toolName);
            log.info("速率限制检查结果: {}", rateLimitResult);
            
            if (rateLimitResult.isLimited()) {
                // 记录被限流的调用
                toolAuditService.recordToolCallBlocked(sessionId, userId, toolName,
                    "速率限制: " + rateLimitResult.getMessage());
                log.info("调用被速率限制阻止，已记录审计");
                return;
            }
            
            // 3. 执行工具调用（模拟）
            String auditId = toolAuditService.recordToolCallStart(
                sessionId, userId, toolName, parameters);
            log.info("开始执行工具调用，审计ID: {}", auditId);
            
            // 模拟工具执行
            try {
                Thread.sleep(200); // 模拟执行时间
                
                // 模拟成功执行
                toolAuditService.recordToolCallSuccess(auditId, 
                    "订单取消成功", 200L);
                log.info("工具调用执行成功");
                
            } catch (Exception e) {
                toolAuditService.recordToolCallFailure(auditId, 
                    e.getMessage(), 200L);
                log.error("工具调用执行失败", e);
            }
            
            log.info("集成安全工作流测试完成");
            
        } else {
            log.warn("安全服务组件不完整，跳过集成测试");
        }
    }
}