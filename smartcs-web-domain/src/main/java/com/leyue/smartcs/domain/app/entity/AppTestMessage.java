package com.leyue.smartcs.domain.app.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * AI应用测试消息实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppTestMessage {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 消息ID
     */
    private String messageId;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * AI应用ID
     */
    private Long appId;
    
    /**
     * 消息类型
     */
    private MessageType messageType;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 消息变量（用户消息时使用）
     */
    private Map<String, Object> variables;
    
    /**
     * 模型信息（AI回复时使用）
     */
    private Map<String, Object> modelInfo;
    
    /**
     * Token使用情况
     */
    private Map<String, Object> tokenUsage;
    
    /**
     * 处理时间（毫秒）
     */
    private Integer processTime;
    
    /**
     * 消息费用
     */
    private BigDecimal cost;
    
    /**
     * 消息状态
     */
    private MessageStatus status;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 消息时间戳（毫秒）
     */
    private Long timestamp;
    
    /**
     * 创建时间
     */
    private Long createdAt;
    
    /**
     * 更新时间
     */
    private Long updatedAt;
    
    /**
     * 创建者
     */
    private String createdBy;
    
    /**
     * 更新者
     */
    private String updatedBy;
    
    /**
     * 消息类型枚举
     */
    public enum MessageType {
        USER("USER", "用户消息"),
        ASSISTANT("ASSISTANT", "AI助手消息"),
        SYSTEM("SYSTEM", "系统消息");
        
        private final String code;
        private final String description;
        
        MessageType(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
        
        public static MessageType fromCode(String code) {
            for (MessageType type : values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown message type code: " + code);
        }
    }
    
    /**
     * 消息状态枚举
     */
    public enum MessageStatus {
        SUCCESS("SUCCESS", "成功"),
        FAILED("FAILED", "失败"),
        PROCESSING("PROCESSING", "处理中");
        
        private final String code;
        private final String description;
        
        MessageStatus(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
        
        public static MessageStatus fromCode(String code) {
            for (MessageStatus status : values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown message status code: " + code);
        }
    }
    
    /**
     * 创建用户消息
     */
    public static AppTestMessage createUserMessage(String messageId, String sessionId, Long appId,
                                                  String content, Map<String, Object> variables) {
        long now = System.currentTimeMillis();
        return AppTestMessage.builder()
                .messageId(messageId)
                .sessionId(sessionId)
                .appId(appId)
                .messageType(MessageType.USER)
                .content(content)
                .variables(variables)
                .status(MessageStatus.SUCCESS)
                .timestamp(now)
                .cost(BigDecimal.ZERO)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
    
    /**
     * 创建AI助手消息
     */
    public static AppTestMessage createAssistantMessage(String messageId, String sessionId, Long appId,
                                                       String content, Map<String, Object> modelInfo,
                                                       Map<String, Object> tokenUsage, Integer processTime,
                                                       BigDecimal cost) {
        long now = System.currentTimeMillis();
        return AppTestMessage.builder()
                .messageId(messageId)
                .sessionId(sessionId)
                .appId(appId)
                .messageType(MessageType.ASSISTANT)
                .content(content)
                .modelInfo(modelInfo)
                .tokenUsage(tokenUsage)
                .processTime(processTime)
                .cost(cost != null ? cost : BigDecimal.ZERO)
                .status(MessageStatus.SUCCESS)
                .timestamp(now)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
    
    /**
     * 标记为失败
     */
    public void markAsFailed(String errorMessage) {
        this.status = MessageStatus.FAILED;
        this.errorMessage = errorMessage;
        this.updatedAt = System.currentTimeMillis();
    }
    
    /**
     * 标记为处理中
     */
    public void markAsProcessing() {
        this.status = MessageStatus.PROCESSING;
        this.updatedAt = System.currentTimeMillis();
    }
    
    /**
     * 标记为成功
     */
    public void markAsSuccess() {
        this.status = MessageStatus.SUCCESS;
        this.updatedAt = System.currentTimeMillis();
    }
}