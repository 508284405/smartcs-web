package com.leyue.smartcs.model.gateway;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.scoring.ScoringModel;

/**
 * 模型提供者端口接口
 * <p>
 * 定义了获取不同类型LLM模型实例的核心能力，作为基础设施层的稳定接口，
 * 供应用层和基础设施层的各个组件使用，避免直接依赖具体的模型管理实现。
 * </p>
 * 
 * <h3>设计原则:</h3>
 * <ul>
 *   <li>单一职责：仅负责模型实例提供，不涉及业务组件装配</li>
 *   <li>依赖倒置：上层组件依赖此抽象接口，而非具体实现</li>
 *   <li>接口隔离：提供最小化但完整的模型获取能力</li>
 * </ul>
 * 
 * <h3>实现要求:</h3>
 * <ul>
 *   <li>实现类应提供模型实例缓存机制以提升性能</li>
 *   <li>应处理模型不存在或配置错误的异常情况</li>
 *   <li>应支持多种模型提供商（OpenAI、Claude等）</li>
 * </ul>
 * 
 * @author Claude
 * @see dev.langchain4j.model.chat.ChatModel
 * @see dev.langchain4j.model.chat.StreamingChatModel
 * @see dev.langchain4j.model.embedding.EmbeddingModel
 * @see dev.langchain4j.model.scoring.ScoringModel
 */
public interface ModelProvider {
    
    /**
     * 根据模型ID获取ChatModel实例
     * <p>
     * 用于执行同步的文本生成任务，适合需要等待完整响应的场景，
     * 如SQL生成、意图识别、查询扩展等。
     * </p>
     * 
     * @param modelId 模型ID，必须是系统中已配置的有效模型标识
     * @return ChatModel实例，可用于执行文本生成任务
     * @throws IllegalArgumentException 当模型ID不存在时抛出
     * @throws RuntimeException 当模型配置错误或创建失败时抛出
     */
    ChatModel getChatModel(Long modelId);
    
    /**
     * 根据模型ID获取StreamingChatModel实例
     * <p>
     * 用于执行流式文本生成任务，适合需要实时响应的场景，
     * 如聊天对话、实时内容生成等用户交互界面。
     * </p>
     * 
     * @param modelId 模型ID，必须是系统中已配置的有效模型标识
     * @return StreamingChatModel实例，可用于执行流式文本生成任务
     * @throws IllegalArgumentException 当模型ID不存在时抛出
     * @throws RuntimeException 当模型配置错误或创建失败时抛出
     */
    StreamingChatModel getStreamingChatModel(Long modelId);
    
    /**
     * 根据模型ID获取EmbeddingModel实例
     * <p>
     * 用于执行文本向量化任务，适合语义搜索、相似度计算、
     * 知识库检索等需要向量表示的场景。
     * </p>
     * 
     * @param modelId 模型ID，必须是系统中已配置的有效模型标识
     * @return EmbeddingModel实例，可用于执行文本向量化任务
     * @throws IllegalArgumentException 当模型ID不存在时抛出
     * @throws RuntimeException 当模型配置错误或创建失败时抛出
     */
    EmbeddingModel getEmbeddingModel(Long modelId);
    
    /**
     * 根据模型ID获取ScoringModel实例
     * <p>
     * 用于执行文本相关性打分任务，适合RAG场景下的内容重排序，
     * 通过AI模型评估查询与文档段落的相关性分数。
     * </p>
     * 
     * @param modelId 模型ID，必须是系统中已配置的有效模型标识
     * @return ScoringModel实例，可用于执行文本相关性打分任务
     * @throws IllegalArgumentException 当模型ID不存在时抛出
     * @throws RuntimeException 当模型配置错误或创建失败时抛出
     */
    ScoringModel getScoringModel(Long modelId);
    
    /**
     * 检查模型是否支持推理
     * <p>
     * 用于在执行推理任务前验证模型的可用性，避免运行时错误。
     * 检查包括模型配置状态、连接状态等。
     * </p>
     * 
     * @param modelId 模型ID
     * @return true 如果模型支持推理，false 如果模型不可用
     */
    boolean supportsInference(Long modelId);
    
    /**
     * 检查模型是否支持流式推理
     * <p>
     * 用于在执行流式推理任务前验证模型的流式能力，
     * 部分模型可能不支持流式输出。
     * </p>
     * 
     * @param modelId 模型ID
     * @return true 如果模型支持流式推理，false 如果不支持
     */
    boolean supportsStreaming(Long modelId);
}