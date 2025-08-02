package com.leyue.smartcs.app.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 聊天安全验证器
 * 提供输入验证、敏感内容检测和防护机制
 */
@Component
@Slf4j
public class ChatSecurityValidator {

    // 配置参数
    @Value("${smartcs.ai.security.max-message-length:10000}")
    private int maxMessageLength;
    
    @Value("${smartcs.ai.security.max-variable-count:20}")
    private int maxVariableCount;
    
    @Value("${smartcs.ai.security.max-variable-length:1000}")
    private int maxVariableLength;
    
    @Value("${smartcs.ai.security.enable-prompt-injection-detection:true}")
    private boolean enablePromptInjectionDetection;
    
    @Value("${smartcs.ai.security.enable-sensitive-data-detection:true}")
    private boolean enableSensitiveDataDetection;

    // 提示词注入攻击模式
    private static final List<Pattern> PROMPT_INJECTION_PATTERNS = Arrays.asList(
        Pattern.compile("(?i)ignore\\s+(previous|above|all)\\s+(instructions?|commands?|prompts?)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i)forget\\s+(everything|all)\\s+(above|before)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i)system\\s*:\\s*you\\s+are\\s+now", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i)new\\s+(instruction|task|role)\\s*:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i)override\\s+(previous|system)\\s+(instruction|prompt)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i)act\\s+as\\s+(if\\s+you\\s+are|a)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i)pretend\\s+(you\\s+are|to\\s+be)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i)roleplay\\s+as", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i)\\[\\s*system\\s*\\]", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i)\\{\\s*system\\s*\\}", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i)###\\s*(instruction|system|new)", Pattern.CASE_INSENSITIVE)
    );

    // 敏感数据模式
    private static final List<Pattern> SENSITIVE_DATA_PATTERNS = Arrays.asList(
        Pattern.compile("\\b\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}\\b"), // 信用卡号
        Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b"), // SSN
        Pattern.compile("\\b\\d{11}\\b"), // 手机号
        Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b"), // 邮箱
        Pattern.compile("\\b(?:password|pwd|passwd|secret|token|key)\\s*[:=]\\s*\\S+", Pattern.CASE_INSENSITIVE)
    );

    /**
     * 验证聊天输入
     */
    public ValidationResult validateChatInput(String message, Map<String, Object> variables, String sessionId) {
        try {
            log.debug("开始验证聊天输入: sessionId={}, messageLength={}", sessionId, 
                     message != null ? message.length() : 0);
            
            // 基本输入验证
            ValidationResult basicResult = validateBasicInput(message, variables);
            if (!basicResult.isValid()) {
                return basicResult;
            }
            
            // 提示词注入检测
            if (enablePromptInjectionDetection) {
                ValidationResult injectionResult = detectPromptInjection(message);
                if (!injectionResult.isValid()) {
                    log.warn("检测到提示词注入攻击: sessionId={}, message={}", sessionId, 
                            message.substring(0, Math.min(message.length(), 100)));
                    return injectionResult;
                }
            }
            
            // 敏感数据检测
            if (enableSensitiveDataDetection) {
                ValidationResult sensitiveResult = detectSensitiveData(message);
                if (!sensitiveResult.isValid()) {
                    log.warn("检测到敏感数据: sessionId={}", sessionId);
                    return sensitiveResult;
                }
            }
            
            log.debug("聊天输入验证通过: sessionId={}", sessionId);
            return ValidationResult.valid();
            
        } catch (Exception e) {
            log.error("聊天输入验证失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
            return ValidationResult.invalid("输入验证过程中出现错误");
        }
    }

    /**
     * 基本输入验证
     */
    private ValidationResult validateBasicInput(String message, Map<String, Object> variables) {
        // 消息长度检查
        if (message == null || message.trim().isEmpty()) {
            return ValidationResult.invalid("消息内容不能为空");
        }
        
        if (message.length() > maxMessageLength) {
            return ValidationResult.invalid("消息长度超过限制: " + maxMessageLength);
        }
        
        // 变量数量和长度检查
        if (variables != null) {
            if (variables.size() > maxVariableCount) {
                return ValidationResult.invalid("变量数量超过限制: " + maxVariableCount);
            }
            
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                if (entry.getValue() != null) {
                    String valueStr = entry.getValue().toString();
                    if (valueStr.length() > maxVariableLength) {
                        return ValidationResult.invalid("变量值长度超过限制: " + entry.getKey());
                    }
                }
            }
        }
        
        return ValidationResult.valid();
    }

    /**
     * 检测提示词注入攻击
     */
    private ValidationResult detectPromptInjection(String message) {
        String normalizedMessage = message.toLowerCase().replaceAll("\\s+", " ");
        
        for (Pattern pattern : PROMPT_INJECTION_PATTERNS) {
            if (pattern.matcher(normalizedMessage).find()) {
                return ValidationResult.invalid("检测到潜在的提示词注入攻击");
            }
        }
        
        // 检查是否包含过多的特殊字符组合
        if (containsSuspiciousPatterns(normalizedMessage)) {
            return ValidationResult.invalid("检测到可疑的输入模式");
        }
        
        return ValidationResult.valid();
    }

    /**
     * 检测敏感数据
     */
    private ValidationResult detectSensitiveData(String message) {
        for (Pattern pattern : SENSITIVE_DATA_PATTERNS) {
            if (pattern.matcher(message).find()) {
                return ValidationResult.invalid("输入包含敏感数据，请移除后重试");
            }
        }
        
        return ValidationResult.valid();
    }

    /**
     * 检查是否包含可疑模式
     */
    private boolean containsSuspiciousPatterns(String message) {
        // 检查是否包含过多的分隔符或特殊字符
        long separatorCount = message.chars()
            .filter(c -> c == '{' || c == '}' || c == '[' || c == ']' || c == '#' || c == '*')
            .count();
        
        return separatorCount > message.length() * 0.1; // 如果特殊字符超过10%，认为可疑
    }

    /**
     * 清理和转义输入内容
     */
    public String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        
        // 移除或转义危险字符
        String sanitized = input
            .replaceAll("(?i)<script[^>]*>.*?</script>", "") // 移除script标签
            .replaceAll("(?i)javascript:", "") // 移除javascript协议
            .replaceAll("(?i)on\\w+\\s*=", "") // 移除事件处理器
            .replaceAll("\\x00|\\x08|\\x0B|\\x0C|\\x0E|\\x0F", ""); // 移除控制字符
        
        // 限制连续的特殊字符
        sanitized = sanitized.replaceAll("[{}\\[\\]#*]{3,}", "***");
        
        return sanitized.trim();
    }

    /**
     * 脱敏敏感信息
     */
    public String maskSensitiveData(String input) {
        if (input == null) {
            return null;
        }
        
        String masked = input;
        
        // 脱敏信用卡号
        masked = masked.replaceAll("\\b\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}[\\s-]?(\\d{4})\\b", "****-****-****-$1");
        
        // 脱敏邮箱
        masked = masked.replaceAll("\\b([A-Za-z0-9])[A-Za-z0-9._%+-]*@([A-Za-z0-9.-]+\\.[A-Z|a-z]{2,})\\b", "$1***@$2");
        
        // 脱敏手机号
        masked = masked.replaceAll("\\b(\\d{3})\\d{4}(\\d{4})\\b", "$1****$2");
        
        // 脱敏密码相关信息
        masked = masked.replaceAll("(?i)(password|pwd|passwd|secret|token|key)\\s*[:=]\\s*(\\S+)", "$1:***");
        
        return masked;
    }

    /**
     * 验证会话ID格式
     */
    public boolean isValidSessionId(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return false;
        }
        
        // 检查长度和格式
        return sessionId.length() <= 100 && 
               sessionId.matches("^[a-zA-Z0-9_-]+$");
    }

    /**
     * 验证应用ID
     */
    public boolean isValidAppId(Long appId) {
        return appId != null && appId > 0;
    }

    /**
     * 获取安全配置信息
     */
    public SecurityConfig getSecurityConfig() {
        return new SecurityConfig(
            maxMessageLength,
            maxVariableCount,
            maxVariableLength,
            enablePromptInjectionDetection,
            enableSensitiveDataDetection
        );
    }

    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        private final String errorCode;
        
        private ValidationResult(boolean valid, String errorMessage, String errorCode) {
            this.valid = valid;
            this.errorMessage = errorMessage;
            this.errorCode = errorCode;
        }
        
        public static ValidationResult valid() {
            return new ValidationResult(true, null, null);
        }
        
        public static ValidationResult invalid(String errorMessage) {
            return new ValidationResult(false, errorMessage, "VALIDATION_FAILED");
        }
        
        public static ValidationResult invalid(String errorMessage, String errorCode) {
            return new ValidationResult(false, errorMessage, errorCode);
        }
        
        public boolean isValid() { return valid; }
        public String getErrorMessage() { return errorMessage; }
        public String getErrorCode() { return errorCode; }
        
        @Override
        public String toString() {
            return String.format("ValidationResult{valid=%s, errorMessage='%s', errorCode='%s'}", 
                               valid, errorMessage, errorCode);
        }
    }

    /**
     * 安全配置类
     */
    public static class SecurityConfig {
        public final int maxMessageLength;
        public final int maxVariableCount;
        public final int maxVariableLength;
        public final boolean enablePromptInjectionDetection;
        public final boolean enableSensitiveDataDetection;
        
        public SecurityConfig(int maxMessageLength, int maxVariableCount, int maxVariableLength,
                            boolean enablePromptInjectionDetection, boolean enableSensitiveDataDetection) {
            this.maxMessageLength = maxMessageLength;
            this.maxVariableCount = maxVariableCount;
            this.maxVariableLength = maxVariableLength;
            this.enablePromptInjectionDetection = enablePromptInjectionDetection;
            this.enableSensitiveDataDetection = enableSensitiveDataDetection;
        }
        
        @Override
        public String toString() {
            return String.format("SecurityConfig{maxMessageLength=%d, maxVariableCount=%d, " +
                               "maxVariableLength=%d, promptInjectionDetection=%s, sensitiveDataDetection=%s}",
                               maxMessageLength, maxVariableCount, maxVariableLength,
                               enablePromptInjectionDetection, enableSensitiveDataDetection);
        }
    }
}