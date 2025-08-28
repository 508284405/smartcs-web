# 企业内部 IM 通讯系统 MVP 架构设计

本文给出企业内部 IM 系统的 MVP 架构方案，并按本仓库 DDD 模块划分（client/domain/infrastructure/app/adapter/start）落地，覆盖总体架构、模块职责、关键数据流、存储与缓存设计、技术栈、接口契约、Kafka/Redis 主题与键空间、以及演进路线与落地任务清单。文末附与当前代码基的映射与待办差距。

## 总体架构
- 客户端通过 WebSocket（STOMP over WebSocket）与服务端建立长连接；REST 短连接用于登录鉴权与历史查询。
- 服务端拆分为：用户服务、好友关系服务、聊天服务（私聊/群聊）、离线消息服务、消息中转/推送服务。
- 横向扩展通过 Nginx/负载均衡分发连接；会话路由与在线态缓存基于 Redis；跨节点消息转发基于 Kafka。
- 数据持久化使用 MySQL（会话、消息、群组），热点态与未读计数、路由采用 Redis。

## 模块划分（对应本仓库）
- smartcs-web-client（DTO/API 契约）
  - DTO：`chat.ws.ChatMessage`、`chat.ws.AckMessage`、`chat.ws.WebSocketMessage` 已存在；可扩展 `Group*`、`Offline*` DTO。
  - API：`MessageService`、`MessageSendService`、`MessageValidatorService` 已存在；可扩展 `GroupService`、`OfflineMessageService`。
- smartcs-web-domain（领域模型与网关）
  - 已有：`domain.chat.Message`、`domain.chat.Session`、`MessageGateway`、`SessionGateway`、`MessageDomainService`。
  - 待扩展：`Group`、`GroupMember` 聚合与 `GroupGateway`；`OfflineMessage` 与 `OfflineMessageGateway`。
- smartcs-web-infrastructure（持久化、缓存、WebSocket/Kafka/Redis）
  - 已有：MyBatis‑Plus 映射 `t_cs_session`、`t_cs_message`；`WebSocketConfig`、`WebSocketSessionManager`（Redis + 本地）、KafkaTemplate 发送；SQL 脚本与 Mapper。
  - 新增：离线消息表、群组与成员表 DDL；相应 Mapper/Convertor/Gateway 实现（后续迭代）。
- smartcs-web-app（应用服务与编排）
  - 已有：`MessageSendServiceImpl`（存储 + WebSocket 推送 + Kafka 事件）、`MessageValidatorServiceImpl`、`SessionServiceImpl`。
  - 待扩展：`GroupChatServiceImpl`、`OfflineMessageServiceImpl`（上线触发补发/拉取）。
- smartcs-web-adapter（REST/WebSocket 控制器）
  - 已有：`ChatWebSocketController`（`/app/chat.sendMessage`、ACK、心跳）、历史接口 `ChatMessageController`。
  - 待扩展：群聊控制器、离线消息拉取/ACK 控制器；必要的管理端接口。
- start（配置/启动）
  - 已有：Kafka、Redis、Cache、WebSocket 配置；可补充 IM 专用 Kafka 主题与开关配置。

## 关键数据流
1) 登录与建连
   - 客户端经 REST 登录获取 Token；握手时以 `token` 参数通过 `WebSocketHandshakeInterceptor` 鉴权，并在 `CustomHandshakeHandler` 将 `userId` 绑定为 `Principal`。
   - `WebSocketSessionManager.registerSession` 记录本地与 Redis 的在线态与路由，`/user/{userId}/queue/*` 可点对点推送。

2) 私聊消息发送
   - 入口：`/app/chat.sendMessage` → `MessageValidatorService.validate` → `MessageSendServiceImpl.send`。
   - 持久化：写入 `t_cs_message`；更新会话最近时间。
   - 投递：若 `isUserOnline(toUserId)`，经 STOMP 发送至 `/user/{toUserId}/queue/messages`；否则写离线消息，并增加未读计数。
   - 事件：向 Kafka 发布消息事件（用于跨节点路由、审计、统计等）。

3) 群聊消息发送
   - 入口同上，服务侧依据群成员列表对在线成员实时推送，离线成员写离线/未读。
   - 存储采用“写一份正文 + 成员索引”的读扩散方案，减少重复存储。
   - 大群广播可写入 Kafka 由消费者集群并行下发。

4) 历史消息查询
   - REST：`GET /api/chat/messages/session/{sessionId}` 支持 beforeId/时间戳分页；已有分页接口 `/page`。
   - MySQL 按会话+时间索引；最近消息可加本地/Redis 缓存。

5) 离线消息推送（推拉结合）
   - 上线时：仅推送会话未读计数与摘要；用户进入会话后按页拉取详情；每批 ACK 后删除离线记录并扣减未读。
   - 移动端彻底离线：对接 APNs/FCM 仅做提醒，不含正文。

