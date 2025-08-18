# AI模型审核功能说明

## 概述

本功能允许前端根据用户选择的AI模型ID动态创建ChatModel实例，实现灵活的内容审核。相比之前的静态模型注入方式，新功能支持：

1. **动态模型选择**：根据前端传递的modelId参数动态选择AI模型
2. **模型隔离**：不同审核请求可以使用不同的AI模型
3. **性能优化**：利用DynamicModelManager的缓存机制避免重复创建模型实例

## 技术架构

### 核心组件

- **ModerationWithModelRequest**: 包含内容和模型ID的请求DTO
- **LangChain4jModerationService**: 重构后的审核服务，支持动态模型创建
- **DynamicModelManager**: 模型管理器，负责根据modelId创建和缓存ChatModel实例
- **AdminModerationController**: 新增的API端点，支持模型参数

### 数据流

```
前端请求 → Controller → Service → DynamicModelManager → ChatModel → AI审核结果
```

## API接口

### 1. 完整AI内容审核

**端点**: `POST /api/admin/moderation/content/ai`

**请求体**:
```json
{
  "content": "待审核内容",
  "modelId": 123,
  "contentType": "MESSAGE",
  "sourceType": "CHAT",
  "sourceId": "msg_001",
  "userId": "user_001",
  "sessionId": "session_001"
}
```

**响应**:
```json
{
  "success": true,
  "data": {
    "content": "待审核内容",
    "result": "APPROVED",
    "riskLevel": "LOW",
    "confidence": 0.95,
    "processingTime": 150
  }
}
```

### 2. 快速AI预检

**端点**: `POST /api/admin/moderation/content/ai/quick`

**请求体**: 同完整审核

**响应**: 同完整审核，但处理速度更快

## 前端使用示例

### TypeScript接口调用

```typescript
import { moderationApi } from '@/api/smartcs/moderation'

// 完整审核
const result = await moderationApi.moderateContentWithModel({
  content: "用户输入的内容",
  modelId: 123,
  contentType: "MESSAGE",
  sourceType: "CHAT"
})

// 快速预检
const quickResult = await moderationApi.quickModerateWithModel({
  content: "用户输入的内容",
  modelId: 123
})
```

### Vue组件示例

```vue
<template>
  <div>
    <textarea v-model="content" placeholder="输入待审核内容"></textarea>
    <select v-model="selectedModelId">
      <option v-for="model in models" :key="model.id" :value="model.id">
        {{ model.name }}
      </option>
    </select>
    <button @click="moderateContent">开始审核</button>
    
    <div v-if="result">
      <h3>审核结果</h3>
      <p>结果: {{ result.result }}</p>
      <p>风险等级: {{ result.riskLevel }}</p>
      <p>置信度: {{ result.confidence }}</p>
      <p>处理时间: {{ result.processingTime }}ms</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { moderationApi } from '@/api/smartcs/moderation'

const content = ref('')
const selectedModelId = ref(1)
const result = ref(null)

const moderateContent = async () => {
  try {
    result.value = await moderationApi.moderateContentWithModel({
      content: content.value,
      modelId: selectedModelId.value
    })
  } catch (error) {
    console.error('审核失败:', error)
  }
}
</script>
```

## 配置说明

### 环境变量

```yaml
moderation:
  ai:
    enabled: true
    timeout-seconds: 10
    model-name: gpt-3.5-turbo  # 默认模型名称（已废弃，现在使用modelId）
```

### 模型配置

模型信息通过数据库中的模型表管理，包括：
- 模型ID
- 模型名称
- 提供商信息
- API密钥配置

## 错误处理

### 常见错误

1. **模型不存在**: 返回400错误，提示"Model ID不存在"
2. **模型不可用**: 返回503错误，提示"模型服务不可用"
3. **审核超时**: 返回408错误，提示"审核超时"
4. **内容为空**: 返回400错误，提示"审核内容不能为空"

### 错误响应格式

```json
{
  "success": false,
  "errCode": "MODEL_NOT_FOUND",
  "errMessage": "指定的模型不存在",
  "data": null
}
```

## 性能优化

### 缓存机制

- **模型实例缓存**: DynamicModelManager缓存已创建的ChatModel实例
- **审核结果缓存**: 可根据内容哈希缓存审核结果（待实现）

### 并发处理

- 使用CompletableFuture支持异步审核
- 支持批量内容审核
- 可配置超时时间避免长时间等待

## 监控和日志

### 关键日志

- 模型创建: `创建ChatModel实例: modelId={}`
- 审核开始: `开始AI模型审核，modelId: {}, contentLength: {}`
- 审核完成: `AI moderation completed in {}ms, modelId: {}, result: {}`
- 错误记录: `AI模型审核失败，modelId: {}, content: {}`

### 性能指标

- 审核响应时间
- 模型创建时间
- 错误率统计
- 模型使用频率

## 扩展计划

### 短期优化

1. 添加审核结果缓存
2. 支持批量模型审核
3. 增加更多模型提供商支持

### 长期规划

1. 智能模型选择算法
2. 多模型结果融合
3. 自适应审核策略
4. 实时模型性能监控

## 注意事项

1. **模型ID验证**: 确保传入的modelId在系统中存在且可用
2. **内容长度限制**: 建议单次审核内容不超过10,000字符
3. **并发限制**: 避免同时发起过多审核请求
4. **错误重试**: 对于临时性错误，建议实现重试机制
5. **成本控制**: 不同模型可能有不同的API调用成本，需要合理选择

## 技术支持

如有问题，请联系开发团队或查看相关日志文件。
