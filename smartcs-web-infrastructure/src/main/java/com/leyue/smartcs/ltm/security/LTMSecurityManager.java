package com.leyue.smartcs.ltm.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

/**
 * LTM安全管理器
 * 提供记忆数据的安全访问控制和隐私保护
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LTMSecurityManager {

    private final LTMAuditLogger auditLogger;
    private final LTMDataEncryptor dataEncryptor;

    @Value("${smartcs.ai.ltm.security.user-isolation.enabled:true}")
    private boolean userIsolationEnabled;

    @Value("${smartcs.ai.ltm.security.user-isolation.strict-mode:true}")
    private boolean strictModeEnabled;

    @Value("${smartcs.ai.ltm.security.access-control.enabled:true}")
    private boolean accessControlEnabled;

    @Value("${smartcs.ai.ltm.security.access-control.sensitive-access-limit:100}")
    private int sensitiveAccessLimit;

    @Value("${smartcs.ai.ltm.security.access-control.audit-logging:true}")
    private boolean auditLoggingEnabled;

    // 用户访问计数器
    private final ConcurrentHashMap<Long, AtomicLong> userAccessCounts = new ConcurrentHashMap<>();
    
    // 敏感内容模式
    private static final Set<Pattern> SENSITIVE_PATTERNS = Set.of(
        Pattern.compile("\\b\\d{11}\\b"), // 手机号
        Pattern.compile("\\b\\d{15,19}\\b"), // 银行卡号
        Pattern.compile("\\b\\d{17}[\\dXx]\\b"), // 身份证号
        Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b"), // 邮箱
        Pattern.compile("\\b(?:密码|password|pwd)\\s*[:=]\\s*\\S+", Pattern.CASE_INSENSITIVE)
    );

    /**
     * 验证用户是否有权访问指定记忆
     */
    public boolean canAccessMemory(Long userId, Long memoryOwnerId, String memoryType, Object memoryData) {
        try {
            // 基本用户隔离检查
            if (userIsolationEnabled && !userId.equals(memoryOwnerId)) {
                if (strictModeEnabled) {
                    auditLog(userId, "MEMORY_ACCESS_DENIED", 
                           "Strict isolation: User %d attempted to access memory of user %d"
                           .formatted(userId, memoryOwnerId));
                    return false;
                }
                
                // 非严格模式下可能允许某些情况的访问（如管理员）
                if (!isAuthorizedCrossUserAccess(userId, memoryOwnerId)) {
                    return false;
                }
            }

            // 访问频率限制检查
            if (accessControlEnabled && isSensitiveMemory(memoryData)) {
                if (!checkAccessLimit(userId)) {
                    auditLog(userId, "MEMORY_ACCESS_RATE_LIMITED", 
                           "User exceeded sensitive memory access limit");
                    return false;
                }
            }

            // 记录访问
            if (auditLoggingEnabled) {
                auditLog(userId, "MEMORY_ACCESS_GRANTED", 
                       "Accessed %s memory of user %d".formatted(memoryType, memoryOwnerId));
            }

            return true;

        } catch (Exception e) {
            log.error("安全验证异常: userId={}, memoryOwnerId={}, error={}", 
                     userId, memoryOwnerId, e.getMessage());
            return false;
        }
    }

    /**
     * 验证用户是否可以修改指定记忆
     */
    public boolean canModifyMemory(Long userId, Long memoryOwnerId, String operation) {
        if (!canAccessMemory(userId, memoryOwnerId, "modify", null)) {
            return false;
        }

        // 修改操作的额外检查
        if (!userId.equals(memoryOwnerId)) {
            auditLog(userId, "MEMORY_MODIFY_DENIED", 
                   "Cross-user modification not allowed: operation=%s".formatted(operation));
            return false;
        }

        auditLog(userId, "MEMORY_MODIFY_GRANTED", 
               "User authorized for operation: %s".formatted(operation));
        return true;
    }

    /**
     * 清理敏感内容
     */
    public String sanitizeContent(String content, Long userId, int privacyLevel) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        String sanitized = content;

        // 根据隐私级别进行不同程度的清理
        switch (privacyLevel) {
            case 1: // 普通级别 - 基本敏感信息清理
                sanitized = sanitizeBasicSensitiveInfo(sanitized);
                break;
            case 2: // 敏感级别 - 更严格的清理
                sanitized = sanitizeSensitiveInfo(sanitized);
                break;
            case 3: // 严格级别 - 最严格的清理
                sanitized = sanitizeStrictSensitiveInfo(sanitized);
                break;
        }

        if (!sanitized.equals(content)) {
            auditLog(userId, "CONTENT_SANITIZED", 
                   "Content sanitized with privacy level %d".formatted(privacyLevel));
        }

        return sanitized;
    }

    /**
     * 加密敏感记忆内容
     */
    public String encryptSensitiveContent(String content, Long userId) {
        if (!isSensitiveContent(content)) {
            return content;
        }

        try {
            String encrypted = dataEncryptor.encrypt(content);
            auditLog(userId, "MEMORY_ENCRYPTED", "Sensitive content encrypted");
            return encrypted;
        } catch (Exception e) {
            log.error("内容加密失败: userId={}, error={}", userId, e.getMessage());
            return content;
        }
    }

    /**
     * 解密敏感记忆内容
     */
    public String decryptSensitiveContent(String encryptedContent, Long userId) {
        if (!dataEncryptor.isEncrypted(encryptedContent)) {
            return encryptedContent;
        }

        try {
            String decrypted = dataEncryptor.decrypt(encryptedContent);
            auditLog(userId, "MEMORY_DECRYPTED", "Sensitive content decrypted");
            return decrypted;
        } catch (Exception e) {
            log.error("内容解密失败: userId={}, error={}", userId, e.getMessage());
            return encryptedContent;
        }
    }

    /**
     * 验证记忆导出权限
     */
    public boolean canExportMemory(Long userId, Long memoryOwnerId) {
        if (!userId.equals(memoryOwnerId)) {
            auditLog(userId, "MEMORY_EXPORT_DENIED", "Cross-user export not allowed");
            return false;
        }

        auditLog(userId, "MEMORY_EXPORT_GRANTED", "User authorized to export memories");
        return true;
    }

    /**
     * 验证记忆删除权限
     */
    public boolean canDeleteMemory(Long userId, Long memoryOwnerId, String memoryType) {
        if (!canModifyMemory(userId, memoryOwnerId, "DELETE")) {
            return false;
        }

        auditLog(userId, "MEMORY_DELETE_GRANTED", 
               "User authorized to delete %s memory".formatted(memoryType));
        return true;
    }

    // 私有辅助方法

    private boolean isAuthorizedCrossUserAccess(Long userId, Long targetUserId) {
        // 简化实现，实际可能需要检查用户角色、权限等
        // 例如：管理员可以访问其他用户的记忆
        return false;
    }

    private boolean checkAccessLimit(Long userId) {
        AtomicLong count = userAccessCounts.computeIfAbsent(userId, k -> new AtomicLong(0));
        long currentCount = count.incrementAndGet();
        
        // 每日重置计数器（简化实现）
        // 实际应该使用更精确的时间窗口
        return currentCount <= sensitiveAccessLimit;
    }

    private boolean isSensitiveMemory(Object memoryData) {
        if (memoryData == null) return false;
        return isSensitiveContent(memoryData.toString());
    }

    private boolean isSensitiveContent(String content) {
        if (content == null) return false;
        
        return SENSITIVE_PATTERNS.stream()
            .anyMatch(pattern -> pattern.matcher(content).find());
    }

    private String sanitizeBasicSensitiveInfo(String content) {
        String result = content;
        
        // 替换手机号
        result = result.replaceAll("\\b\\d{11}\\b", "***********");
        
        // 替换邮箱
        result = result.replaceAll("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b", "***@***.***");
        
        return result;
    }

    private String sanitizeSensitiveInfo(String content) {
        String result = sanitizeBasicSensitiveInfo(content);
        
        // 替换银行卡号
        result = result.replaceAll("\\b\\d{15,19}\\b", "****************");
        
        // 替换身份证号
        result = result.replaceAll("\\b\\d{17}[\\dXx]\\b", "******************");
        
        return result;
    }

    private String sanitizeStrictSensitiveInfo(String content) {
        String result = sanitizeSensitiveInfo(content);
        
        // 替换密码相关信息
        result = result.replaceAll("\\b(?:密码|password|pwd)\\s*[:=]\\s*\\S+", 
                                 "密码:***", Pattern.CASE_INSENSITIVE);
        
        // 可以添加更多严格的清理规则
        
        return result;
    }

    private void auditLog(Long userId, String action, String details) {
        if (auditLoggingEnabled && auditLogger != null) {
            auditLogger.log(userId, action, details);
        }
    }
}