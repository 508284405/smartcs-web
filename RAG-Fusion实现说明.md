# RAG-Fusion 实现说明

## 概述

本文档描述了在 smartcs-web 项目中实现的 RAG-Fusion 技术，用于提升 RAG（Retrieval-Augmented Generation）系统的检索准确性和答案相关性。

## RAG-Fusion 技术介绍

RAG-Fusion 是一种增强型检索技术，通过以下方式提升传统 RAG 系统的性能：

1. **Query 多样化生成**：针对同一个用户问题生成多个不同的检索查询
2. **多轮并发检索**：使用多个查询并发检索向量数据库
3. **结果融合**：对多轮检索结果进行去重、排序和融合
4. **上下文优化**：构建更全面、相关性更高的上下文

## 实现架构

### 核心组件

#### 1. FusionQuestionAnswerAdvisor
- **位置**：`smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/bot/advisor/FusionQuestionAnswerAdvisor.java`
- **功能**：实现 RAG-Fusion 的核心逻辑
- **主要方法**：
  - `performFusionSearch(String userQuery)`：执行融合检索
  - `generateQueries(String originalQuery)`：生成查询变体
  - `batchRetrieve(List<String> queries)`：批量检索
  - `fuseDocuments(List<Document> allDocuments)`：融合检索结果
  - `buildContext(List<Document> documents)`：构建上下文

#### 2. LLMGatewayImpl 增强
- **位置**：`smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/bot/gatewayimpl/LLMGatewayImpl.java`
- **增强功能**：
  - 新增 `buildChatClientWithFusion` 方法
  - 修改 `generateAnswer` 和 `generateAnswerStream` 方法支持 RAG-Fusion
  - 在 RAG 模式下自动使用 RAG-Fusion 技术

### 配置类

#### FusionConfig
提供可配置的参数：
- `maxQueries`：最大查询变体数量（默认：5）
- `topK`：每次检索返回的文档数量（默认：3）
- `similarityThreshold`：相似度阈值（默认：0.7）
- `maxFusedDocuments`：融合后的最大文档数量（默认：10）
- `maxContextTokens`：最大上下文 token 数量（默认：4000）

## 实现细节

### Query 多样化策略

当前实现了简单的查询变体生成策略：
```java
List<String> queries = new ArrayList<>();
queries.add(originalQuery); // 原始query
queries.add("如何 " + originalQuery);
queries.add("什么是 " + originalQuery);
queries.add(originalQuery + " 的解决方案");
queries.add(originalQuery + " 相关信息");
```

### 并发检索机制

使用 `CompletableFuture` 实现并发检索：
```java
List<CompletableFuture<List<Document>>> futures = queries.stream()
    .map(query -> CompletableFuture.supplyAsync(() -> {
        // 执行向量检索
        return vectorStore.similaritySearch(searchRequest);
    }))
    .collect(Collectors.toList());
```

### 结果融合算法

1. **去重**：按文档内容去重
2. **计分**：统计文档在多次检索中的出现频次
3. **排序**：按出现频次降序排列
4. **限制**：限制最终文档数量

### 上下文构建

1. **Token 估算**：简单按字符数/4估算 token 数量
2. **截断控制**：超过最大 token 限制时截断
3. **格式化**：将文档内容拼接为连贯的上下文

## 使用方式

### 1. 直接调用
```java
FusionQuestionAnswerAdvisor fusionAdvisor = new FusionQuestionAnswerAdvisor(vectorStore);
String context = fusionAdvisor.performFusionSearch("用户问题");
```

### 2. 通过 LLMGateway
```java
// 启用 RAG 模式时自动使用 RAG-Fusion
llmGateway.generateAnswer(sessionId, question, botId, true);
```

### 3. 自定义配置
```java
FusionConfig config = new FusionConfig();
config.setMaxQueries(3);
config.setTopK(5);
config.setSimilarityThreshold(0.8);

FusionQuestionAnswerAdvisor fusionAdvisor = new FusionQuestionAnswerAdvisor(vectorStore, config);
```

## 测试

### 单元测试
- **位置**：`smartcs-web-infrastructure/src/test/java/com/leyue/smartcs/bot/advisor/FusionQuestionAnswerAdvisorTest.java`
- **覆盖场景**：
  - 正常查询处理
  - 空查询和 null 查询处理
  - 无检索结果处理
  - 异常处理
  - 自定义配置测试
  - 配置类测试

### 测试运行
```bash
mvn test -Dtest=FusionQuestionAnswerAdvisorTest -pl smartcs-web-infrastructure
```

## 性能优化

### 1. 并发检索
- 使用 `CompletableFuture` 并发执行多个查询
- 减少总体检索时间

### 2. 异常处理
- 单个查询失败不影响整体流程
- 提供降级机制

### 3. 内存优化
- 使用流式处理避免大量内存占用
- 及时释放不需要的中间结果

## 扩展点

### 1. Query 生成策略
- 可集成 LLM 生成更智能的查询变体
- 支持基于实体、同义词的查询扩展
- 支持多语言查询生成

### 2. 融合算法
- 支持更复杂的相关性评分
- 引入语义相似度计算
- 支持时间衰减等因素

### 3. 上下文构建
- 支持更精确的 token 计算
- 支持智能截断（保留完整句子）
- 支持多模态内容融合

## 配置示例

### application.yaml
```yaml
smartcs:
  rag-fusion:
    max-queries: 5
    top-k: 3
    similarity-threshold: 0.7
    max-fused-documents: 10
    max-context-tokens: 4000
```

## 监控和日志

### 关键日志
- 查询变体生成：`生成查询变体: {}`
- 批量检索结果：`批量检索获得文档数: {}`
- 融合结果：`融合后文档数: {}`
- 异常处理：`RAG-Fusion处理失败: {}`

### 性能指标
- 检索延迟
- 融合效果（去重率、相关性提升）
- 上下文质量

## 注意事项

1. **向量数据库性能**：多查询会增加向量数据库负载
2. **Token 消耗**：更长的上下文会增加 LLM token 消耗
3. **延迟权衡**：需要在检索质量和响应时间之间平衡
4. **配置调优**：需要根据具体业务场景调整参数

## 未来改进

1. **智能查询生成**：使用 LLM 生成更相关的查询变体
2. **动态参数调整**：根据查询类型动态调整检索参数
3. **结果缓存**：缓存常见查询的融合结果
4. **A/B 测试**：对比传统 RAG 和 RAG-Fusion 的效果
5. **多模态支持**：支持图片、表格等多种类型的检索和融合 