package com.leyue.smartcs.dto.app;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * AI应用聊天命令
 * 
 * <p>包含聊天请求的所有必要参数，支持自定义RAG配置以优化检索增强生成效果。</p>
 * 
 * <h3>基本使用示例:</h3>
 * <pre>{@code
 * AiAppChatCmd cmd = new AiAppChatCmd();
 * cmd.setAppId(123L);
 * cmd.setModelId(456L);
 * cmd.setMessage("你好，请帮我分析数据");
 * }</pre>
 * 
 * <h3>自定义RAG配置示例:</h3>
 * <pre>{@code
 * RagComponentConfig ragConfig = RagComponentConfig.builder()
 *     .contentAggregator(RagComponentConfig.ContentAggregatorConfig.builder()
 *         .maxResults(10)
 *         .minScore(0.7)
 *         .build())
 *     .queryTransformer(RagComponentConfig.QueryTransformerConfig.builder()
 *         .n(3)
 *         .build())
 *     .build();
 * 
 * AiAppChatCmd cmd = new AiAppChatCmd();
 * cmd.setRagConfig(ragConfig);
 * }</pre>
 * 
 * @see RagComponentConfig
 */
@Data
public class AiAppChatCmd {

    /**
     * 应用ID
     */
    @NotNull(message = "应用ID不能为空")
    private Long appId;

    /**
     * 用户消息内容（可选）。当图片存在时可为空。
     */
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
     * 图片URL列表（可选）。当文本为空时，至少需要一张图片。
     * 建议为公网可访问的 http/https URL。
     */
    @Size(max = 5, message = "单次最多支持上传5张图片")
    private List<String> imageUrls;

    /**
     * 推理参数（JSON格式）
     */
    private String inferenceParams;

    /**
     * RAG组件配置（可选，不指定则使用默认配置）
     * 
     * <p>允许前端自定义RAG各个组件的参数，包括：</p>
     * <ul>
     *   <li>内容聚合器：控制返回结果数量和相关性阈值</li>
     *   <li>查询转换器：控制查询扩展的数量</li>
     *   <li>查询路由器：控制启用哪些检索器</li>
     *   <li>Web搜索：控制搜索结果数量和超时时间</li>
     *   <li>知识库搜索：控制返回的最相关结果数量和分数阈值</li>
     * </ul>
     * 
     * <p>如果不指定此字段或字段为null，系统将使用以下默认配置：</p>
     * <ul>
     *   <li>内容聚合器：maxResults=5, minScore=0.5</li>
     *   <li>查询转换器：n=5</li>
     *   <li>Web搜索：maxResults=10, timeout=10</li>
     *   <li>知识库搜索：topK=5, scoreThreshold=0.7</li>
     * </ul>
     */
    @Valid
    private RagComponentConfig ragConfig;

    /**
     * 连接超时时间（毫秒）
     */
    private Long timeout = 300000L;

    /**
     * 跨字段校验：文本与图片至少提供一个
     */
    @AssertTrue(message = "消息内容与图片至少需要提供一个")
    public boolean isTextOrImagesPresent() {
        boolean hasText = message != null && !message.trim().isEmpty();
        boolean hasImages = imageUrls != null && !imageUrls.isEmpty();
        return hasText || hasImages;
    }
}
