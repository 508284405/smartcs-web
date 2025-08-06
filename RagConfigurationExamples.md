# RAG组件配置使用示例

## 概述

本文档展示了如何使用新增的RAG组件配置功能来自定义AI聊天的检索增强生成行为。

## 基本使用

### 1. 默认配置（不指定ragConfig）

```json
{
  "appId": 123,
  "modelId": 456,
  "message": "请帮我分析当前的市场趋势",
  "sessionId": "optional-session-id"
}
```

### 2. 自定义内容聚合器配置

```json
{
  "appId": 123,
  "modelId": 456,
  "message": "请帮我分析当前的市场趋势",
  "ragConfig": {
    "contentAggregator": {
      "maxResults": 10,
      "minScore": 0.7
    }
  }
}
```

### 3. 自定义查询转换器配置

```json
{
  "appId": 123,
  "modelId": 456,
  "message": "什么是人工智能？",
  "ragConfig": {
    "queryTransformer": {
      "n": 3
    }
  }
}
```

### 4. 自定义Web搜索配置

```json
{
  "appId": 123,
  "modelId": 456,
  "message": "最新的AI技术发展",
  "ragConfig": {
    "webSearch": {
      "maxResults": 20,
      "timeout": 15
    }
  }
}
```

### 5. 自定义知识库搜索配置

```json
{
  "appId": 123,
  "modelId": 456,
  "message": "公司的产品规格",
  "ragConfig": {
    "knowledgeSearch": {
      "topK": 8,
      "scoreThreshold": 0.8
    }
  }
}
```

### 6. 综合配置示例

```json
{
  "appId": 123,
  "modelId": 456,
  "message": "请基于我们的知识库和最新网络信息，分析竞争对手的产品策略",
  "ragConfig": {
    "contentAggregator": {
      "maxResults": 15,
      "minScore": 0.6
    },
    "queryTransformer": {
      "n": 5
    },
    "queryRouter": {
      "enableKnowledgeRetrieval": true,
      "enableWebSearch": true,
      "enableSqlQuery": false
    },
    "webSearch": {
      "maxResults": 10,
      "timeout": 10
    },
    "knowledgeSearch": {
      "topK": 10,
      "scoreThreshold": 0.7
    }
  }
}
```

## 配置参数说明

### ContentAggregatorConfig（内容聚合器）
- `maxResults`: 最大结果数量，范围[1-50]，默认值：5
- `minScore`: 最小相关性分数，范围[0.0-1.0]，默认值：0.5

### QueryTransformerConfig（查询转换器）
- `n`: 查询扩展数量，范围[1-10]，默认值：5

### QueryRouterConfig（查询路由器）
- `enableKnowledgeRetrieval`: 是否启用知识库检索，默认值：true
- `enableWebSearch`: 是否启用Web搜索，默认值：true
- `enableSqlQuery`: 是否启用SQL查询检索，默认值：false

### WebSearchConfig（Web搜索）
- `maxResults`: Web搜索最大结果数量，范围[1-50]，默认值：10
- `timeout`: 搜索超时时间（秒），范围[1-60]，默认值：10

### KnowledgeSearchConfig（知识库搜索）
- `topK`: 返回的最相关结果数量，范围[1-100]，默认值：5
- `scoreThreshold`: 相关性分数阈值，范围[0.0-1.0]，默认值：0.7

## 参数验证

系统会自动验证所有配置参数：

1. **超出范围的参数会被自动修正为默认值**
2. **修正过程会记录警告日志**
3. **配置验证失败时会回退到默认配置**
4. **所有验证过程都有详细的日志记录**

## 性能考虑

1. **缓存策略**：
   - 使用默认配置的服务实例会被缓存
   - 使用自定义配置的服务实例不会被缓存
   - 建议在需要定制化时使用自定义配置

2. **最佳实践**：
   - 根据具体场景调整参数
   - 监控系统日志了解参数修正情况
   - 在生产环境中测试配置效果

## 错误处理

当配置验证失败时，系统会：
1. 记录错误日志
2. 使用默认配置继续执行
3. 确保服务的稳定性和可用性