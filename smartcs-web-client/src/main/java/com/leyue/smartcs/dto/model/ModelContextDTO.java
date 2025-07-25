package com.leyue.smartcs.dto.model;

import com.alibaba.cola.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 模型上下文DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ModelContextDTO extends DTO {

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 模型ID
     */
    private Long modelId;

    /**
     * 上下文消息列表
     */
    private List<ContextMessage> messages;

    /**
     * 上下文窗口大小
     */
    private Integer contextWindow;

    /**
     * 当前上下文长度
     */
    private Integer currentLength;

    /**
     * 创建时间（毫秒时间戳）
     */
    private Long createdAt;

    /**
     * 最后更新时间（毫秒时间戳）
     */
    private Long updatedAt;

    /**
     * 上下文消息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContextMessage {
        /**
         * 消息角色（user, assistant, system）
         */
        private String role;

        /**
         * 消息内容
         */
        private String content;

        /**
         * 消息时间戳
         */
        private Long timestamp;
    }
}