## 数据库与缓存设计
- MySQL 表（已存在）
  - `t_cs_session`：会话（索引：`session_id`、`customer_id`、`agent_id`、`session_state`）。
  - `t_cs_message`：消息（索引：`msg_id` 唯一、`session_id`、`chat_type`、`timestamp`）。
- 新增表（DDL 已附）
  - `t_im_offline_message`：离线消息存储（receiver_id + msg_id 唯一，过期清理策略）。
  - `t_im_unread_counter`：未读计数（user_id + conversation_id 唯一，或以 Redis 为主 DB 兜底）。
  - `t_im_group`、`t_im_group_member`：群组与成员关系。
- Redis 键空间建议
  - `ws:session:{userId}` → Hash(sessionId,userType) 路由映射。
  - `ws:agents`/`ws:customers` → 在线集合。
  - `im:unread:{userId}` → Hash(conversationId → count)。
  - `im:route:{userId}` → String(serverNodeId)（可选，与 `ws:session` 合并）。
  - `im:offline:{userId}` → List/Stream（可选，MVP 以 MySQL 为准）。
  - 分布式锁/限流：`im:lock:*` `im:rate:*`。

## Kafka 主题与分区
- 主题建议
  - `im.direct`：私聊跨节点转发（key=toUserId；按用户有序）。
  - `im.group`：群聊广播（key=groupId；按群有序）。
  - `im.event`：审计/统计/机器人联动事件。
  - 现有：`chat-messages`（已用于消息事件）。
- 消费组：`im-dispatcher`（节点内部路由）、`im-persist`（冗余/审计）、`im-notify`（移动推送）。

## 接口契约（MVP）
- WebSocket（STOMP）
  - 端点：`/ws/chat`（客户）、`/ws/agent`（客服）
  - 客户端订阅：`/user/queue/messages`（下行消息）、`/user/queue/heartbeat`、`/user/queue/session`（会话状态）
  - 客户端发送：`/app/chat.sendMessage`、`/app/chat.ack`、`/app/chat.heartbeat`
- REST
  - 历史查询：`GET /api/chat/messages/session/{sessionId}`（支持 beforeId/limit）；`GET /api/chat/messages/session/{sessionId}/page`（offset/limit）。
  - 离线：`GET /api/im/offline/summary`、`GET /api/im/offline/{conversationId}`、`POST /api/im/offline/ack`（后续补充）。
  - 群组：`POST /api/im/groups`、`POST /api/im/groups/{groupId}/members`、`POST /api/im/groups/{groupId}/messages` 等（后续补充）。

## 高可用与一致性
- WebSocket 会话路由在 Redis 中维护，节点无状态；Kafka 多副本；Redis Sentinel/Cluster；MySQL 主从或 MGR。
- 幂等：消息全局唯一 `msgId`（库唯一约束），ACK 确认后删除离线记录；重试去重。
- 顺序：会话内按时间/自增序列；跨节点按 Kafka 分区与 key 保障局部顺序。

## 与现有代码映射
- 已具备
  - STOMP 配置：`WebSocketConfig`，握手鉴权与 Principal 绑定：`WebSocketHandshakeInterceptor`、`CustomHandshakeHandler`。
  - 会话/消息：`SessionGatewayImpl`、`MessageGatewayImpl` + MyBatis/SQL；历史查询 REST；`MessageSendServiceImpl` 完成存储、在线推送与 Kafka 事件。
  - Redis 会话管理：`WebSocketSessionManager`（在线管理 / 点对点发送）。
- 待完善（本 PR 提出 DDL 与文档，代码后续迭代）
  - 离线消息服务：表结构、网关、应用服务、上线推拉逻辑、ACK 扣减未读。
  - 群聊：群组/成员表、网关、服务、消息 fanout 与大群优化。
  - WebSocket 在线检测：`isUserOnline` 改为 Redis/本地双查（本次已修复）。

## 迭代计划（建议）
1) 离线消息 MVP（1 周）
   - 建表与 Mapper；`OfflineMessageGateway`/`Service`；WebSocket ACK/REST 拉取；未读计数（Redis）。
2) 群聊 MVP（1.5 周）
   - 建表与 Mapper；群成员缓存；群发投递与大群 Kafka 广播；历史与未读。
3) 跨节点分发（0.5 周）
   - Kafka topic 拆分；消费与节点内分发；压测调优。

## 附：DDL 索引
- Chat（已存在）：`smartcs-web-infrastructure/src/main/resources/sql/modules/chat/02_cs_session.sql`、`03_cs_message.sql`
- 本次新增（见同目录）：`04_offline_message.sql`、`05_group.sql`

