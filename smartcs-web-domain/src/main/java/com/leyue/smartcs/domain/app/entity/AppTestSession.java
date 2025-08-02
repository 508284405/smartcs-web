package com.leyue.smartcs.domain.app.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * AI应用测试会话实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppTestSession {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * AI应用ID
     */
    private Long appId;
    
    /**
     * 会话名称
     */
    private String sessionName;
    
    /**
     * 使用的模型ID
     */
    private Long modelId;
    
    /**
     * 测试用户ID
     */
    private Long userId;
    
    /**
     * 会话配置（变量、参数等）
     */
    private Map<String, Object> sessionConfig;
    
    /**
     * 会话状态
     */
    private SessionState sessionState;
    
    /**
     * 消息总数
     */
    private Integer messageCount;
    
    /**
     * 最后消息时间（毫秒时间戳）
     */
    private Long lastMessageTime;
    
    /**
     * 会话开始时间（毫秒时间戳）
     */
    private Long startTime;
    
    /**
     * 会话结束时间（毫秒时间戳）
     */
    private Long endTime;
    
    /**
     * 总消耗Token数
     */
    private Integer totalTokens;
    
    /**
     * 总费用
     */
    private BigDecimal totalCost;
    
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
     * 会话状态枚举
     */
    public enum SessionState {
        ACTIVE("ACTIVE", "活跃"),
        FINISHED("FINISHED", "已完成"),
        EXPIRED("EXPIRED", "已过期");
        
        private final String code;
        private final String description;
        
        SessionState(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
        
        public static SessionState fromCode(String code) {
            for (SessionState state : values()) {
                if (state.code.equals(code)) {
                    return state;
                }
            }
            throw new IllegalArgumentException("Unknown session state code: " + code);
        }
    }
    
    /**
     * 创建新会话
     */
    public static AppTestSession createNew(String sessionId, Long appId, Long modelId, Long userId,
                                          Map<String, Object> sessionConfig) {
        long now = System.currentTimeMillis();
        return AppTestSession.builder()
                .sessionId(sessionId)
                .appId(appId)
                .sessionName("测试会话")
                .modelId(modelId)
                .userId(userId)
                .sessionConfig(sessionConfig)
                .sessionState(SessionState.ACTIVE)
                .messageCount(0)
                .startTime(now)
                .lastMessageTime(now)
                .totalTokens(0)
                .totalCost(BigDecimal.ZERO)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
    
    /**
     * 更新统计信息
     */
    public void updateStats(Integer messageCount, Long lastMessageTime, Integer totalTokens, BigDecimal totalCost) {
        this.messageCount = messageCount;
        this.lastMessageTime = lastMessageTime;
        this.totalTokens = totalTokens;
        this.totalCost = totalCost;
        this.updatedAt = System.currentTimeMillis();
    }
    
    /**
     * 结束会话
     */
    public void finish() {
        this.sessionState = SessionState.FINISHED;
        this.endTime = System.currentTimeMillis();
        this.updatedAt = this.endTime;
    }
    
    /**
     * 会话是否活跃
     */
    public boolean isActive() {
        return SessionState.ACTIVE.equals(this.sessionState);
    }
    
    /**
     * 更新会话状态
     */
    public void updateStatus(SessionState status) {
        this.sessionState = status;
        this.updatedAt = System.currentTimeMillis();
        if (SessionState.FINISHED.equals(status) || SessionState.EXPIRED.equals(status)) {
            this.endTime = this.updatedAt;
        }
    }
    
    /**
     * 获取会话状态
     */
    public SessionState getStatus() {
        return this.sessionState;
    }
}