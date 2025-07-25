package com.leyue.smartcs.dto.model;

import com.alibaba.cola.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 模型推理响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ModelInferResponse extends DTO {

    /**
     * 任务ID（异步推理时返回）
     */
    private String taskId;

    /**
     * 推理结果内容
     */
    private String content;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 使用的token数量
     */
    private Integer tokenUsage;

    /**
     * 推理耗时（毫秒）
     */
    private Long inferenceTime;

    /**
     * 是否来自缓存
     */
    private Boolean fromCache;

    /**
     * 错误信息（如果有）
     */
    private String errorMessage;

    /**
     * 推理状态
     */
    private String status;
}