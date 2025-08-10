package com.leyue.smartcs.common.secret;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * 日志脱敏工具类
 * 用于在请求、响应、异常和审计日志中脱敏敏感信息
 * 
 * @author Claude
 */
@Slf4j
public class LogDesensitizationUtil {
    
    /**
     * 敏感字段名称集合（不区分大小写）
     */
    private static final Set<String> SENSITIVE_FIELDS = Set.of(
        "apikey", "api_key", "accesskey", "access_key", 
        "secret", "secretkey", "secret_key", "token", 
        "authorization", "password", "pwd", "passphrase",
        "privatekey", "private_key", "certificate", "cert"
    );
    
    /**
     * 敏感字段的正则表达式模式
     */
    private static final Pattern SENSITIVE_PATTERN = Pattern.compile(
        "(?i)(apikey|api_key|accesskey|access_key|secret|secretkey|secret_key|" +
        "token|authorization|password|pwd|passphrase|privatekey|private_key|" +
        "certificate|cert)\\s*[=:]\\s*[\"']?([^\\s,\"';}]+)",
        Pattern.CASE_INSENSITIVE
    );
    
    /**
     * 脱敏替换字符
     */
    private static final String MASK_CHAR = "••••••••••••••••";
    
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    /**
     * 脱敏JSON对象中的敏感字段
     * 
     * @param jsonObject JSON对象
     * @return 脱敏后的JSON对象
     */
    public static Object desensitizeJsonObject(Object jsonObject) {
        if (jsonObject == null) {
            return null;
        }
        
        try {
            String jsonString = OBJECT_MAPPER.writeValueAsString(jsonObject);
            String desensitizedString = desensitizeJsonString(jsonString);
            return OBJECT_MAPPER.readValue(desensitizedString, Object.class);
        } catch (JsonProcessingException e) {
            log.warn("JSON脱敏处理失败，返回原对象", e);
            return jsonObject;
        }
    }
    
    /**
     * 脱敏JSON字符串中的敏感字段
     * 
     * @param jsonString JSON字符串
     * @return 脱敏后的JSON字符串
     */
    public static String desensitizeJsonString(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return jsonString;
        }
        
        try {
            JsonNode jsonNode = OBJECT_MAPPER.readTree(jsonString);
            JsonNode desensitizedNode = desensitizeJsonNode(jsonNode);
            return OBJECT_MAPPER.writeValueAsString(desensitizedNode);
        } catch (JsonProcessingException e) {
            log.warn("JSON字符串脱敏处理失败，使用正则表达式脱敏", e);
            return desensitizeStringWithRegex(jsonString);
        }
    }
    
    /**
     * 脱敏普通字符串中的敏感信息
     * 
     * @param text 原始文本
     * @return 脱敏后的文本
     */
    public static String desensitizeString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        
        // 先尝试JSON脱敏
        if (isJsonLike(text)) {
            try {
                return desensitizeJsonString(text);
            } catch (Exception e) {
                // 如果JSON解析失败，继续使用正则表达式
            }
        }
        
        return desensitizeStringWithRegex(text);
    }
    
    /**
     * 递归脱敏JsonNode
     */
    private static JsonNode desensitizeJsonNode(JsonNode node) {
        if (node == null) {
            return null;
        }
        
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            ObjectNode result = OBJECT_MAPPER.createObjectNode();
            
            objectNode.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey();
                JsonNode value = entry.getValue();
                
                if (isSensitiveField(fieldName)) {
                    // 敏感字段替换为掩码
                    result.put(fieldName, MASK_CHAR);
                } else {
                    // 递归处理非敏感字段
                    result.set(fieldName, desensitizeJsonNode(value));
                }
            });
            
            return result;
        } else if (node.isArray()) {
            node.forEach(element -> desensitizeJsonNode(element));
        }
        
        return node;
    }
    
    /**
     * 使用正则表达式脱敏字符串
     */
    private static String desensitizeStringWithRegex(String text) {
        return SENSITIVE_PATTERN.matcher(text)
                .replaceAll(matchResult -> {
                    String fieldName = matchResult.group(1);
                    return fieldName + "=" + MASK_CHAR;
                });
    }
    
    /**
     * 判断字段名是否为敏感字段
     */
    private static boolean isSensitiveField(String fieldName) {
        if (fieldName == null) {
            return false;
        }
        
        String lowerFieldName = fieldName.toLowerCase();
        return SENSITIVE_FIELDS.contains(lowerFieldName);
    }
    
    /**
     * 判断字符串是否像JSON格式
     */
    private static boolean isJsonLike(String text) {
        if (text == null) {
            return false;
        }
        
        String trimmed = text.trim();
        return (trimmed.startsWith("{") && trimmed.endsWith("}")) ||
               (trimmed.startsWith("[") && trimmed.endsWith("]"));
    }
    
    /**
     * 脱敏异常堆栈信息
     * 
     * @param throwable 异常对象
     * @return 脱敏后的异常信息
     */
    public static String desensitizeThrowable(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(throwable.getClass().getName());
        
        if (throwable.getMessage() != null) {
            sb.append(": ").append(desensitizeString(throwable.getMessage()));
        }
        
        // 只记录前几层堆栈，避免过长
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        if (stackTrace != null && stackTrace.length > 0) {
            int maxLevels = Math.min(5, stackTrace.length);
            for (int i = 0; i < maxLevels; i++) {
                sb.append("\n\tat ").append(stackTrace[i].toString());
            }
            if (stackTrace.length > maxLevels) {
                sb.append("\n\t... ").append(stackTrace.length - maxLevels).append(" more");
            }
        }
        
        // 处理原因异常
        if (throwable.getCause() != null && throwable.getCause() != throwable) {
            sb.append("\nCaused by: ").append(desensitizeThrowable(throwable.getCause()));
        }
        
        return sb.toString();
    }
    
    /**
     * 脱敏URL参数
     * 
     * @param url 原始URL
     * @return 脱敏后的URL
     */
    public static String desensitizeUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return url;
        }
        
        // 脱敏URL中的敏感参数
        return SENSITIVE_PATTERN.matcher(url)
                .replaceAll(matchResult -> {
                    String fieldName = matchResult.group(1);
                    return fieldName + "=" + MASK_CHAR;
                });
    }
}