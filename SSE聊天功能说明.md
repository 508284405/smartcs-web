# SSE聊天功能说明

## 概述

本项目已成功实现了基于Server-Sent Events (SSE) 的聊天功能，支持实时流式响应。

## 功能特性

- ✅ 实时流式响应：支持AI回答的逐步推送
- ✅ 进度提示：显示处理进度（开始处理、检索知识、构建提示、调用AI等）
- ✅ 错误处理：完善的错误处理和超时机制
- ✅ 会话管理：支持会话历史和上下文
- ✅ 知识检索：集成FAQ和文档向量检索
- ✅ 可配置参数：支持模型、温度、最大token等参数

## 架构设计

### 目录结构
```
smartcs-web-adapter/src/main/java/com/leyue/smartcs/sse/
├── ChatSSEController.java          # SSE聊天控制器
└── SSETestController.java          # SSE测试控制器

smartcs-web-client/src/main/java/com/leyue/smartcs/
├── api/BotSSEService.java          # SSE服务接口
└── dto/bot/
    ├── BotChatSSERequest.java      # SSE聊天请求DTO
    ├── BotChatSSEResponse.java     # SSE聊天响应DTO
    └── SSEMessage.java             # SSE消息封装DTO

smartcs-web-app/src/main/java/com/leyue/smartcs/bot/
├── service/BotSSEServiceImpl.java  # SSE服务实现
└── executor/ChatSSECmdExe.java     # SSE聊天命令执行器
```

### 核心组件

1. **ChatSSEController**: 处理HTTP SSE请求
2. **BotSSEService**: 业务服务接口
3. **ChatSSECmdExe**: 核心业务逻辑执行器
4. **SSEMessage**: 统一的SSE消息格式

## API接口

### 1. SSE聊天接口

**接口地址**: `POST /api/sse/chat`

**Content-Type**: `application/json`

**Accept**: `text/event-stream`

**请求参数**:
```json
{
  "sessionId": "可选，会话ID",
  "question": "必填，用户问题",
  "includeHistory": true,
  "model": "可选，模型名称",
  "temperature": 0.7,
  "maxTokens": 2000,
  "topK": 5,
  "timeout": 300000
}
```

**响应格式**:
```
event: start
id: start_sessionId
data: {"type":"start","data":"sessionId","timestamp":1234567890,"id":"start_sessionId"}

event: progress
id: progress_sessionId_timestamp
data: {"type":"progress","data":"开始处理您的问题...","timestamp":1234567890,"id":"progress_sessionId_timestamp"}

event: data
id: data_sessionId_timestamp
data: {"type":"data","data":{"sessionId":"xxx","answer":"部分回答","finished":false},"timestamp":1234567890,"id":"data_sessionId_timestamp"}

event: complete
id: complete_sessionId
data: {"type":"complete","data":{"sessionId":"xxx","answer":"完整回答","finished":true,"sources":[...],"processTime":5000},"timestamp":1234567890,"id":"complete_sessionId"}
```

### 2. SSE测试接口

**接口地址**: `GET /api/sse/test/simple`

**Accept**: `text/event-stream`

用于测试SSE连接是否正常工作。

### 3. 服务状态接口

**接口地址**: `GET /api/sse/status`

**响应**: `"SSE服务正常运行"`

## 消息类型

### 1. start - 开始消息
表示SSE连接建立，开始处理请求。

### 2. progress - 进度消息
显示当前处理进度：
- "开始处理您的问题..."
- "正在检索相关知识..."
- "正在构建AI提示..."
- "正在调用AI生成回答..."

### 3. data - 数据消息
包含AI生成的部分回答内容，支持流式显示。

### 4. complete - 完成消息
包含最终的完整回答和相关信息（知识来源、处理时间等）。

### 5. error - 错误消息
当处理过程中出现错误时发送。

### 6. timeout - 超时消息
当连接超时时发送。

## 前端集成示例

