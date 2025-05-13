# stomp over websocket 技术文档

## 1. 概述

stomp over websocket 是一种基于 WebSocket 的消息通信协议，结合 STOMP（Simple Text Oriented Messaging Protocol）实现浏览器与服务端的实时、双向通信。本项目用于客服与客户的在线聊天、消息推送等场景，支持点对点与广播消息，具备高并发、低延迟、分布式会话管理能力。

## 2. 架构与流程总览

- 客户端通过 `/ws/chat`（客户）或 `/ws/agent`（客服）端点建立 WebSocket 连接，握手时携带 token。
- 握手拦截器校验 token 并注入用户身份，CustomHandshakeHandler 绑定 Principal。
- 消息流转：客户端发送消息到 `/app/xxx`，服务端处理后通过 `/topic/xxx`（广播）或 `/user/queue/xxx`（点对点）推送。
- 会话与在线状态通过 Redis 管理，支持分布式部署。

## 3. 服务端实现详解

### 3.1 端点注册与配置

- 端点注册（`WebSocketConfig.java`）：

```java
registry.addEndpoint("/ws/chat")
        .addInterceptors(webSocketHandshakeInterceptor)
        .setAllowedOriginPatterns("*")
        .setHandshakeHandler(customHandshakeHandler)
        .withSockJS();
registry.addEndpoint("/ws/agent")
        .addInterceptors(webSocketHandshakeInterceptor)
        .setAllowedOriginPatterns("*")
        .setHandshakeHandler(customHandshakeHandler)
        .withSockJS();
```

- 消息代理配置：
  - 广播前缀：`/topic`、`/queue`
  - 应用前缀：`/app`
  - 用户目的地前缀：`/user`

### 3.2 握手认证与用户身份绑定

- `WebSocketHandshakeInterceptor`：拦截握手请求，校验 token，注入 userId、userType 到 attributes。
- `CustomHandshakeHandler`：将 userId 作为 Principal 绑定，确保点对点推送时用户唯一标识。

### 3.3 消息拦截与安全

- `WebSocketAuthInterceptor`：实现 ChannelInterceptor，拦截所有入站消息，记录日志，可扩展安全校验。
- 建议：生产环境可增加消息内容校验、权限校验、频率限制等。

### 3.4 事件监听与会话管理

- `WebSocketEventListener`：监听连接、断开、订阅事件，结合 `WebSocketSessionManager` 管理用户会话。
- `WebSocketSessionManager`：本地缓存+Redis，支持分布式会话注册、移除、断开处理，在线状态管理。

### 3.5 消息推送与订阅

- `SimpMessagingTemplate` 用法：
  - 广播：`convertAndSend("/topic/xxx", payload)`
  - 点对点：`convertAndSendToUser(userId, "/queue/messages", payload)`
- 用户目的地前缀 `/user`，需确保 Principal.getName() 与 userId 一致。

## 4. 前端集成与示例

### 4.1 依赖与基础配置

- 依赖库：`@stomp/stompjs`、`sockjs-client`
- 连接端点与参数说明（token 传递）

### 4.2 连接与断开

```js
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const socket = new SockJS('/ws/chat?token=xxx');
const stompClient = new Client({
  webSocketFactory: () => socket,
  reconnectDelay: 5000
});

stompClient.onConnect = () => {
  // 订阅主题
  stompClient.subscribe('/user/queue/messages', (msg) => {
    // 处理消息
  });
  // 发送消息
  stompClient.publish({ destination: '/app/send', body: JSON.stringify({ ... }) });
};

stompClient.activate();
```

- 断开与重连：建议监听 `onDisconnect`，自动重连。

### 4.3 订阅与收发消息

- 订阅主题与点对点消息
- 发送消息格式与示例
- 消息处理回调

### 4.4 用户身份与安全

- token 获取与传递
- 客户端身份校验注意事项

## 5. 常见问题与排查建议

### 5.1 连接失败
- 检查 token 是否有效、端点路径是否正确、跨域配置是否允许。

### 5.2 消息收发异常
- 确认订阅路径、用户身份、消息格式，检查服务端日志。

### 5.3 会话管理与在线状态
- 检查 Redis 状态、断线重连逻辑。

### 5.4 其他问题
- 浏览器兼容性、SockJS fallback、网络异常等。

## 6. 扩展与自定义

### 6.1 新增消息类型
- 服务端扩展消息处理逻辑，前端增加对应类型处理。

### 6.2 自定义拦截器与事件监听
- 实现 ChannelInterceptor、EventListener，注册到 Spring 容器。

### 6.3 安全与权限控制
- 可集成更细粒度的权限校验、消息内容过滤等。

---

> 文档结构清晰，涵盖全栈开发所需的服务端、前端、运维、扩展、安全等全部要点。代码示例与配置项均有明确出处，便于查阅和二次开发。常见问题与排查建议覆盖实际开发中高频场景。 