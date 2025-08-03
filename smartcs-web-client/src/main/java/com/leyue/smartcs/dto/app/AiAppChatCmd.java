package com.leyue.smartcs.dto.app;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * AI应用聊天命令
 */
@Data
public class AiAppChatCmd {

    /**
     * 应用ID
     */
    @NotNull(message = "应用ID不能为空")
    private Long appId;

    /**
     * 用户消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 4000, message = "消息内容长度不能超过4000个字符")
    private String message;

    /**
     * 模板变量映射
     */
    private Map<String, Object> variables;

    /**
     * 会话ID（可选，不指定则创建新会话）
     */
    private String sessionId;

    @NotNull(message = "模型ID不能为空")
    private Long modelId;

    /**
     * 知识库ID列表（可选，支持多选）
     */
    private List<Long> knowledgeBaseIds;

    /**
     * 推理参数（JSON格式）
     */
    private String inferenceParams;

    /**
     * 连接超时时间（毫秒）
     */
    private Long timeout = 300000L;
}