### JavaScript EventSource
```javascript
const eventSource = new EventSource('/api/sse/chat', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    question: "如何退款？",
    includeHistory: true
  })
});

eventSource.onmessage = function(event) {
  const message = JSON.parse(event.data);
  console.log('收到消息:', message);
  
  switch(message.type) {
    case 'start':
      console.log('开始处理');
      break;
    case 'progress':
      console.log('进度:', message.data);
      break;
    case 'data':
      console.log('部分回答:', message.data.answer);
      break;
    case 'complete':
      console.log('完成:', message.data);
      eventSource.close();
      break;
    case 'error':
      console.error('错误:', message.data);
      eventSource.close();
      break;
  }
};

eventSource.onerror = function(event) {
  console.error('SSE连接错误:', event);
};
```

### Fetch API (推荐)
```javascript
async function chatWithSSE(question) {
  const response = await fetch('/api/sse/chat', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'text/event-stream'
    },
    body: JSON.stringify({
      question: question,
      includeHistory: true
    })
  });

  const reader = response.body.getReader();
  const decoder = new TextDecoder();

  while (true) {
    const { done, value } = await reader.read();
    if (done) break;
    console.log("value: ", value)
    const chunk = decoder.decode(value);
    const lines = chunk.split('\n');

    for (const line of lines) {
      if (line.startsWith('data: ')) {
        const data = line.slice(6);
        if (data.trim()) {
          const message = JSON.parse(data);
          handleSSEMessage(message);
        }
      }
    }
  }
}

function handleSSEMessage(message) {
  switch(message.type) {
    case 'start':
      showProgress('开始处理...');
      break;
    case 'progress':
      showProgress(message.data);
      break;
    case 'data':
      appendAnswer(message.data.answer);
      break;
    case 'complete':
      showComplete(message.data);
      break;
    case 'error':
      showError(message.data);
      break;
  }
}
```

## 配置说明

### 超时配置
- 默认SSE连接超时：5分钟（300000毫秒）
- 可通过请求参数`timeout`自定义

### 模型配置
- 支持通过`model`参数指定使用的AI模型
- 支持`temperature`参数控制回答的随机性
- 支持`maxTokens`参数限制回答长度

### 知识检索配置
- 支持通过`topK`参数控制检索的知识条数
- 默认检索5条相关知识

## 错误处理

### 常见错误类型
1. **参数错误**: 问题为空等
2. **超时错误**: 连接超时
3. **AI服务错误**: LLM调用失败
4. **知识检索错误**: 检索服务异常

### 错误响应格式
```json
{
  "type": "error",
  "data": "错误描述信息",
  "timestamp": 1234567890,
  "id": "error_sessionId"
}
```

## 性能优化

1. **异步处理**: 使用CompletableFuture异步处理请求
2. **流式响应**: 支持AI回答的实时推送
3. **连接管理**: 自动处理连接超时和错误
4. **资源释放**: 及时释放SSE连接资源

## 监控和日志

### 关键日志
- SSE连接建立和关闭
- 处理进度和耗时
- 错误和异常信息
- 知识检索结果

### 监控指标
- SSE连接数
- 平均响应时间
- 错误率
- 超时率

## 部署注意事项

1. **反向代理配置**: 确保Nginx等反向代理支持SSE
2. **防火墙设置**: 确保SSE端口可访问
3. **负载均衡**: 注意SSE连接的粘性会话
4. **资源限制**: 合理设置连接数和超时时间

## 测试方法

### 1. 使用curl测试
```bash
curl -N -H "Accept: text/event-stream" \
     -H "Content-Type: application/json" \
     -d '{"question":"如何退款？"}' \
     http://localhost:8080/api/sse/chat
```

### 2. 使用浏览器测试
访问: `http://localhost:8080/api/sse/test/simple`

### 3. 使用Postman测试
设置Accept头为`text/event-stream`，发送POST请求到SSE接口。

## 故障排查

### 常见问题
1. **连接立即断开**: 检查Accept头是否正确
2. **无法接收消息**: 检查防火墙和代理配置
3. **超时过快**: 调整timeout参数
4. **乱码问题**: 确保UTF-8编码

### 调试方法
1. 查看服务器日志
2. 使用浏览器开发者工具
3. 检查网络连接状态
4. 验证请求参数格式 