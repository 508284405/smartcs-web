package com.leyue.smartcs.rag.query.pipeline;

import dev.langchain4j.rag.query.Query;

import java.util.Collection;

/**
 * 查询转换器阶段接口
 * 定义了查询转换管线中单个阶段的处理契约
 * 
 * @author Claude
 */
public interface QueryTransformerStage {
    
    /**
     * 应用当前阶段的转换逻辑
     * 
     * @param context 查询上下文，包含原始查询、配置、预算控制等信息
     * @param queries 输入的查询集合
     * @return 转换后的查询集合
     * @throws QueryTransformationException 当转换过程中发生错误时抛出
     */
    Collection<Query> apply(QueryContext context, Collection<Query> queries);
    
    /**
     * 获取阶段名称，用于日志记录和监控
     * 
     * @return 阶段名称
     */
    String getName();
    
    /**
     * 检查当前阶段是否已启用
     * 
     * @param context 查询上下文
     * @return 是否启用
     */
    default boolean isEnabled(QueryContext context) {
        return true;
    }
    
    /**
     * 阶段初始化钩子
     * 在管线开始执行前调用，可用于资源准备
     * 
     * @param context 查询上下文
     */
    default void initialize(QueryContext context) {
        // 默认空实现
    }
    
    /**
     * 阶段清理钩子
     * 在管线执行完成后调用，可用于资源释放
     * 
     * @param context 查询上下文
     */
    default void cleanup(QueryContext context) {
        // 默认空实现
    }
}